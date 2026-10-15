package org.example;

import java.util.ArrayList;
import java.util.Scanner;

public class RM {
  String HDD = Config.outer_disk_file_name;
  RAM ram = new RAM();
  CPU cpu = new CPU(ram, HDD);
  private static RM instance = null;

  private RM() {}

  public static RM getInstance() {
    if (instance == null) {
      instance = new RM();
    }
    return instance;
  }

  private int getUserOption(String[] options) {
    Scanner scanner = new Scanner(System.in);
    for (String option : options) {
      System.out.println(option);
    }
    System.out.print("Enter your option: ");
    return scanner.nextInt();
  }

  public boolean test() {
    if (cpu.getSI() != 3) return true;
    return false;
  }

  // TODO: add guards
  public void getMenu() {
    String[] mainOptions = {"1. Choose file", "2. Quit"};
    Scanner scanner = new Scanner(System.in);

    int userOption = getUserOption(mainOptions);
    switch (userOption) {
      case 1:
        System.out.print("Enter file name: ");
        String fileName = scanner.nextLine();
        cpu.setFileName(fileName);
        ArrayList<String> res = cpu.loadDataToRam();

        //        for (String r : res) {
        //          System.out.println(r);
        //        }

        break;
      case 2:
        cpu.halt();
        break;
    }
  }
}
