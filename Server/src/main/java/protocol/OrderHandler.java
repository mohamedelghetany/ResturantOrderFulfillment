package protocol;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.google.gson.Gson;
import common.Order;
import common.Queue;
import common.RestaurantException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

/**
 * Handles cache commands
 */
public class OrderHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger logger = Logger.getLogger(OrderHandler.class);
  private final Queue orderQueue;
  private final Queue dispatcherQueue;

  public OrderHandler(Queue orderQueue, Queue dispatcherQueue) {
    this.orderQueue = orderQueue;
    this.dispatcherQueue = dispatcherQueue;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws RestaurantException {
    final String strOrder = request.content().toString(CharsetUtil.UTF_8);
    final Order order = new Gson().fromJson(strOrder, Order.class);

    logger.info("Received order " + order);

    // final Processor dispatcher = new DispatcherV2();
    // dispatcher.process(order);
    // final Processor orderProcessor = new OrderProcessorV2();
    // orderProcessor.process(order);

    orderQueue.add(order);
    dispatcherQueue.add(order);

    ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, OK));
  }
}
