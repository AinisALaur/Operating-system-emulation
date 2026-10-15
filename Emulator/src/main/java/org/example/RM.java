package org.example;

import java.util.Scanner;

public class RM {
  private static String HDD = Config.outer_disk_file_name;
  private RAM ram = RAM.getInstance();
  private CPU cpu = CPU.getInstance();
  private ChannelDevice channelDevice = ChannelDevice.getInstance();
  private Boolean on = true;

  private static RM instance = null;

  private RM() {}

  public static RM getInstance() {
    if (instance == null) {
      instance = new RM();
    }
    return instance;
  }

  public static String geHDD() {
    return HDD;
  }

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

  public int getVmPointer(int pointer, int vm) {
    return RAM.getInstance().getPointerFromPTR(pointer, vm);
  }

  public String getWord(int block, int word) {
    return ram.getWord(block, word);
  }

  // TODO: add guards
  public VM getMenu() {
    String[] mainOptions = {"1. Choose file", "2. Quit"};
    Scanner scanner = new Scanner(System.in);

    int userOption = getUserOption(mainOptions);
    switch (userOption) {
      case 1:
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine();
        while (fileName.length() > Config.ram_word_size) {
          System.out.print(
              "File name's length must be <= " + Config.ram_word_size + " characters, try again: ");
          fileName = scanner.nextLine();
        }

        channelDevice.setFN(fileName);
        ram.loadToSupervisor();
        break;
      case 2:
        this.on = false;
        break;
    }

    return new VM(0);
  }

  public void loadUserData(int vm) {
    ram.loadData(vm);
  }
}
