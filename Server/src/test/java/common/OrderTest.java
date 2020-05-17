package common;

import java.util.concurrent.TimeUnit;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

public class OrderTest {

  @Test
  public void testOrderEquals() {
    EqualsVerifier.forClass(Order.class)
        // EqualsVerifier uses final to decide is the field is immutable or not
        // but in our case we can not add final since we are using Gson to serialize/deserialize we have to have default empty constructor
        .suppress(Warning.NONFINAL_FIELDS)
        .withIgnoredFields("orderLife", "ageInSeconds", "createTimeStamp") // these fields are mutable and depends on the order itself
        .verify();
  }

  @Test
  public void testToStringReturnsCorrectJson() {
    final Order order = new Order("1", "testOrder", Temp.COLD, 1, 0.5f);
    final String orderStr = order.toString();

    // Str order {"id":"1","name":"testOrder","temp":"cold","shelfLife":1,"orderLife":0.0,"decayRate":0.5,"createTimeStamp":1589735400934,"ageInSeconds":0}
    // I am intentionally not using .equals because createTimeStamp will be diff
    Assert.assertTrue(orderStr.contains("\"name\":\"testOrder\""));
    Assert.assertTrue(orderStr.contains("\"id\":\"1\""));
    Assert.assertTrue(orderStr.contains("\"temp\":\"cold\""));
    Assert.assertTrue(orderStr.contains("\"decayRate\":0.5"));
    Assert.assertTrue(orderStr.contains("\"shelfLife\":1"));
    Assert.assertTrue(orderStr.contains("\"orderLife\"")); // We don't care about the value
    Assert.assertTrue(orderStr.contains("\"createTimeStamp\"")); // We don't care about the value
    Assert.assertTrue(orderStr.contains("\"ageInSeconds\"")); // We don't care about the value
  }


  @Test
  public void testOrderCreateFromJson() {
    final String strOrder = "{\n" +
        "    \"id\": \"972aa5b8-5d83-4d5e-8cf3-8a1a1437b18a\",\n" +
        "    \"name\": \"Chocolate Gelato\",\n" +
        "    \"temp\": \"frozen\",\n" +
        "    \"shelfLife\": 300,\n" +
        "    \"decayRate\": 0.61\n" +
        "  }";

    final Order order = Order.createFromJson(strOrder);

    Assert.assertEquals("972aa5b8-5d83-4d5e-8cf3-8a1a1437b18a", order.getId());
    Assert.assertEquals("Chocolate Gelato", order.getName());
    Assert.assertEquals(Temp.FROZEN, order.getTemp());
    Assert.assertEquals(300, order.getShelfLife());
    Assert.assertEquals(0.61f, order.getDecayRate(), 0);
  }

  @Test
  public void testUpdateOrderLifeUpdatesOrderAgeAsWell() {
    final long now = System.currentTimeMillis();
    final Order order = new Order("1", "testOrder", Temp.COLD, 1, 0.5f);
    final float life = order.updateAndGetLife(() -> now);

    final float expectedAge = TimeUnit.MILLISECONDS.toSeconds(now - order.getCreateTimeStamp());
    final float expectedLife = ((1 - 0.5f * expectedAge * Temp.COLD.getDecayModifier()) / 1);

    Assert.assertEquals(expectedAge, order.getAgeInSeconds(), 0);
    Assert.assertEquals(expectedLife, order.getOrderLife(), 0);

  }
}
