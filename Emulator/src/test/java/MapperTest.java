import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.Mapper;
import org.junit.jupiter.api.Test;

public class MapperTest {
  @Test
  void testMapperToInt() {
    assertTrue(Mapper.toInt("0") == 0);
    assertTrue(Mapper.toInt("1") == 1);
    assertTrue(Mapper.toInt("2") == 2);
    assertTrue(Mapper.toInt("3") == 3);
    assertTrue(Mapper.toInt("4") == 4);
    assertTrue(Mapper.toInt("5") == 5);
    assertTrue(Mapper.toInt("6") == 6);
    assertTrue(Mapper.toInt("7") == 7);
    assertTrue(Mapper.toInt("8") == 8);
    assertTrue(Mapper.toInt("9") == 9);
    assertTrue(Mapper.toInt("A") == 10);
    assertTrue(Mapper.toInt("B") == 11);
    assertTrue(Mapper.toInt("C") == 12);
    assertTrue(Mapper.toInt("D") == 13);
    assertTrue(Mapper.toInt("E") == 14);
    assertTrue(Mapper.toInt("F") == 15);
    assertTrue(Mapper.toInt("FF") == 255);
    assertTrue(Mapper.toInt("43") == 67);
  }

  @Test
  void testMapperToHex() {
    assertTrue(Mapper.toHex(0).equals("0000"));
    assertTrue(Mapper.toHex(1).equals("0001"));
    assertTrue(Mapper.toHex(2).equals("0002"));
    assertTrue(Mapper.toHex(3).equals("0003"));
    assertTrue(Mapper.toHex(4).equals("0004"));
    assertTrue(Mapper.toHex(5).equals("0005"));
    assertTrue(Mapper.toHex(6).equals("0006"));
    assertTrue(Mapper.toHex(7).equals("0007"));
    assertTrue(Mapper.toHex(8).equals("0008"));
    assertTrue(Mapper.toHex(9).equals("0009"));
    assertTrue(Mapper.toHex(10).equals("000A"));
    assertTrue(Mapper.toHex(11).equals("000B"));
    assertTrue(Mapper.toHex(12).equals("000C"));
    assertTrue(Mapper.toHex(13).equals("000D"));
    assertTrue(Mapper.toHex(14).equals("000E"));
    assertTrue(Mapper.toHex(15).equals("000F"));
    assertTrue(Mapper.toHex(255).equals("00FF"));
    assertTrue(Mapper.toHex(67).equals("0043"));
  }

  @Test
  void testMapperRejectsValuesOutsideItsDomain() {
    assertThrows(IllegalArgumentException.class, () -> Mapper.toInt(null));
    assertThrows(IllegalArgumentException.class, () -> Mapper.toInt(""));
    assertThrows(IllegalArgumentException.class, () -> Mapper.toInt("G1"));
  }
}
