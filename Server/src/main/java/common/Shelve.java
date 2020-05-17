package common;

import io.netty.util.internal.ConcurrentSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class Shelve {
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
    if (count.get() > capacity) {
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

  @Override
  public String toString() {
    return name;
  }
}
