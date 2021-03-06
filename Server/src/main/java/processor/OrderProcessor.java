package processor;

import common.GlobalStats;
import common.Order;
import common.Queue;
import common.Shelf;
import common.ShelvesManager;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

public class OrderProcessor implements Runnable {
  private static Logger logger = Logger.getLogger(OrderProcessor.class);
  private final Queue queue;

  public OrderProcessor(@Nonnull final Queue queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    logger.info("Starting OrderProcessor thread.");
    run(() -> true);
  }

  /**
   * Keep running in a loop while the given condition is true!
   *
   * @param condition the condition when met Run will end (the thread will stop)
   */
  public void run(@Nonnull final Supplier<Boolean> condition) {
    while (condition.get()) {
      try {
        final Order order = queue.fetch();
        logger.info(String.format("Processing order %s", order));

        final Optional<Shelf> shelf = ShelvesManager.getInstance().addOrder(order);

        if(shelf.isPresent()){
          logger.info(String.format("Added order to shelf. Order %s -> shelf %s", order, shelf.get()));
          GlobalStats.getInstance().reportProcessedOrder();
        }else {
          logger.error(String.format("Failed to add an order to any shelf"));
        }

      } catch (final Exception e) {
        // We want this thread to keep running so we don't wanna any exception to escape
        // In here we will just log, yes we might have failed to process this order and
        // we might need to enqueue the order again or Alert. For now lets log
        logger.error("Error while pooling from the queue", e);
      }
    }
  }
}
