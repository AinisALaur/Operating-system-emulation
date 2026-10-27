package org.example;

public class InterruptHandler {
  private static InterruptHandler instance = null;

  public static InterruptHandler getInstance() {
    if (instance == null) {
      instance = new InterruptHandler();
    }
    return instance;
  }

  public void handleInterrupt() {
    CPU.getInstance().setMODE('1');
    char pi = RM.getInstance().getPI();
    char si = RM.getInstance().getSI();
    char ti = RM.getInstance().getTI();

    if (pi != '0') {
      handleProgramInterrupt(pi);
    } else if (si != '0') {
      handleSupervisorInterrupt(si);
    } else if (ti == '0') {
      handleTimerInterrupt();
    }
    CPU.getInstance().setMODE('0');
  }

  // TODO: implement full handling
  private void handleProgramInterrupt(char pi) {
    if (pi == '1') {
      System.out.println("Program interrupt: Invalid address");
    } else if (pi == '2') {
      System.out.println("Program interrupt: Invalid OP code");
    } else if (pi == '3') {
      System.out.println("Program interrupt: Invalid number");
    }
  }

  // TODO: implement full handling
  private void handleSupervisorInterrupt(char si) {
    if (si == '1') {
      System.out.println("Supervisor interrupt: Input operation");
    } else if (si == '2') {
      System.out.println("Supervisor interrupt: Output operation");
    } else if (si == '3') {
      System.out.println("Supervisor interrupt: HALT operation");
      RM.getInstance().turnOffVM();
    } else if (si == '4') {
      System.out.println("GOnx");
    }
  }

  // TODO: implement full handling
  private void handleTimerInterrupt() {
    System.out.println("Timer interrupt occurred");
  }
}
