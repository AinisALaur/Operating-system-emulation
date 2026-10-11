import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.example.*;
import org.junit.jupiter.api.Test;

public class RamTest {

  @Test
  void testExample() {
    RAM ram = new RAM();
    CPU cpu = new CPU(ram, Config.outer_disk_file_name);
    cpu.setFileName("abcd");

    ArrayList<String> result = ram.loadData("src/test/java/testHDD.txt", cpu);
    ArrayList<String> expected =
        new ArrayList<>(
            List.of("51 0 $RJG", "51 1 uzd1", "51 2 SUBX", "51 3 MOVY", "52 0 ADDZ", "52 1 $END"));
    assertTrue(result.equals(expected));
  }
}
