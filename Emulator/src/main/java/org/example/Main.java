package org.example;

public class Main {
  public static void main(String[] args) {
    RM realMachine = RM.getInstance();

    while (realMachine.isOn()) {
      String fileName = realMachine.getMenu();
      if (fileName.isEmpty()) {
        continue;
      }

      realMachine.setFileName(fileName);
      realMachine.loadToSupervisor();
      VM vm = realMachine.createVM();
      if (vm == null) {
        continue;
      }

      realMachine.setIP(new char[] {'5', '0'}); // Points to data segment by default
      vm.loadDataToMemory();

      while (vm.test()) {
        String command = vm.getNextCommand();
        if (!command.isEmpty()) {
          System.out.println("Command: " + command);
        }
        vm.getMenu();
      }
      realMachine.turnOffVM();
    }
  }
}
