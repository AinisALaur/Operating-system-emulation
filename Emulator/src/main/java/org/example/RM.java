package org.example;

import java.util.Scanner;

public class RM {
  private static String HDD = Config.outer_disk_file_name;
  private RAM ram = RAM.getInstance();
  private CPU cpu = CPU.getInstance();
  private ChannelDevice channelDevice = ChannelDevice.getInstance();
  private boolean on = true;
  private int vmNum = 0;
  private static RM instance = null;
  private boolean fileFound = false;

  private RM() {}

  public static RM getInstance() {
    if (instance == null) {
      instance = new RM();
    }
    return instance;
  }

  public void setFileFoundState() {
    fileFound = !fileFound;
  }

  public static String geHDD() {
    return HDD;
  }

  // Helper function to get user input
  private int getUserOption(String[] options) {
    Scanner scanner = new Scanner(System.in);
    for (String option : options) {
      System.out.println(option);
    }
    while (true) {
      System.out.print("Enter your option: ");
      if (scanner.hasNextInt()) {
        int choice = scanner.nextInt();
        if (choice >= 1 && choice <= options.length) {
          return choice;
        }
      } else {
        scanner.next();
      }
      System.out.println("Invalid option, try again.");
    }
  }

  // Cast vm's pointer to real location pointer in memory
  public int getVmPointer(int pointer, int vm) {
    return RAM.getInstance().getPointerFromPTR(pointer, vm);
  }

  // Returns the word at a specific block and word
  public String getWord(int block, int word) {
    return ram.getWord(block, word);
  }

  public String getMenu() {
    String[] mainOptions = {"1. Choose file", "2. Quit"};
    Scanner scanner = new Scanner(System.in);
    String fileName = "";

    int userOption = getUserOption(mainOptions);
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
        this.on = false;
        break;
    }

    return fileName;
  }

  // Fills the vm's user memory
  public void loadUserData(int vm) {
    ram.loadData(vm);
  }

  public void setFileName(String fileName) {
    if (fileName.isEmpty()) {
      return;
    }

    channelDevice.setFN(fileName);
  }

  public void loadToSupervisor() {
    ram.loadToSupervisor();
  }

  public VM createVM() {
    if (!fileFound) {
      cpu.setSI('1');
      return null;
    }
    cpu.setSI('0');
    int currentVm = vmNum;
    vmNum = (vmNum + 1) % 3;
    return new VM(currentVm);
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

  public void printAllCPURegisters() {
    String r0 = "" + cpu.getR0()[0] + cpu.getR0()[1] + cpu.getR0()[2] + cpu.getR0()[3];
    String r1 = "" + cpu.getR1()[0] + cpu.getR1()[1] + cpu.getR1()[2] + cpu.getR1()[3];
    String ip = "" + cpu.getIP()[0] + cpu.getIP()[1];
    String sf = "" + cpu.getSF()[0] + cpu.getSF()[1] + cpu.getSF()[2] + cpu.getSF()[3];
    String ptr = "" + cpu.getPTR()[0] + cpu.getPTR()[1];
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

  // This will have more logic when multiple vm's will be running
  public void turnOffVM() {
    setFileFoundState();
  }

  public void ld(char x, char y) {
    cpu.setR0(getWord(Character.digit(x, 16), Character.digit(y, 16)).toCharArray());
  }

  public void st(char x, char y) {}

  public void swap() {
    char[] temp = cpu.getR0();
    cpu.setR0(cpu.getR1());
    cpu.setR1(temp);
  }

  public void addX() {
    String R0 = "" + cpu.getR0()[0] + cpu.getR0()[1] + cpu.getR0()[2] + cpu.getR0()[3];
    String R1 = "" + cpu.getR1()[0] + cpu.getR1()[1] + cpu.getR1()[2] + cpu.getR1()[3];
    int result = Mapper.toInt(R0) + Mapper.toInt(R1);
    if (result == 0) {
      cpu.setSF(0, '1');
    } else {
      cpu.setSF(0, '0');
    }
    if (result >= 0) {
      cpu.setSF(1, '0');
    } else {
      cpu.setSF(1, '1');
    }
    if (result > Config.max_number_in_register || result < Config.min_number_in_register) {
      cpu.setSF(3, '1');
    } else {
      cpu.setSF(3, '0');
    }
    cpu.setR0(Mapper.toHex(result).toCharArray());
  }

  public void subX() {
    String R0 = "" + cpu.getR0()[0] + cpu.getR0()[1] + cpu.getR0()[2] + cpu.getR0()[3];
    String R1 = "" + cpu.getR1()[0] + cpu.getR1()[1] + cpu.getR1()[2] + cpu.getR1()[3];
    int result = Mapper.toInt(R0) - Mapper.toInt(R1);
    if (result == 0) {
      cpu.setSF(0, '1'); // zero-flag
    } else {
      cpu.setSF(0, '0');
    }
    if (result >= 0) {
      cpu.setSF(1, '0'); // sign-flag
    } else {
      cpu.setSF(1, '1');
    }
    if (result > Config.max_number_in_register || result < Config.min_number_in_register) {
      cpu.setSF(3, '1'); // overflow-flag
    } else {
      cpu.setSF(3, '0');
    }
    cpu.setR0(Mapper.toHex(result).toCharArray());
  }

  public void cmpX() {
    int R0 = Mapper.toInt("" + cpu.getR0()[0] + cpu.getR0()[1] + cpu.getR0()[2] + cpu.getR0()[3]);
    int R1 = Mapper.toInt("" + cpu.getR0()[0] + cpu.getR0()[1] + cpu.getR0()[2] + cpu.getR0()[3]);

    if (R0 == R1) {
      cpu.setSF(0, '1');
      cpu.setSF(1, '0');
    }

    if (R0 < R1) {
      cpu.setSF(0, '0');
      cpu.setSF(1, '1');
    }

    if (R0 > R1) {
      cpu.setSF(0, '0');
      cpu.setSF(1, '0');
    }
  }

  public void jp(char x, char y) {
    cpu.setIP(new char[] {x, y});
  }

  public void je(char x, char y) {
    if (cpu.getSF()[0] == '1') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void jn(char x, char y) {
    if (cpu.getSF()[0] == '0') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void ja(char x, char y) {
    if (cpu.getSF()[0] == '0' && cpu.getSF()[1] == '0') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void jb(char x, char y) {
    if (cpu.getSF()[0] == '0' && cpu.getSF()[1] == '1') {
      cpu.setIP(new char[] {x, y});
    }
  }

  public void getD() {
  }

  public void p(char x, char y, char z) {}

  public void gon(char x) {}

  public void halt() {
    cpu.setSI('3');
  }
}
