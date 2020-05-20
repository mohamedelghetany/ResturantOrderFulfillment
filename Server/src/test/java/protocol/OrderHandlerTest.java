package protocol;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import common.Order;
import common.RestaurantException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.Charset;
import mocks.MockQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

public class OrderHandlerTest {
  private ChannelHandlerContext mockCtx;

  @Before
  public void initialize() {
    mockCtx = BDDMockito.mock(ChannelHandlerContext.class);
  }

  @Test
  public void testOrderHandlerDeserializeOrderCorrectly() throws Exception {
    final String orderStr = " {\n" +
        "    \"id\": \"a8cfcb76-7f24-4420-a5ba-d46dd77bdffd\",\n" +
        "    \"name\": \"Banana Split\",\n" +
        "    \"temp\": \"frozen\",\n" +
        "    \"shelfLife\": 20,\n" +
        "    \"decayRate\": 0.63\n" +
        "  }";
    final Order order = Order.createFromJson(orderStr);

    final MockQueue orderQueue = new MockQueue();
    final MockQueue dispatcherQueue = new MockQueue();

    final OrderHandler handler = new OrderHandler(orderQueue, dispatcherQueue);
    final FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:1234", Unpooled.wrappedBuffer(orderStr.getBytes(Charset.defaultCharset())));

    handler.channelRead(mockCtx, fullHttpRequest);

    Assert.assertEquals(1, orderQueue.size());
    Assert.assertEquals(1, dispatcherQueue.size());

    Assert.assertEquals(order, orderQueue.fetch());
    Assert.assertEquals(order, dispatcherQueue.fetch());

    Mockito.verify(mockCtx).writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, OK));
  }

  @Test
  public void testOrderHandlerReturnsServiceUnavailableWhenFailToAddToOrderQueue() throws Exception {
    final String orderStr = "{\n" +
        "    \"id\": \"2ec069e3-576f-48eb-869f-74a540ef840c\",\n" +
        "    \"name\": \"Acai Bowl\",\n" +
        "    \"temp\": \"cold\",\n" +
        "    \"shelfLife\": 249,\n" +
        "    \"decayRate\": 0.3\n" +
        "  }";

    final MockQueue orderQueue = new MockQueue(() -> false, null);
    final OrderHandler handler = new OrderHandler(orderQueue, new MockQueue());
    final FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:1234", Unpooled.wrappedBuffer(orderStr.getBytes(Charset.defaultCharset())));

    handler.channelRead(mockCtx, fullHttpRequest);

    Assert.assertEquals(0, orderQueue.size());

    Mockito.verify(mockCtx).writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE));
  }

  @Test
  public void testOrderHandlerReturnsServiceUnavailableWhenFailToAddToDispatchingQueue() throws Exception {
    final String orderStr = "{\n" +
        "    \"id\": \"58e9b5fe-3fde-4a27-8e98-682e58a4a65d\",\n" +
        "    \"name\": \"McFlury\",\n" +
        "    \"temp\": \"frozen\",\n" +
        "    \"shelfLife\": 375,\n" +
        "    \"decayRate\": 0.4\n" +
        "  }";

    final MockQueue dispatcherQueue = new MockQueue(() -> false, null);
    final OrderHandler handler = new OrderHandler(new MockQueue(), dispatcherQueue);
    final FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:1234", Unpooled.wrappedBuffer(orderStr.getBytes(Charset.defaultCharset())));

    handler.channelRead(mockCtx, fullHttpRequest);

    Assert.assertEquals(0, dispatcherQueue.size());

    Mockito.verify(mockCtx).writeAndFlush(new DefaultFullHttpResponse(HTTP_1_1, SERVICE_UNAVAILABLE));
  }

  @Test(expected = RestaurantException.class)
  public void testOrderHandlerOnlyThrowsRestaurantException() throws Exception {
    final String orderStr = "{\n" +
        "    \"id\": \"58e9b5fe-3fde-4a27-8e98-682e58a4a65d\",\n" +
        "    \"name\": \"McFlury\",\n" +
        "    \"temp\": \"frozen\",\n" +
        "    \"shelfLife\": 375,\n" +
        "    \"decayRate\": 0.4\n" +
        "  }";

    final MockQueue dispatcherQueue = new MockQueue(() -> {
      throw new RuntimeException("Oops, Random error");
    }, null);

    final OrderHandler handler = new OrderHandler(new MockQueue(), dispatcherQueue);
    final FullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "http://localhost:1234", Unpooled.wrappedBuffer(orderStr.getBytes(Charset.defaultCharset())));

    handler.channelRead(mockCtx, fullHttpRequest);
  }
}
