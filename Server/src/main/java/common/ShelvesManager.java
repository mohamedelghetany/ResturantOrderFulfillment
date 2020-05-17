package common;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

/**
 * Encapsulates all the logic of choosing a shelve and placing an order on the chosen shelve
 */
public class ShelvesManager {
  private final static Logger logger = Logger.getLogger(ShelvesManager.class);
  private static ShelvesManager INSTANCE;

  private final ConcurrentMap<Temp, Shelve> shelves;
  private final ShelveOrderGarbageCollector shelveOrderGarbageCollector;

  private ShelvesManager() {
    shelves = new ConcurrentHashMap<>();
    shelveOrderGarbageCollector = new ShelveOrderGarbageCollector();

    for(final Temp temp : Temp.values()) {
      shelves.put(temp, new Shelve(temp.getShelfName(), temp.getCapacity(), temp));
    }

    final Thread orderGc = new Thread(shelveOrderGarbageCollector);
    orderGc.setName("OrdersGarbageCollector");
    orderGc.start();
  }

  public static ShelvesManager getInstance() {
    if (INSTANCE == null) {
      synchronized (ShelvesManager.class) {
        if (INSTANCE == null) {
          INSTANCE = new ShelvesManager();
        }
      }
    }

    return INSTANCE;
  }

  /**
   * Handles adding Order to a shelves
   *
   * @param order to be added
   * @return {@link Shelve} that the order added to, {@link Optional#empty()} if no shelves are available
   */
  public Optional<Shelve> addOrder(@Nonnull final Order order) {
    // Try adding the order to its shelve
    final Shelve shelve = shelves.get(order.getTemp());

    if (shelve.addOrder(order)) {
      logger.debug(String.format("Added Order to shelve. Order: %s, shelve: %s", order, shelve));
      return Optional.of(shelve);
    }

    // Order's temperature shelve is full so lets
    // try the overflow shelve
    final Shelve overflowShelve = shelves.get(Temp.ANY);

    if (overflowShelve.addOrder(order)) {
      logger.debug(String.format("Added Order to Overflow shelve. Order: %s, shelve: %s", order, shelve));
      return Optional.of(overflowShelve);
    }

    // Overflow is full so lets try to move any order out of overflow
    // either by moving it to the right shelve or completely discard it
    // because it is expired

    for (Iterator<Order> it = overflowShelve.getOrdersIterator(); it.hasNext(); ) {
      final Order overflowOrder = it.next();
      if (shelves.get(overflowOrder.getTemp()).addOrder(overflowOrder)) {
        logger.debug(String.format("Moved Order. From Overflow shelve to %s shelve. Order: %s", shelve, order));
      }
    }

    // Try adding again to overflow
    if (overflowShelve.addOrder(order)) {
      logger.debug(String.format("Added Order to Overflow shelve. Order: %s, shelve: %s", order, shelve));
      return Optional.of(overflowShelve);
    }

    shelveOrderGarbageCollector.forceRun();

    // Try adding again to overflow
    if (overflowShelve.addOrder(order)) {
      logger.debug(String.format("Added Order to Overflow shelve. Order: %s, shelve: %s", order, shelve));
      return Optional.of(overflowShelve);
    }

    logger.debug("Could not add order to any shelve!");

    return Optional.empty();
  }

  public boolean getOrder(@Nonnull final Order order) {
    final Shelve shelve = shelves.get(order.getTemp());

    if (shelve.hasOrder(order)) {
      return shelve.removeOrder(order);
    }

    return false;
  }

  /**
   * A background thread that will take care of cleaning the shelves from any expired orders
   * The GC runs on a timer controlled by {@link ServerProperties#orderGCIntervalInMS}
   */
  public class ShelveOrderGarbageCollector implements Runnable {
    private Logger logger = Logger.getLogger(ShelveOrderGarbageCollector.class);

    @Override
    public void run() {
      logger.info("Starting ShelveOrderGarbageCollector thread....");

      while (true) {
        try {
          forceRun();
          Thread.sleep(ServerProperties.orderGCIntervalInMS.get());
        } catch (final Exception e) {
          // We want this thread to keep running so we don't wanna any exception to escape
          logger.error("Error while collecting order", e);
        }
      }
    }

    public void forceRun() {
      logger.debug("Cleaning up all shelves");

      final Set<Temp> keySet = shelves.keySet();

      for (final Temp temp : keySet) {
        final Shelve shelve = shelves.get(temp);

        for (final Iterator<Order> it = shelve.getOrdersIterator(); it.hasNext(); ) {
          final Order order = it.next();

          if (order.updateAndGetLife() <= 0f) {
            shelve.removeOrder(order);
            logger.info(String.format("Removed an expired order %s", order));
          }
        }
      }
    }
  }
}
