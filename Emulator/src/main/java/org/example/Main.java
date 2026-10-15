package org.example;

public class Main {
  public static void main(String[] args) {
    RM realMachine = RM.getInstance();
    VM vm = realMachine.getMenu();
    vm.loadDataToMemory();

    for (String command : vm.getMyCommands()) {
      System.out.println(command);
    }
  }
}
