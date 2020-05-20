package protocol;

import common.GlobalStats;
import common.OrdersQueue;
import common.Queue;
import common.ServerProperties;
import courier.DispatcherManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.log4j.Logger;
import processor.OrderProcessorManager;

/**
 * Simple Netty Server to process an order.
 *
 * The way that the server works,
 * When the server receives an order it adds it to {@link Queue} then another set of threads will process the orders in the queue
 * By doing that we are
 * 1- In real PROD system, my {@link Queue} can be a distributed queue like Kafka. My server should still work by just providing
 * an adaptor to Kafka
 * 2- Not blocking netty threads for a long time, it is only blocked for the times used to add to the queue
 */
public class RestaurantServer {
  private static final Logger logger = Logger.getLogger(RestaurantServer.class);
  private final int port;
  private int nettyWorkerThreadCount;

  public RestaurantServer(final int port, int availableProcessorsCount) {
    this.port = port;
    this.nettyWorkerThreadCount = availableProcessorsCount;
  }

  private void initAndRun() throws InterruptedException {
    logger.info("Initializing and running RestaurantServer...");

    final EventLoopGroup bossGroup = new NioEventLoopGroup();
    // This can be changed by -Dio.netty.eventLoopThreads
    final EventLoopGroup workerGroup = new NioEventLoopGroup(nettyWorkerThreadCount);

    try {
      GlobalStats.getInstance().initialize();
      final Queue orderQueue = new OrdersQueue();
      final Queue dispatcherQueue = new OrdersQueue();
      final OrderProcessorManager orderProcessorManager = new OrderProcessorManager(orderQueue);
      final DispatcherManager dispatcherManager = new DispatcherManager(dispatcherQueue);
      orderProcessorManager.initialize();
      dispatcherManager.initialize();

      final EventExecutorGroup handlerGroup = new DefaultEventExecutorGroup(nettyWorkerThreadCount);
      final ServerBootstrap serverBootstrap = new ServerBootstrap();
      serverBootstrap.group(bossGroup, workerGroup);
      serverBootstrap.channel(NioServerSocketChannel.class);
      serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        protected void initChannel(SocketChannel ch) {
          ch.pipeline().addLast(new HttpServerCodec());
          ch.pipeline().addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
          ch.pipeline().addLast(handlerGroup, "OrderHandler" ,new OrderHandler(orderQueue, dispatcherQueue));
        }
      });
      serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
      serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

      final ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

      // Run & Wait
      logger.info(String.format("Starting server on Port %d ", port));
      channelFuture.channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    ServerProperties.initialize();

    final int port = ServerProperties.PORT.get();

    final int availableProcessorsCount = Runtime.getRuntime().availableProcessors() * 2;

    logger.debug(String.format("Found %d available processors", availableProcessorsCount));

    new RestaurantServer(port, availableProcessorsCount).initAndRun();
  }
}
