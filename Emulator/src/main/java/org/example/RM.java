package org.example;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class RM {
  private static String HDD = Config.outer_disk_file_name;
  private RAM ram = RAM.getInstance();
  private CPU cpu = CPU.getInstance();
  private ChannelDevice channelDevice = ChannelDevice.getInstance();
  private boolean on = true;
  private VM currentVM = null;
  private static RM instance = null;
  private boolean fileFound = false;
  private static ArrayList<Integer> availableBlocks = null;
  private final ArrayList<Integer> blocksInUse = new ArrayList<>();

  private RM() {}

  public static RM getInstance() {
    if (instance == null) {
      instance = new RM();
      availableBlocks = null;
      inicializeRandomQueue();
    }
    return instance;
  }

  // Creates an array of [0; 50] and shuffles randomly, this is used for allocating 17 blocks for a
  // VM
  private static void inicializeRandomQueue() {
    availableBlocks = new ArrayList<>();
    for (int i = 0; i < Config.user_block_amount; ++i) {
      availableBlocks.add(i);
    }

    Random random = new Random();
    for (int i = availableBlocks.size() - 1; i > 0; --i) {
      int j = random.nextInt(i + 1);
      int temp = availableBlocks.get(i);
      availableBlocks.set(i, availableBlocks.get(j));
      availableBlocks.set(j, temp);
    }
  }

  // returns the next available block in HEX
  public String getNextAvailableBlock() {
    if (availableBlocks.isEmpty()) {
      throw new IllegalStateException("No available memory blocks left");
    }

    int block = availableBlocks.removeLast();
    blocksInUse.add(block);
    return Mapper.toHex(block)
        .substring(2, 4); // toHex returns a String of 4 chars, but PTR only has 2 bytes
  }

  private void releaseVmBlocks() {
    availableBlocks.addAll(blocksInUse);
    blocksInUse.clear();
  }

  public VM getCurrentVM() {
    return this.currentVM;
  }

  // This will have more logic when multiple vm's will be running
  public void turnOffVM() {
    if (currentVM == null) {
      return;
    }

    releaseVmBlocks();
    currentVM.setOn(false);
    fileFound = false;
    currentVM = null;
  }

  public void setFileFoundState() {
    fileFound = true;
  }

  public static String geHDD() {
    return HDD;
  }

  // Helper function to get user input
  private int getUserOption(String[] options, Scanner scanner) {
    for (String option : options) {
      System.out.println(option);
    }
    while (true) {
      System.out.print("Enter your option: ");
      if (scanner.hasNextInt()) {
        int choice = scanner.nextInt();
        if (choice >= 1 && choice <= options.length) {
          scanner.nextLine();
          return choice;
        }
      } else {
        scanner.next();
      }
      System.out.println("Invalid option, try again.");
    }
  }

  public String getMenu() {
    String[] mainOptions = {
      "1. Choose file", "2. Change to " + (getSF()[2] == '1' ? "user" : "step") + " mode", "3. Quit"
    };
    Scanner scanner = new Scanner(System.in);
    String fileName = "";

    int userOption = getUserOption(mainOptions, scanner);
    switch (userOption) {
      case 1:
        System.out.print("Enter file name: ");
        fileName = scanner.nextLine();
        while (fileName.isBlank() || fileName.length() > Config.ram_word_size) {
          System.out.print(
              "File name must contain between 1 and "
                  + Config.ram_word_size
                  + " characters, try again: ");
          fileName = scanner.nextLine();
        }
        break;
      case 2:
        if (getSF()[2] == '1') {
          setSF(2, '0');
        } else {
          setSF(2, '1');
        }
        if (getSF()[2] == '1') {
          System.out.println("Switched to step mode");
        } else {
          System.out.println("Switched to user mode");
        }
        break;
      case 3:
        this.on = false;
        break;
    }

    return fileName;
  }

  // Cast vm's pointer to real location pointer in memory
  public int getVmPointer(int pointer) {
    return RAM.getInstance().getPointerFromPTR(pointer);
  }

  // Fills the vm's user memory
  public void loadUserData() {
    cpu.setPTR();
    ram.fillPtrTable();

    channelDevice.setST('2');
    channelDevice.setDT('1');
    channelDevice.setSB(String.format("%02X", Config.supervisor_first_block).toCharArray());
    channelDevice.setDB(
        String.format(
                "%02X", Config.code_segment_start / (Config.block_size * Config.ram_word_size))
            .toCharArray());
    channelDevice.xchg();
  }

  public void loadToSupervisor(String fileName) {
    fileFound = false;
    channelDevice.setFN(fileName.toCharArray());
    channelDevice.setST('3');
    channelDevice.setDT('2');
    channelDevice.setDB(String.format("%02X", Config.supervisor_first_block).toCharArray());
    channelDevice.xchg();
  }

  public VM createVM() {
    if (!fileFound) {
      return null;
    }
    VM vm = new VM();
    RM.getInstance().setIP(new char[] {'5', '0'});
    this.currentVM = vm;
    return vm;
  }

  public void clearRegisters() {
    cpu.clearRegisters();
  }

  public boolean isOn() {
    return on;
  }

  public char[] getIP() {
    return cpu.getIP();
  }

  public void setIP(char[] newIP) {
    cpu.setIP(newIP);
  }

  public char getPI() {
    return cpu.getPI();
  }

  public void setPI(char newPI) {
    cpu.setPI(newPI);
  }

  public char getSI() {
    return cpu.getSI();
  }

  public void setSI(char newSI) {
    cpu.setSI(newSI);
  }

  public char getTI() {
    return cpu.getTI();
  }

  public void setTI(char newTI) {
    cpu.setTI(newTI);
  }

  public char[] getPTR() {
    return cpu.getPTR();
  }

  public char[] getSF() {
    return cpu.getSF();
  }

  public void setSF(int idx, char value) {
    cpu.setSF(idx, value);
  }

  public void printAllCPURegisters() {
    String r0 = new String(cpu.getR0());
    String r1 = new String(cpu.getR1());
    String ip = new String(cpu.getIP());
    String sf = new String(cpu.getSF());
    String ptr = new String(cpu.getPTR());
    String mode = "" + cpu.getMODE();
    char pi = cpu.getPI();
    char si = cpu.getSI();
    char ti = cpu.getTI();

    System.out.println("-------------------------");
    System.out.println("R0: " + r0);
    System.out.println("R1: " + r1);
    System.out.println("IP: " + ip);
    System.out.println("SF: " + sf);
    System.out.println("PTR: " + ptr);
    System.out.println("MODE: " + mode);
    System.out.println("PI: " + pi);
    System.out.println("SI: " + si);
    System.out.println("TI: " + ti);
    System.out.println("-------------------------");
  }

  public void printMemory() {
    ram.printMemory();
  }

  public void ld(char x, char y) {
    int virtualBlock = Character.digit(x, 16);
    int virtualWord = Character.digit(y, 16);
    if (virtualBlock < 0 || virtualWord < 0 || virtualWord >= Config.block_size) {
      setPI('1');
      return;
    }

    int virtualPointer =
        virtualBlock * Config.block_size * Config.ram_word_size
            + virtualWord * Config.ram_word_size;
    int realPointer = getVmPointer(virtualPointer);
    cpu.setR0(ram.readWordAt(realPointer).toCharArray());
  }

  public void st(char x, char y) {
    int virtualBlock = Character.digit(x, 16);
    int virtualWord = Character.digit(y, 16);
    if (virtualBlock < 0 || virtualWord < 0 || virtualWord >= Config.block_size) {
      setPI('1');
      return;
    }

    int virtualPointer =
        virtualBlock * Config.block_size * Config.ram_word_size
            + virtualWord * Config.ram_word_size;
    int realPointer = getVmPointer(virtualPointer);
    ram.writeWordAt(realPointer, new String(cpu.getR0()));
  }

  public void swap() {
    char[] temp = cpu.getR0();
    cpu.setR0(cpu.getR1());
    cpu.setR1(temp);
  }

  private int[] readRegisters() {
    try {
      return new int[] {
        Mapper.toInt(new String(cpu.getR0())), Mapper.toInt(new String(cpu.getR1()))
      };
    } catch (IllegalArgumentException e) {
      setPI('3');
      return null;
    }
  }

  private void arithmetics(boolean add) {
    int[] registers = readRegisters();
    if (registers == null) {
      return;
    }

    int result = add ? registers[0] + registers[1] : registers[0] - registers[1];
    boolean overflow =
        result > Config.max_number_in_register || result < Config.min_number_in_register;
    if (overflow) {
      int range = Config.max_number_in_register - Config.min_number_in_register + 1;
      result =
          Math.floorMod(result - Config.min_number_in_register, range)
              + Config.min_number_in_register;
    }

    cpu.setSF(0, result == 0 ? '1' : '0');
    cpu.setSF(1, result < 0 ? '1' : '0');
    cpu.setSF(3, overflow ? '1' : '0');
    cpu.setR0(Mapper.toHex(result).toCharArray());
  }

  private boolean isHexDigit(char value) {
    return Character.digit(value, 16) >= 0;
  }

  private boolean jumpIsInRange(char x, char y) {
    int dx = Character.digit(x, 16);
    int dy = Character.digit(y, 16);
    if (dx < 0 || dy < 0 || dy >= Config.block_size) {
      setPI('1');
      return false;
    }
    return true;
  }

  public void addX() {
    arithmetics(true);
  }

  public void subX() {
    arithmetics(false);
  }

  public void cmpX() {
    int[] registers = readRegisters();
    if (registers == null) {
      return;
    }

    cpu.setSF(0, registers[0] == registers[1] ? '1' : '0');
    cpu.setSF(1, registers[0] < registers[1] ? '1' : '0');
  }

  public void jp(char x, char y) {
    if (!jumpIsInRange(x, y)) {
      return;
    }
    cpu.setIP(new char[] {x, y});
  }

  public void je(char x, char y) {
    if (!jumpIsInRange(x, y)) {
      return;
    }
    if (cpu.getSF()[0] == '1') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void jn(char x, char y) {
    if (!jumpIsInRange(x, y)) {
      return;
    }
    if (cpu.getSF()[0] == '0') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void jg(char x, char y) {
    if (!jumpIsInRange(x, y)) {
      return;
    }

    if (cpu.getSF()[0] == '0' && cpu.getSF()[1] == cpu.getSF()[3]) {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void jl(char x, char y) {
    if (!jumpIsInRange(x, y)) {
      return;
    }

    if (cpu.getSF()[0] != cpu.getSF()[3]) {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void getD() {
    cpu.setSI('1');
  }

  public void p(char x, char y, char z) {
    if (!isHexDigit(x) || !isHexDigit(y) || !isHexDigit(z) || Character.digit(z, 16) > 3) {
      setPI('1');
      return;
    }

    int length;
    try {
      length = Mapper.toInt(new String(cpu.getR1()));
    } catch (IllegalArgumentException e) {
      setPI('3');
      return;
    }
    if (length < 0 || length > 0xFF) {
      setPI('3');
      return;
    }

    InterruptHandler.getInstance().setX(x);
    InterruptHandler.getInstance().setY(y);
    InterruptHandler.getInstance().setZ(z);
    InterruptHandler.getInstance().setB(cpu.getR1());
    cpu.setSI('2');
  }

  public void gon(char x) {
    if (!isHexDigit(x)) {
      setPI('1');
      return;
    }

    InterruptHandler.getInstance().setX(x);
    cpu.setSI('4');
  }

  public void halt() {
    cpu.setSI('3');
  }
}
