package org.example;

import java.util.Scanner;

public class VM {
  private int number;
  private boolean on = true;

  public VM(int num) {
    if (num < 0 || num >= Config.VM_amount) {
      throw new IllegalArgumentException(
          "VM number must be between 0 and " + (Config.VM_amount - 1) + ": " + num);
    }
    this.number = num;
  }

  public boolean isOn() {
    return on;
  }

  public void setOn(boolean on) {
    this.on = on;
  }

  public void loadDataToMemory() {
    RM.getInstance().loadUserData(this.number);
  }

  public boolean test() {
    int pi = RM.getInstance().getPI() - '0';
    int si = RM.getInstance().getSI() - '0';
    int ti = Mapper.toInt("" + RM.getInstance().getTI());
    return !((pi + si) > 0 || ti == 0);
  }

  public void decrementTI() {
    RM realMachine = RM.getInstance();
    int ti = Mapper.toInt("" + realMachine.getTI());
    realMachine.setTI(Character.toUpperCase(Character.forDigit(Math.max(0, ti - 1), 16)));
  }

  // maybe this comment is old now??
  // prints commands in the code segment, later should set its IP and move along it
  public String getCommand() {
    String ipValue = new String(RM.getInstance().getIP());
    if (ipValue.isBlank()) {
      return "";
    }

    int pointer;
    try {
      pointer = Mapper.toInt(ipValue) * Config.ram_word_size;
    } catch (IllegalArgumentException e) {
      RM.getInstance().setPI('3');
      return "";
    }

    String command = "";
    if (pointer >= 0 && pointer <= Config.VM_ram_size - Config.ram_word_size) {
      int realPointer = RM.getInstance().getVmPointer(pointer, this.number);

      int realBlock = realPointer / (Config.block_size * Config.ram_word_size);
      int realWord = realPointer % (Config.block_size * Config.ram_word_size) / 4;

      String word = RM.getInstance().getWord(realBlock, realWord);
      if (!word.trim().isEmpty()) {
        command = word;
      } else {
        incrementIP();
      }

    } else {
      RM.getInstance().setPI('1');
    }
    return command;
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
      RM.getInstance().setPI('3');
      return;
    }

    int maxWordIndex = Config.VM_ram_size / Config.ram_word_size;
    int newWordIndex = wordIndex + 1 >= maxWordIndex ? 0 : wordIndex + 1;
    RM.getInstance().setIP(String.format("%02X", newWordIndex).toCharArray());
  }

  private void printHelper(int[] blocks) {
    System.out.println("-------------------------");
    for (int block : blocks) {
      for (int i = 0; i < Config.block_size; ++i) {
        int pointer = block * Config.block_size * Config.ram_word_size + i * Config.ram_word_size;
        int realPointer = RM.getInstance().getVmPointer(pointer, this.number);
        int realBlock = realPointer / (Config.block_size * Config.ram_word_size);
        int realWord =
            realPointer % (Config.block_size * Config.ram_word_size) / Config.ram_word_size;
        String word = RM.getInstance().getWord(realBlock, realWord);
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

  // Blocks [5; 16]
  private void printCS() {
    printHelper(new int[] {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
  }

  private void printPTR() {
    int block = Mapper.toInt(new String(RM.getInstance().getPTR()));
    System.out.println("-------------------------");
    for (int i = 0; i < Config.block_size; ++i) {
      System.out.println(
          Mapper.formatDisplayHex(block)
              + " "
              + Mapper.formatDisplayHex(i)
              + " "
              + RM.getInstance().getWord(block, i));
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
      "6. Exec next command",
      "7. Turn off VM"
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
          return;
        case 7:
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
      case String op when op.startsWith("GON") -> {
        realMachine.gon(op.charAt(3));
        incrementIP();
      }
      case String op when op.charAt(0) == 'P' -> {
        realMachine.p(op.charAt(1), op.charAt(2), op.charAt(3));
        incrementIP();
      }
      case "HALT" -> realMachine.halt();
      default -> realMachine.setPI('2');
    }
    decrementTI();
  }
}
