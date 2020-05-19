import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.log4j.Logger;

public final class OrderSender {
  private static final Logger logger = Logger.getLogger(OrderSender.class);
  private final CloseableHttpAsyncClient httpClient;

  public OrderSender() {
    httpClient = HttpAsyncClients.createDefault();
    httpClient.start();
  }

  public void sendOrders(@Nonnull final List<Order> orders, final int rate) {
    Preconditions.checkNotNull(orders);

    final RateLimiter rateLimiter = RateLimiter.create(rate);
    IntStream.range(0, 4).forEach(index -> {
      rateLimiter.acquire();
      sendOrder(orders.get(index));
    });
  }

  private boolean sendOrder(@Nonnull final Order order) {
    logger.info(String.format("Sending Order %s", order));

    final HttpPost request = new HttpPost("http://localhost:8080");
    request.setEntity(new StringEntity(order.toString(), ContentType.APPLICATION_JSON));

    httpClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse httpResponse) {
        logger.debug(String.format("Order Sent %s Status %s", order, httpResponse.getStatusLine()));
      }

      @Override
      public void failed(Exception e) {
        logger.error(String.format("Failed to send order %s", order), e);
      }

      @Override
      public void cancelled() {
      }
    });

    return true;
  }
}
