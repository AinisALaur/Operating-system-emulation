package org.example;

import java.util.Scanner;

public class VM {
  private boolean on = true;

  public boolean isOn() {
    return on;
  }

  public void setOn(boolean on) {
    this.on = on;
  }

  public void loadDataToMemory() {
    RM.getInstance().loadUserData();
  }

  public boolean test() {
    int pi = RM.getInstance().getPI() - '0';
    int si = RM.getInstance().getSI() - '0';
    int ti = Mapper.toInt("" + RM.getInstance().getTI());
    return ((pi + si) > 0 || ti == 0);
  }

  public void decrementTI() {
    RM realMachine = RM.getInstance();
    int ti = Mapper.toInt("" + realMachine.getTI());
    realMachine.setTI(Character.toUpperCase(Character.forDigit(Math.max(0, ti - 1), 16)));
  }

  private void incrementIP() {
    String ipValue = new String(RM.getInstance().getIP());
    if (ipValue.isBlank()) {
      return;
    }

    int wordIndex;
    try {
      wordIndex = Mapper.toInt(ipValue);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid IP value: " + ipValue, e);
    }

    int newWordIndex = wordIndex + 1 >= Config.maxWordIndex ? 0 : wordIndex + 1;
    RM.getInstance().setIP(String.format("%02X", newWordIndex).toCharArray());
  }

  public String getCommand() {
    String ipValue = new String(RM.getInstance().getIP());
    if (ipValue.isBlank()) {
      return "";
    }

    int pointer;
    try {
      pointer = Mapper.toInt(ipValue) * Config.ram_word_size;
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid IP value: " + ipValue, e);
    }

    String command = "";
    if (pointer >= 0 && pointer <= Config.VM_ram_size - Config.ram_word_size) {
      int realPointer = RM.getInstance().getVmPointer(pointer);

      String word = RAM.getInstance().readWordAt(realPointer);
      if (!word.trim().isEmpty()) {
        command = word;
      } else {
        incrementIP();
      }

    } else {
      throw new IndexOutOfBoundsException(
          "IP value out of bounds: " + ipValue + ", VM RAM size: " + Config.VM_ram_size);
    }
    return command;
  }

  private void printHelper(int[] blocks) {
    System.out.println("-------------------------");
    for (int block : blocks) {
      for (int i = 0; i < Config.block_size; ++i) {
        int pointer = block * Config.block_size * Config.ram_word_size + i * Config.ram_word_size;
        int realPointer = RM.getInstance().getVmPointer(pointer);
        String word = RAM.getInstance().readWordAt(realPointer);
        if (!word.trim().isEmpty()) {
          System.out.println(
              Mapper.formatDisplayHex(block) + " " + Mapper.formatDisplayHex(i) + " " + word);
        }
      }
    }
    System.out.println("-------------------------");
  }

  // Blocks [0; 3]
  private void printDS() {
    printHelper(new int[] {0, 1, 2, 3});
  }

  // Block 4
  private void printParameters() {
    printHelper(new int[] {4});
  }

  // Blocks [5; 15]
  private void printCS() {
    printHelper(new int[] {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
  }

  private void printPTR() {
    int block = Mapper.toInt(new String(RM.getInstance().getPTR()));
    System.out.println("-------------------------");
    for (int i = 0; i < Config.block_size; ++i) {
      int pointer = block * Config.block_size * Config.ram_word_size + i * Config.ram_word_size;
      System.out.println(
          Mapper.formatDisplayHex(block)
              + " "
              + Mapper.formatDisplayHex(i)
              + " "
              + RAM.getInstance().readWordAt(pointer));
    }
    System.out.println("-------------------------");
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

  public void getMenu() {
    String[] mainOptions = {
      "1. Show data segment",
      "2. Show parameters",
      "3. Show code segment",
      "4. Show PTR",
      "5. Show all RM registers",
      "6. Show RM memory",
      "7. Exec next command",
      "8. Turn off VM"
    };
    while (true) {
      String ip = new String(RM.getInstance().getIP());
      System.out.println("Next command: " + ip + " " + getCommand());
      int userOption = getUserOption(mainOptions);
      switch (userOption) {
        case 1:
          printDS();
          break;
        case 2:
          printParameters();
          break;
        case 3:
          printCS();
          break;
        case 4:
          printPTR();
          break;
        case 5:
          RM.getInstance().printAllCPURegisters();
          break;
        case 6:
          RM.getInstance().printMemory();
          break;
        case 7:
          return;
        case 8:
          RM.getInstance().turnOffVM();
          return;
      }
    }
  }

  public void exec() {

    RM realMachine = RM.getInstance();

    String command = getCommand();

    if (command.isEmpty()) {
      return;
    }

    if (command.length() != 4) {
      realMachine.setPI('2');
      return;
    }

    switch (command) {
      case String op when op.startsWith("LD") -> {
        realMachine.ld(op.charAt(2), op.charAt(3));
        incrementIP();
      }
      case String op when op.startsWith("ST") -> {
        realMachine.st(op.charAt(2), op.charAt(3));
        incrementIP();
      }
      case "SWAP" -> {
        realMachine.swap();
        incrementIP();
      }
      case "ADDX" -> {
        realMachine.addX();
        incrementIP();
      }
      case "SUBX" -> {
        realMachine.subX();
        incrementIP();
      }
      case "CMPX" -> {
        realMachine.cmpX();
        incrementIP();
      }
      case String op when op.startsWith("JP") -> realMachine.jp(op.charAt(2), op.charAt(3));
      case String op when op.startsWith("JE") -> realMachine.je(op.charAt(2), op.charAt(3));
      case String op when op.startsWith("JN") -> realMachine.jn(op.charAt(2), op.charAt(3));
      case String op when op.startsWith("JA") -> realMachine.ja(op.charAt(2), op.charAt(3));
      case String op when op.startsWith("JB") -> realMachine.jb(op.charAt(2), op.charAt(3));
      case "GETD" -> {
        realMachine.getD();
        incrementIP();
      }
      case String op when op.charAt(0) == 'P' -> {
        realMachine.p(op.charAt(1), op.charAt(2), op.charAt(3));
        incrementIP();
      }
      case String op when op.startsWith("GON") -> {
        realMachine.gon(op.charAt(3));
        incrementIP();
      }
      case "HALT" -> realMachine.halt();
      default -> realMachine.setPI('2');
    }
    decrementTI();
  }
}
