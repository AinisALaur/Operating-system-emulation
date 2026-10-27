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
      realMachine.setCurrentVM(vm);

      if (vm == null) {
        continue;
      }

      realMachine.setIP(new char[] {'5', '0'}); // Points to data segment by default
      vm.loadDataToMemory();

      while (vm.isOn()) {
        if (realMachine.getSF()[2] == '1') {
          vm.getMenu();
        }
        if (!vm.isOn()) {
          break;
        }
        vm.exec();
        if (vm.test()) {
          InterruptHandler.getInstance().handleInterrupt();
        }
      }
    }
  }
}
