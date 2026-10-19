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

    assertTrue(vm.getNextCommand().isEmpty());
  }

  @Test
  void testLoadDataToMemoryWithEmptySupervisorLoadsNothing() {
    VM vm = new VM(0);
    vm.loadDataToMemory();

    assertTrue(vm.getNextCommand().isEmpty());
  }

  @Test
  void testGetMyCommandsReturnsLoadedCommandsInCodeSegment() {
    loadFileToSupervisor("abcd");

    VM vm = new VM(0);
    RM rm = RM.getInstance();
    vm.loadDataToMemory();
    rm.setIP(new char[] {'5', '0'});
    assertEquals("5 0 SUBX", vm.getNextCommand());
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

  @Test
  void testMemoryRejectsOutOfBoundsAddresses() {
    RAM ram = RAM.getInstance();

    assertThrows(IndexOutOfBoundsException.class, () -> ram.getWord(-1, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getWord(Config.block_amount, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getWord(0, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getWord(0, Config.block_size));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getPointerFromPTR(-1, 0));
    assertThrows(
        IndexOutOfBoundsException.class, () -> ram.getPointerFromPTR(Config.VM_ram_size, 0));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getPointerFromPTR(0, -1));
    assertThrows(IndexOutOfBoundsException.class, () -> ram.getPointerFromPTR(0, Config.VM_amount));
  }

  @Test
  void testVmRejectsOutOfBoundsNumbers() {
    assertThrows(IllegalArgumentException.class, () -> new VM(-1));
    assertThrows(IllegalArgumentException.class, () -> new VM(Config.VM_amount));
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
    assertEquals("  ", new String(rm.getIP()));
    assertEquals('1', cpu.getMODE());
    assertEquals('F', rm.getTI());
  }

  @Test
  void testCpuRejectsOutOfBoundsRegisterValues() {
    CPU cpu = CPU.getInstance();

    assertThrows(IllegalArgumentException.class, () -> cpu.setMODE('2'));
    assertThrows(IllegalArgumentException.class, () -> cpu.setPI('4'));
    assertThrows(IllegalArgumentException.class, () -> cpu.setSI('/'));

    cpu.setIP(new char[] {'0'});
    assertEquals('1', cpu.getPI());
  }

  @Test
  void testVmTwoUsesItsOwnMappedBlocks() {
    loadFileToSupervisor("abcd");

    new VM(2).loadDataToMemory();

    assertEquals("SUBX", RM.getInstance().getWord(17, 0));
  }

  @Test
  void testChannelDeviceRejectsInvalidFileNameLengths() {
    ChannelDevice channelDevice = ChannelDevice.getInstance();

    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN(null));
    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN(""));
    assertThrows(IllegalArgumentException.class, () -> channelDevice.setFN("abcde"));

    channelDevice.setFN("a");
    assertEquals("a", channelDevice.getFN());
  }
}
