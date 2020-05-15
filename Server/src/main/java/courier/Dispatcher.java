package courier;

import common.Order;
import common.Queue;
import common.ServerProperties;
import common.ShelvesManager;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

public class Dispatcher implements Runnable {
  private static Logger logger = Logger.getLogger(Dispatcher.class);
  private final Queue queue;

  public Dispatcher(@Nonnull final Queue queue) {
    this.queue = queue;
  }

  @Override
  public void run() {
    logger.info("Starting Dispatcher thread....");
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
        logger.debug(String.format("Dispatching order %s", order));

        final int randomWait = (int) (Math.random() * (ServerProperties.dispatcherHighWaitTimeInSeconds.get() - ServerProperties.dispatcherLowWaitTimeInSeconds.get()))
            + ServerProperties.dispatcherLowWaitTimeInSeconds.get();

        logger.debug(String.format("Waiting for dispatcher. Wait time: %d", randomWait));

        Thread.sleep(TimeUnit.SECONDS.toMillis(randomWait));

        if (ShelvesManager.getInstance().getOrder(order)) {
          logger.info(String.format("Dispatcher pickup order %s", order));
        } else {
          logger.error(String.format("Dispatcher failed to pickup order %s", order));
        }
      } catch (final Exception e) {
        // We want this thread to keep running so we don't wanna any exception to escape
        // In here we will just log, yes we might have failed to process this order and
        // we might need to enqueue the order again or Alert
        //TODO: stats
        logger.error("Error while pooling from the queue", e);
      }
    }
  }
}
