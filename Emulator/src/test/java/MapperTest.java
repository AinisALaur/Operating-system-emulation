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
    assertTrue(Mapper.toHex(0).equals("0"));
    assertTrue(Mapper.toHex(1).equals("1"));
    assertTrue(Mapper.toHex(2).equals("2"));
    assertTrue(Mapper.toHex(3).equals("3"));
    assertTrue(Mapper.toHex(4).equals("4"));
    assertTrue(Mapper.toHex(5).equals("5"));
    assertTrue(Mapper.toHex(6).equals("6"));
    assertTrue(Mapper.toHex(7).equals("7"));
    assertTrue(Mapper.toHex(8).equals("8"));
    assertTrue(Mapper.toHex(9).equals("9"));
    assertTrue(Mapper.toHex(10).equals("A"));
    assertTrue(Mapper.toHex(11).equals("B"));
    assertTrue(Mapper.toHex(12).equals("C"));
    assertTrue(Mapper.toHex(13).equals("D"));
    assertTrue(Mapper.toHex(14).equals("E"));
    assertTrue(Mapper.toHex(15).equals("F"));
    assertTrue(Mapper.toHex(255).equals("FF"));
    assertTrue(Mapper.toHex(67).equals("43"));
  }
}
