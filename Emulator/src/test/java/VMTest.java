import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import org.example.CPU;
import org.example.ChannelDevice;
import org.example.RAM;
import org.example.RM;
import org.example.VM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VMTest {
  private static final String TEST_HDD = "src/test/java/testHDD.txt";

  // The machine is built from singletons, so reset them to get a clean memory for each test
  @BeforeEach
  void resetMachine() throws Exception {
    setStatic(RAM.class, "instance", null);
    setStatic(CPU.class, "instance", null);
    setStatic(ChannelDevice.class, "instance", null);
    setStatic(RM.class, "instance", null);
    setStatic(RM.class, "HDD", TEST_HDD);
  }

  private static void setStatic(Class<?> clazz, String name, Object value) throws Exception {
    Field field = clazz.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static void loadFileToSupervisor(String fileName) {
    RM.getInstance();
    ChannelDevice.getInstance().setFN(fileName);
    RAM.getInstance().loadToSupervisor();
  }

  @Test
  void testGetMyCommandsIsEmptyWhenNothingLoaded() {
    VM vm = new VM(0);

    assertTrue(vm.getMyCommands().isEmpty());
  }

  @Test
  void testLoadDataToMemoryWithEmptySupervisorLoadsNothing() {
    VM vm = new VM(0);
    vm.loadDataToMemory();

    assertTrue(vm.getMyCommands().isEmpty());
  }

  @Test
  void testGetMyCommandsReturnsLoadedCommandsInCodeSegment() {
    loadFileToSupervisor("abcd");

    VM vm = new VM(0);
    vm.loadDataToMemory();

    assertEquals(List.of("5 0 SUBX", "5 1 MOVY", "5 2 ADDZ"), vm.getMyCommands());
  }

  @Test
  void testLoadDataToMemoryPlacesCommandsInMappedRealBlock() {
    loadFileToSupervisor("abcd");

    new VM(0).loadDataToMemory();

    // VM 0 block 5 maps to real block 15 (every third block starting from 0)
    assertEquals("SUBX", RM.getInstance().getWord(15, 0));
    assertEquals("MOVY", RM.getInstance().getWord(15, 1));
    assertEquals("ADDZ", RM.getInstance().getWord(15, 2));
  }

  @Test
  void testLoadDataToMemoryClearsSupervisorMemory() {
    loadFileToSupervisor("abcd");

    new VM(0).loadDataToMemory();

    for (int word = 0; word < 3; ++word) {
      assertEquals("    ", RM.getInstance().getWord(51, word));
    }
  }
}
