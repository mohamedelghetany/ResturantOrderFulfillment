import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.RateLimiter;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.log4j.Logger;

/**
 * A simple class to that sends orders to the server with a specific rate.
 * Main method is {@link OrderSender#sendOrders(List, int)}
 */
public final class OrderSender {
  private static final Logger logger = Logger.getLogger(OrderSender.class);
  private final CloseableHttpAsyncClient httpClient;
  private final Executor executor;

  public OrderSender() {
    httpClient = HttpAsyncClients.createDefault();
    executor = Executors.newCachedThreadPool();
    httpClient.start();
  }

  /**
   * Send the list of orders with the given rate
   *
   * @param orders to be sent
   * @param rate that will be used to send the orders
   */
  public void sendOrders(@Nonnull final List<Order> orders, final int rate) {
    Preconditions.checkNotNull(orders);

    final RateLimiter rateLimiter = RateLimiter.create(rate);
    IntStream.range(0, 4).forEach(index -> {
      rateLimiter.acquire();
      executor.execute(() -> sendOrder(orders.get(index)));
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
