import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

public class OrderSender implements Runnable {
  private static final Logger logger = Logger.getLogger(OrderSender.class);
  @Nonnull
  private final Supplier<List<Order>> ordersSupplier;
  private final int rate;

  public OrderSender(@Nonnull final Supplier<List<Order>> ordersSupplier, final int rate) {
    Preconditions.checkArgument(ordersSupplier != null, "ordersSupplier can not be null");
    Preconditions.checkArgument(rate > 0, "rate can not be < 0");

    this.ordersSupplier = ordersSupplier;
    this.rate = rate;
  }

  private boolean sendOrder(@Nonnull final Order order) throws IOException {
    final Response execute = Request.Post("http://localhost:11211")
        .bodyString(order.toString(), ContentType.APPLICATION_JSON)
        .execute();

    final HttpResponse httpResponse = execute.returnResponse();
    logger.info(httpResponse.getEntity().getContent());
    return true;
  }

  @Override
  public void run() {
    int limit = 1;
    int count = limit;
    for (Order order : ordersSupplier.get()) {
      if (count > limit) {
        break;
      }
      count++;
      try {
        logger.info(String.format("Sending Order %s", order));

        sendOrder(order);

        logger.info(String.format("Waiting %s for next order", rate));
        Thread.sleep(rate);

        break;
      } catch (InterruptedException | MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
