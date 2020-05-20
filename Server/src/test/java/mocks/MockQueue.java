package mocks;

import common.Order;
import common.Queue;
import java.util.LinkedList;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * Simple Mock queue class, in PROD queue will probably be a distributed queue like Kafka,...
 * so this class is a mocking for Unit-test
 */
public class MockQueue implements Queue {
  final java.util.Queue<Order> queue = new LinkedList<>();
  private final Supplier<Boolean> addFaultInjectorSupplier;
  private final Supplier<Order> fetchFaultInjectorSupplier;

  public MockQueue() {
    this(null, null);
  }

  public MockQueue(@Nonnull final Supplier<Boolean> addFaultInjectorSupplier, @Nonnull final Supplier<Order> fetchFaultInjectorSupplier) {
    this.addFaultInjectorSupplier = addFaultInjectorSupplier;
    this.fetchFaultInjectorSupplier = fetchFaultInjectorSupplier;
  }

  @Override
  public Order fetch() {
    if(fetchFaultInjectorSupplier != null) {
      return fetchFaultInjectorSupplier.get();
    }

    return queue.poll();
  }

  @Override
  public boolean add(@Nonnull Order order) {
    if (addFaultInjectorSupplier != null) {
      return addFaultInjectorSupplier.get();
    }

    return queue.add(order);
  }

  public int size() {
    return queue.size();
  }
}