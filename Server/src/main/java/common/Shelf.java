package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.apache.log4j.Logger;

/**
 * Encapsulate a "Shelf" with a list of Orders on it
 */
public class Shelf {
  private static Logger logger = Logger.getLogger(Shelf.class);

  private final String name;
  private final int capacity;
  private final Temp temperature;
  private final ConcurrentMap<String, Order> orders;
  private AtomicInteger count;

  public Shelf(@Nonnull final String name, final int capacity, @Nonnull final Temp temperature) {
    this.name = name;
    this.capacity = capacity;
    this.temperature = temperature;
    orders = new ConcurrentHashMap<>();
    count = new AtomicInteger();
  }

  public boolean addOrder(@Nonnull final Order order) {
    if (count.get() >= capacity) {
      return false;
    }

    orders.put(getKey(order), order);
    count.incrementAndGet();

    return true;
  }

  public boolean removeOrder(@Nonnull final Order order) {
    if(!orders.containsKey(getKey(order))) {
      return false;
    }

    orders.remove(getKey(order));
    count.decrementAndGet();

    return true;
  }

  public boolean hasOrder(@Nonnull final Order order) {
    return orders.containsKey(getKey(order));
  }

  public Iterator<Order> getOrdersIterator() {
    return orders.values().iterator();
  }

  public List<Order> removeExpiredOrders() {
    final List<Order> result = new ArrayList<>();

    for (final Iterator<Order> it = getOrdersIterator(); it.hasNext(); ) {
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

  public Optional<Order> removeOldestOrder() {
    if(orders.isEmpty()) {
      return Optional.empty();
    }

    final List<Order> values = new ArrayList<>(orders.values());
    Collections.sort(values, Comparator.comparingLong(Order::getCreateTimeStamp));
    final Order oldestOrder = values.get(0);

    removeOrder(oldestOrder);

    return Optional.of(oldestOrder);
  }

  private String getKey(Order order) {
    return order.getId();
  }
}
