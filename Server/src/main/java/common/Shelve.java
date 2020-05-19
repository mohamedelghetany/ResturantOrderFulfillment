package common;

import io.netty.util.internal.ConcurrentSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

/**
 * Encapsulate a "Shelve" with a list of Orders on it
 */
public class Shelve {
  private static Logger logger = Logger.getLogger(Shelve.class);

  private final String name;
  private final int capacity;
  private final Temp temperature;
  private final ConcurrentSet<Order> orders;
  private AtomicInteger count;

  public Shelve(@Nonnull final String name, final int capacity, @Nonnull final Temp temperature) {
    this.name = name;
    this.capacity = capacity;
    this.temperature = temperature;
    orders = new ConcurrentSet<>();
    count = new AtomicInteger();
  }

  public boolean addOrder(@Nonnull final Order order) {
    if (count.get() >= capacity) {
      return false;
    }

    final boolean result = orders.add(order);

    if (result) {
      count.incrementAndGet();
    }

    return result;
  }

  public boolean removeOrder(@Nonnull final Order order) {
    final boolean result = orders.remove(order);

    if (result) {
      count.decrementAndGet();
    }

    return result;
  }

  public boolean hasOrder(@Nonnull final Order order) {
    return orders.contains(order);
  }

  public Iterator<Order> getOrdersIterator() {
    return orders.iterator();
  }

  public List<Order> removeExpiredOrders() {
    final List<Order> result = new ArrayList<>();

    for (final Iterator<Order> it = orders.iterator(); it.hasNext(); ) {
      final Order order = it.next();

      if (order.updateAndGetLife() <= 0f) {
        removeOrder(order);
        logger.info(String.format("Removed an expired order %s", order));
        result.add(order);
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return name;
  }

  public Temp getTemperature() {
    return temperature;
  }

  public String getName() {
    return name;
  }
}
