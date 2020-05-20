package processor;

import common.Order;
import common.ShelvesManager;
import common.Temp;
import mocks.MockQueue;
import org.junit.Assert;
import org.junit.Test;

public class OrderProcessorTest {
  @Test
  public void testOrderProcessorProcessOrderImmediately() {
    ShelvesManager.reset();
    
    final MockQueue queue = new MockQueue();
    final Order order = new Order("1235", "Order1", Temp.HOT, 100, 0.1f);
    queue.add(order);

    OrderProcessor processor = new OrderProcessor(queue);
    processor.run(() -> queue.size() != 0);

    Assert.assertTrue(ShelvesManager.getInstance().removeOrder(order));
  }
}
