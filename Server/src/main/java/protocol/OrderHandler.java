package protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.sun.tools.corba.se.idl.constExpr.Or;
import common.Order;
import common.Queue;
import common.RestaurantException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

/**
 * Handles cache commands
 */
public class OrderHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
  private static final Logger logger = Logger.getLogger(OrderHandler.class);

  private final Queue orderQueue;
  private final Queue dispatcherQueue;

  public OrderHandler(@Nonnull final Queue orderQueue, @Nonnull final Queue dispatcherQueue) {
    this.orderQueue = orderQueue;
    this.dispatcherQueue = dispatcherQueue;
  }
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws RestaurantException {
    final String strOrder = request.content().toString(CharsetUtil.UTF_8);
    final Order order = new Gson().fromJson(strOrder, Order.class);


    logger.debug("Received order " + order);

    dispatcherQueue.add(order);
    orderQueue.add(order);

    ctx.writeAndFlush(order);
  }
}
