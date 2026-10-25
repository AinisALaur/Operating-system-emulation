package org.example;

// TODO: currently some of the interrupt resisters(like PI) is set at incorrect places (please read
// chapter 2.6)
public class InterruptHandler {
  private static InterruptHandler instance = null;

  public static InterruptHandler getInstance() {
    if (instance == null) {
      instance = new InterruptHandler();
    }
    return instance;
  }

  public void handleInterrupt() {
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
  }

  // TODO: implement full handling
  private void handleProgramInterrupt(char pi) {
    if (pi == '1') {
      System.out.println("Program interrupt: Invalid address");
    } else if (pi == '2') {
      System.out.println("Program interrupt: Invalid OP code");
    } else if (pi == '3') {
      System.out.println("Program interrupt: Invalid assignment");
    } else if (pi == '4') {
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
    }
  }

  // TODO: implement full handling
  private void handleTimerInterrupt() {
    System.out.println("Timer interrupt occurred");
  }
}
