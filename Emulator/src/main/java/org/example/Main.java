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
      realMachine.createVM();

      if (realMachine.getCurrentVM() == null) {
        continue;
      }

      realMachine.getCurrentVM().loadDataToMemory();

      while (realMachine.getCurrentVM() != null && realMachine.getCurrentVM().isOn()) {
        if (realMachine.getSF()[2] == '1') {
          realMachine.getCurrentVM().getMenu();
        }
        if (realMachine.getCurrentVM() == null || !realMachine.getCurrentVM().isOn()) {
          break;
        }
        realMachine.getCurrentVM().exec();
        if (realMachine.getCurrentVM().test()) {
          InterruptHandler.getInstance().handleInterrupt();
        }
      }
    }
  }
}
