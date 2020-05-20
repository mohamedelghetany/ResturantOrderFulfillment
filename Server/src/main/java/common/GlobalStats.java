package common;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;

/**
 * Singleton class that contains Statistics about the orders
 *
 * To get an instance of this class, use {@link GlobalStats#getInstance()}
 * Call {@link GlobalStats#initialize()} to reset statistics
 *
 * This class also starts a {@link StatsReporter} thread once an instance created.
 * This thread reports Global Stats every {@link ServerProperties#statsReporterIntervalInMS}.
 * Currently the reporter just Logs but that can be changed to publish to any additional monitoring tools
 *
 * Current statistics are:
 *
 * {@link GlobalStats#receivedOrdersCount} gets incremented every time the server receives an order
 * {@link GlobalStats#processedOrdersCount} gets incremented every time the server process an order successfully
 * {@link GlobalStats#dispatchedOrdersCount} gets incremented every time the server dispatches an order successfully
 * {@link GlobalStats#failedToDispatchCount} gets incremented every time the server fails to dispatch an order
 * {@link GlobalStats#discardedOrdersCount} gets incremented every time the server discards an order before it's being picked up
 */
public class GlobalStats {
  private static GlobalStats INSTANCE;
  private AtomicInteger discardedOrdersCount = new AtomicInteger();
  private AtomicInteger receivedOrdersCount = new AtomicInteger();
  private AtomicInteger processedOrdersCount = new AtomicInteger();
  private AtomicInteger dispatchedOrdersCount = new AtomicInteger();
  private AtomicInteger failedToDispatchCount = new AtomicInteger();
  private AtomicInteger expiredOrdersCount = new AtomicInteger();

  private GlobalStats() {
    final Thread statsReporterThread = new Thread(new StatsReporter());
    statsReporterThread.setName("GlobalStats-StatsReporter");
    statsReporterThread.start();
  }

  public static GlobalStats getInstance() {
    if (INSTANCE == null) {
      synchronized (GlobalStats.class) {
        if (INSTANCE == null) {
          INSTANCE = new GlobalStats();
        }
      }
    }

    return INSTANCE;
  }

  public void initialize() {
    receivedOrdersCount.set(0);
    processedOrdersCount.set(0);
    dispatchedOrdersCount.set(0);
    failedToDispatchCount.set(0);
    discardedOrdersCount.set(0);
  }

  public void reportDiscardedOrder() {
    discardedOrdersCount.addAndGet(1);
  }

  public void reportReceivedOrder() {
    receivedOrdersCount.addAndGet(1);
  }

  public void reportProcessedOrder() {
    processedOrdersCount.addAndGet(1);
  }

  public void reportDispatchedOrder() {
    dispatchedOrdersCount.addAndGet(1);
  }

  public void reportFailedDispatch() {
    failedToDispatchCount.addAndGet(1);
  }

  public void reportExpiredOrder() {
    expiredOrdersCount.addAndGet(1);
  }

  public AtomicInteger getReceivedOrdersCount() {
    return receivedOrdersCount;
  }

  public AtomicInteger getProcessedOrdersCount() {
    return processedOrdersCount;
  }

  public AtomicInteger getDispatchedOrdersCount() {
    return dispatchedOrdersCount;
  }

  public AtomicInteger getDiscardedOrdersCount() {
    return discardedOrdersCount;
  }

  public AtomicInteger getFailedToDispatchCount() {
    return failedToDispatchCount;
  }

  public AtomicInteger getExpiredOrdersCount() {
    return expiredOrdersCount;
  }

  public static class StatsReporter implements Runnable {
    private static final Logger logger = Logger.getLogger(StatsReporter.class);

    @Override
    public void run() {
      try {
        logger.info("Starting GlobalStats reporter");

        while (true) {
          final String report = String.format("GlobalStats - # Received Orders: %d, # Processed Orders: %d, # Dispatched Orders: %d, # Discarded Orders: %d, # Failed Pickup: %d, # Expired: %d",
              GlobalStats.getInstance().getReceivedOrdersCount().get(),
              GlobalStats.getInstance().getProcessedOrdersCount().get(),
              GlobalStats.getInstance().getDispatchedOrdersCount().get(),
              GlobalStats.getInstance().getDiscardedOrdersCount().get(),
              GlobalStats.getInstance().getFailedToDispatchCount().get(),
              GlobalStats.getInstance().getExpiredOrdersCount().get());

          logger.info(report);

          Thread.sleep(ServerProperties.statsReporterIntervalInMS.get());
        }
      } catch (InterruptedException e) {
        logger.warn("StatsReporter sleep interrupted");
      }
    }
  }
}
