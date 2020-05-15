package protocol;

import common.Order;
import common.Queue;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Decode incoming order
 *
 * This class extends {@link ReplayingDecoder} for simplicity. I was considering extending
 * {@link io.netty.handler.codec.ByteToMessageDecoder}, the biggest difference between ReplayingDecoder and ByteToMessageDecoder
 * is that ReplayingDecoder allows you to implement the decode() and decodeLast() methods just like all
 * required bytes were received already, rather than checking the availability of the required bytes.
 * Off course this simplicity doesn't come for free. There is a slight performance hit.
 *
 * More details: https://netty.io/4.0/api/io/netty/handler/codec/ReplayingDecoder.html
 */
public class InputDecoder extends MessageToMessageDecoder<String> {
  private static final Logger logger = LogManager.getLogger(InputDecoder.class);
  private final Queue orderQueue;
  private final Queue dispatcherQueue;

  public InputDecoder(@Nonnull final Queue orderQueue, @Nonnull final Queue dispatcherQueue) {
    this.orderQueue = orderQueue;
    this.dispatcherQueue = dispatcherQueue;
  }


  @Override
  protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
    logger.info(msg);

  }
}
