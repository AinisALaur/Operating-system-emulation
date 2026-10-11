import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.example.*;
import org.junit.jupiter.api.Test;

public class RamTest {

  @Test
  void testExample() {
    RAM ram = new RAM();
    char[] mode = {1};
    ArrayList<String> result = ram.loadData("src/test/java/testHDD.txt", mode);
    ArrayList<String> expected = new ArrayList<>(List.of("51 0 ADDX", "51 1 SUBY", "51 2 MOVZ"));
    assertTrue(result.equals(expected));
  }
}
