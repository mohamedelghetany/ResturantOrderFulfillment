package protocol;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import common.Order;
import common.Queue;
import common.RestaurantException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

/**
 * Simple Netty Channel Inbound Handler to handel incoming orders
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
    String strOrder = null;

    try {
      strOrder = request.content().toString(CharsetUtil.UTF_8);
      final Order order = Order.createFromJson(strOrder);

      logger.info("Received order " + order);

      final boolean addOrderQueueResult = orderQueue.add(order);
      final boolean dispatcherQueueAddResult = dispatcherQueue.add(order);

      ctx.writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, (addOrderQueueResult && dispatcherQueueAddResult) ? OK : SERVICE_UNAVAILABLE));

    } catch (final Exception e) {
      final String errorMessage = String.format("Error while processing order %s", strOrder);
      logger.error(errorMessage);
      throw new RestaurantException(errorMessage, e);
    }
  }
}
