import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;

public class Main {
  private final static Logger logger = Logger.getLogger(Main.class);

  public static void main(String[] args) throws FileNotFoundException {
    ClientProperties.initialize();
    final Reader reader = new BufferedReader(new FileReader(Main.class.getClassLoader().getResource("").getPath() + "orders.json"));

    for (final Order order : Arrays.asList(new Gson().fromJson(reader, Order[].class))) {
      try {
        logger.info(String.format("Sending Order %s", order));

        sendOrder(order);

        logger.info(String.format("Waiting %s for next order", ClientProperties.RATE.get()));
        Thread.sleep(ClientProperties.RATE.get());
      } catch (InterruptedException | MalformedURLException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static boolean sendOrder(@Nonnull final Order order) throws IOException {
    final Response response = Request.Post("http://localhost:11211")
        .bodyString(order.toString(), ContentType.APPLICATION_JSON)
        .execute();

    response.discardContent();
    return true;
  }
}
