package org.example;

public class Main {
  public static void main(String[] args) {
    RM realMachine = RM.getInstance();

    while (realMachine.isOn()) {
      // currently user/step mode is configured for RM, not per VM, maybe it's fine
      String fileName = realMachine.getMenu();
      if (fileName.isEmpty()) {
        continue;
      }

      realMachine.loadToSupervisor(fileName);
      VM vm = realMachine.createVM();

      if (vm == null) {
        continue;
      }

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
