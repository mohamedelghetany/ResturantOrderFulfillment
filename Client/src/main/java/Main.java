import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

public class Main {
  private final static Logger logger = Logger.getLogger(Main.class);

  public static void main(String[] args) throws FileNotFoundException {
    logger.info("Client started");

    ClientProperties.initialize();
    final Reader reader = new BufferedReader(new FileReader(Main.class.getClassLoader().getResource("").getPath() + "orders.json"));
    final List<Order> orders = Arrays.asList(new Gson().fromJson(reader, Order[].class));

    final OrderSender sender = new OrderSender();
    sender.sendOrders(orders, ClientProperties.RATE.get());
  }
}
