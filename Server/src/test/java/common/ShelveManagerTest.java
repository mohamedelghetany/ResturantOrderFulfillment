package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ShelveManagerTest {

  @Before
  public void init() {
    ShelvesManager.reset();
  }

  @Test
  public void testAddOrderAddsOverCapacityToOverflowShelf() {
    createAndAddRandomOrders(Temp.COLD.getCapacity(), Temp.COLD, "coldOrders");

    final Order order = new Order("100", "testOrder", Temp.COLD, 1, 0.5f);
    final ShelvesManager manager = ShelvesManager.getInstance();
    final Optional<Shelve> shelve = manager.addOrder(order);

    Assert.assertTrue(shelve.isPresent());
    Assert.assertEquals(Temp.ANY.getShelfName(), shelve.get().getName());
    Assert.assertEquals(Temp.ANY, shelve.get().getTemperature());
  }

  @Test
  public void testAddOrderAddsToTheCorrectShelf() {
    final Order coldOrder = new Order("100", "testOrder", Temp.COLD, 1, 0.5f);
    final Order hotOrder = new Order("100", "testOrder", Temp.HOT, 1, 0.5f);
    final Order frozenOrder = new Order("100", "testOrder", Temp.FROZEN, 1, 0.5f);

    final ShelvesManager manager = ShelvesManager.getInstance();

    final Optional<Shelve> shelveCold = manager.addOrder(coldOrder);
    Assert.assertTrue(shelveCold.isPresent());
    Assert.assertEquals(Temp.COLD.getShelfName(), shelveCold.get().getName());
    Assert.assertEquals(Temp.COLD, shelveCold.get().getTemperature());
    Assert.assertTrue(shelveCold.get().hasOrder(coldOrder));

    final Optional<Shelve> shelveHot = manager.addOrder(hotOrder);
    Assert.assertTrue(shelveHot.isPresent());
    Assert.assertEquals(Temp.HOT.getShelfName(), shelveHot.get().getName());
    Assert.assertEquals(Temp.HOT, shelveHot.get().getTemperature());
    Assert.assertTrue(shelveHot.get().hasOrder(hotOrder));

    final Optional<Shelve> shelveFrozen = manager.addOrder(frozenOrder);
    Assert.assertTrue(shelveFrozen.isPresent());
    Assert.assertEquals(Temp.FROZEN.getShelfName(), shelveFrozen.get().getName());
    Assert.assertEquals(Temp.FROZEN, shelveFrozen.get().getTemperature());
    Assert.assertTrue(shelveFrozen.get().hasOrder(frozenOrder));
  }

  @Test
  public void testAddOrderToTheRightShelveAfterBeingFull() {
    // Fill the Hot shelf which will overflow to Overflow shelf which will get filled
    final List<Order> orders = createAndAddRandomOrders(Temp.HOT.getCapacity() + Temp.ANY.getCapacity(), Temp.HOT, "orders");

    final ShelvesManager manager = ShelvesManager.getInstance();
    Assert.assertTrue(manager.removeOrder(orders.get(0)));

    final Order order = new Order(String.valueOf(100), "newOrder-100", Temp.HOT, 100, 0.5f);

    Optional<Shelve> shelve = manager.addOrder(order);

    Assert.assertTrue(shelve.isPresent());
    Assert.assertEquals(Temp.HOT.getShelfName(), shelve.get().getName());
    Assert.assertEquals(Temp.HOT, shelve.get().getTemperature());
  }

  @Test
  public void testAddOrderTryToMoveOrdersToTheRightShelveFirstIfPossible() {
    // Fill all the shelves
    final List<Order> ordersHot = createAndAddRandomOrders(Temp.HOT.getCapacity(), Temp.HOT, "ordersHot");
    createAndAddRandomOrders(Temp.COLD.getCapacity(), Temp.COLD, "ordersCold");
    createAndAddRandomOrders(Temp.FROZEN.getCapacity(), Temp.FROZEN, "ordersFrozen");

    // Add more...Will go to Overflow shelf
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.HOT, "ordersOverflowHot");
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.COLD, "ordersOverflowCold");
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.FROZEN, "ordersOverflowFrozen");

    // All shelves are fill now, including overflow shelf.
    final ShelvesManager manager = ShelvesManager.getInstance();
    Assert.assertTrue(manager.removeOrder(ordersHot.get(0)));

    // Add new cold order, cold shelf is full so it should go to overflow after moving one Hot order to Hot shelf
    final Order order = new Order(String.valueOf(2000), "newOrderCold-100", Temp.COLD, 100, 0.5f);
    Optional<Shelve> shelve = manager.addOrder(order);

    Assert.assertTrue(shelve.isPresent());
    Assert.assertEquals(Temp.ANY.getShelfName(), shelve.get().getName());
    Assert.assertEquals(Temp.ANY, shelve.get().getTemperature());
  }

  @Test
  public void testAddOrderForceRemoveOrderIfNoOtherOption() {
    // Fill all the shelves
    final List<Order> ordersHot = createAndAddRandomOrders(Temp.HOT.getCapacity(), Temp.HOT, "forceHot");
    createAndAddRandomOrders(Temp.COLD.getCapacity(), Temp.COLD, "forceCold");
    createAndAddRandomOrders(Temp.FROZEN.getCapacity(), Temp.FROZEN, "forceFrozen");

    // Add more...Will go to Overflow shelf
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.HOT, "forceOverflowHot");
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.COLD, "forceOverflowCold");
    createAndAddRandomOrders(Temp.ANY.getCapacity() / 2, Temp.FROZEN, "forceOverflowFrozen");

    // All shelves are fill now, including overflow shelf.
    final ShelvesManager manager = ShelvesManager.getInstance();

    // Add new hot order which will replace the oldest order
    Collections.sort(ordersHot, Comparator.comparingLong(Order::getCreateTimeStamp));
    final Order oldest = ordersHot.get(0);

    final Order order = new Order(String.valueOf(2000), "force-1", Temp.HOT, 100, 0.5f);
    Optional<Shelve> shelve = manager.addOrder(order);

    Assert.assertTrue(shelve.isPresent());
    Assert.assertEquals(Temp.ANY.getShelfName(), shelve.get().getName());
    Assert.assertEquals(Temp.ANY, shelve.get().getTemperature());
    Assert.assertTrue(shelve.get().hasOrder(order));
    Assert.assertFalse(shelve.get().hasOrder(oldest));
  }

  private List<Order> createAndAddRandomOrders(int count, Temp temp, String idPrefix) {
    final List<Order> orders = new ArrayList<>();

    for (int i = 1; i <= count; i++) {
      final Order order = new Order(idPrefix + " - " + String.valueOf(i), "testOrder-" + i, temp, i * 100, 0.5f);
      final ShelvesManager manager = ShelvesManager.getInstance();
      manager.addOrder(order);
      orders.add(order);
    }

    return orders;
  }
}
