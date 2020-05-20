package common;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

/**
 * Encapsulates all the logic of choosing a shelf and placing an order on that shelf. It is a singleton thread-safe class
 * Also this class encapsulates {@link ShelfGarbageCollector} which is a GC for expired orders.
 * The GC runs on a diff thread on an interval decided by {@link ServerProperties#shelfGarbageCollectorIntervalInMS}
 */
public class ShelvesManager {
  private final static Logger logger = Logger.getLogger(ShelvesManager.class);
  private static volatile ShelvesManager INSTANCE;

  private final ConcurrentMap<Temp, Shelf> shelves;
  private final ShelfGarbageCollector shelfGarbageCollector;

  private ShelvesManager() {
    shelves = new ConcurrentHashMap<>();
    shelfGarbageCollector = new ShelfGarbageCollector();

    for (final Temp temp : Temp.values()) {
      shelves.put(temp, new Shelf(temp.getShelfName(), temp.getCapacity(), temp));
    }

    final Thread orderGc = new Thread(shelfGarbageCollector);
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

  public static void reset() {
    INSTANCE = null;
  }

  /**
   * Handles adding Order to a shelves
   *
   * @param order to be added
   * @return {@link Shelf} that the order added to, {@link Optional#empty()} if no shelves are available
   */
  public Optional<Shelf> addOrder(@Nonnull final Order order) {
    // Clean-up the shelf (remove expired orders)
    // Try adding the order to its shelf
    final Shelf shelf = shelves.get(order.getTemp());

    shelf.removeExpiredOrders();

    if (shelf.addOrder(order)) {
      logger.debug(String.format("Added Order to shelf. Order: %s, shelf: %s", order, shelf));
      return Optional.of(shelf);
    }

    // Order's shelf is full so lets
    // try the overflow shelf
    final Shelf overflowShelf = shelves.get(Temp.ANY);

    overflowShelf.removeExpiredOrders();

    if (overflowShelf.addOrder(order)) {
      logger.debug(String.format("Added Order to Overflow shelf. Order: %s, shelf: %s", order, shelf));
      return Optional.of(overflowShelf);
    }

    // Overflow is full so lets try to move any order out of overflow
    // either by moving it to the right shelf or completely discard it
    // because it is expired

    for (Iterator<Order> it = overflowShelf.getOrdersIterator(); it.hasNext(); ) {
      final Order overflowOrder = it.next();
      if (shelves.get(overflowOrder.getTemp()).addOrder(overflowOrder)) {
        overflowShelf.removeOrder(overflowOrder);
        logger.debug(String.format("Moved Order. From Overflow shelf to %s shelf. Order: %s", shelf, order));
      }
    }

    // Try adding again to overflow after reshuffling orders
    if (overflowShelf.addOrder(order)) {
      logger.debug(String.format("Added Order to Overflow shelf. Order: %s, shelf: %s", order, shelf));
      return Optional.of(overflowShelf);
    }

    // remove an order
    if (overflowShelf.removeOldestOrder().isPresent() && overflowShelf.addOrder(order)) {
      logger.debug(String.format("Added Order to shelf after force removing another order out. Order: %s, shelf: %s", order, shelf));
      return Optional.of(overflowShelf);
    }

    logger.info("Could not add order to any shelf!");

    return Optional.empty();
  }

  public boolean removeOrder(@Nonnull final Order order) {
    final Shelf shelf = shelves.get(order.getTemp());

    if (shelf.hasOrder(order)) {
      return shelf.removeOrder(order);
    }

    return false;
  }

  /**
   * A background thread that will take care of cleaning the shelves from any expired orders
   * The GC runs on a timer controlled by {@link ServerProperties#shelfGarbageCollectorIntervalInMS}
   */
  public class ShelfGarbageCollector implements Runnable {
    private Logger logger = Logger.getLogger(ShelfGarbageCollector.class);

    @Override
    public void run() {
      logger.info("Starting ShelfGarbageCollector thread");

      while (true) {
        try {
          runNow();
          Thread.sleep(ServerProperties.shelfGarbageCollectorIntervalInMS.get());
        } catch (final Exception e) {
          // We want this thread to keep running so we don't wanna any exception to escape
          logger.error("Error while collecting order", e);
        }
      }
    }

    public void runNow() {
      logger.debug("Cleaning up all shelves");

      final Set<Temp> keySet = shelves.keySet();

      for (final Temp temp : keySet) {
        final Shelf shelf = shelves.get(temp);
        shelf.removeExpiredOrders();
      }
    }
  }
}
