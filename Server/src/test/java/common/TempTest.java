package common;

import org.junit.Assert;
import org.junit.Test;

public class TempTest {
  @Test
  public void testTempHasTheRightDecayModifierByDefault() {
    Assert.assertEquals(1, Temp.COLD.getDecayModifier());
    Assert.assertEquals(1, Temp.HOT.getDecayModifier());
    Assert.assertEquals(1, Temp.FROZEN.getDecayModifier());
    Assert.assertEquals(2, Temp.ANY.getDecayModifier());
  }

  @Test
  public void testTempHasTheRightCapacityByDefault() {
    Assert.assertEquals(10, Temp.COLD.getCapacity());
    Assert.assertEquals(10, Temp.HOT.getCapacity());
    Assert.assertEquals(10, Temp.FROZEN.getCapacity());
    Assert.assertEquals(15, Temp.ANY.getCapacity());
  }
}
