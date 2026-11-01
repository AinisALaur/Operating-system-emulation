import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.example.CPU;
import org.example.ChannelDevice;
import org.example.Config;
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
    setStatic(RM.class, "availableBlocks", null);
    setStatic(RM.class, "HDD", TEST_HDD);
  }

  private static void setStatic(Class<?> clazz, String name, Object value) throws Exception {
    Field field = clazz.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static void loadFileToSupervisor(String fileName) {
    RM.getInstance().loadToSupervisor(fileName);
  }

  // Throws because PTR is empty
  @Test
  void testGetMyCommandsThrowsIllegalArgumentException() {
    VM vm = new VM();

    assertThrows(
        IllegalArgumentException.class,
        () -> {
          vm.getCommand();
        });
  }

  @Test
  void testLoadDataToMemoryWithEmptySupervisorLoadsNothing() {
    VM vm = new VM();
    vm.loadDataToMemory();

    assertTrue(vm.getCommand().isEmpty());
  }

  @Test
  void testGetMyCommandsReturnsLoadedCommandsInCodeSegment() {
    loadFileToSupervisor("abcd");

    VM vm = new VM();
    RM rm = RM.getInstance();
    vm.loadDataToMemory();
    rm.setIP(new char[] {'5', '0'});
    assertEquals("SUBX", vm.getCommand());
  }

  @Test
  void testLoadDataToMemoryPlacesCommandsInMappedRealBlock() {
    loadFileToSupervisor("abcd");

    new VM().loadDataToMemory();

    int codePointer = Config.code_segment_start;
    int realPointer = RAM.getInstance().getPointerFromPTR(codePointer);
    assertEquals("SUBX", RAM.getInstance().readWordAt(realPointer));
    assertEquals("MOVY", RAM.getInstance().readWordAt(realPointer + Config.ram_word_size));
    assertEquals("ADDZ", RAM.getInstance().readWordAt(realPointer + 2 * Config.ram_word_size));
  }

  @Test
  void testLoadDataToMemoryClearsSupervisorMemory() {
    loadFileToSupervisor("abcd");

    new VM().loadDataToMemory();

    for (int word = 0; word < 3; ++word) {
      int pointer =
          Config.supervisor_first_block * Config.block_size * Config.ram_word_size
              + word * Config.ram_word_size;
      assertEquals("    ", RAM.getInstance().readWordAt(pointer));
    }
  }

  @Test
  void testClearRegistersClearsCpuRegisters() {
    RM rm = RM.getInstance();
    CPU cpu = CPU.getInstance();
    rm.setPI('3');
    rm.setSI('3');
    rm.setIP(new char[] {'F', 'F'});
    cpu.setMODE('0');

    rm.clearRegisters();

    assertEquals('0', rm.getPI());
    assertEquals('0', rm.getSI());
    assertEquals("00", new String(rm.getIP()));
    assertEquals('0', cpu.getMODE());
    assertEquals('F', rm.getTI());
  }

  @Test
  void testDecrementTI() {
    VM vm = new VM();
    RM rm = RM.getInstance();

    vm.decrementTI();
    assertEquals('E', rm.getTI());

    rm.setTI('A');
    vm.decrementTI();
    assertEquals('9', rm.getTI());

    rm.setTI('0');
    vm.decrementTI();
    assertEquals('0', rm.getTI());
  }

  @Test
  void testCpuRejectsOutOfBoundsRegisterValues() {
    CPU cpu = CPU.getInstance();

    assertThrows(IllegalArgumentException.class, () -> cpu.setMODE('2'));
    assertThrows(IllegalArgumentException.class, () -> cpu.setPI('4'));
    assertThrows(IllegalArgumentException.class, () -> cpu.setSI('/'));
    assertThrows(IllegalArgumentException.class, () -> cpu.setIP(new char[] {'0'}));
    assertEquals('0', cpu.getPI());
  }

  @Test
  void testLoadDataToMemoryMapsCommandsThroughPtr() {
    loadFileToSupervisor("abcd");

    new VM().loadDataToMemory();

    int codePointer = Config.code_segment_start;
    int realPointer = RAM.getInstance().getPointerFromPTR(codePointer);
    assertEquals("SUBX", RAM.getInstance().readWordAt(realPointer));
  }

  @Test
  void testChannelDeviceRejectsInvalidFileNameLengths() {
    ChannelDevice channelDevice = ChannelDevice.getInstance();

    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN(null));
    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN(new char[0]));
    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN("abcde".toCharArray()));

    channelDevice.setFN("a".toCharArray());
    assertEquals("a", new String(channelDevice.getFN()).trim());
  }
}
