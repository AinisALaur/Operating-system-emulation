package org.example;

public class InterruptHandler {
  private static InterruptHandler instance = null;

  // used for supervisor interrupts 2 and 4 (see 2.3 in description pdf)
  private char x; // reused for supervisor interrupts 2 and 4
  private char y;
  private char z;
  private char[] b = {'0', '0', '0', '0'}; // number of bytes to output, coming from R0

  public static InterruptHandler getInstance() {
    if (instance == null) {
      instance = new InterruptHandler();
    }
    return instance;
  }

  public char getX() {
    return x;
  }

  public void setX(char x) {
    this.x = x;
  }

  public char getY() {
    return y;
  }

  public void setY(char y) {
    this.y = y;
  }

  public char getZ() {
    return z;
  }

  public void setZ(char z) {
    this.z = z;
  }

  public char[] getB() {
    return b.clone();
  }

  public void setB(char[] b) {
    this.b = b.clone();
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

  private void handleProgramInterrupt(char pi) {
    switch (pi) {
      case '1' -> System.out.println("Program interrupt: Invalid address");
      case '2' -> System.out.println("Program interrupt: Invalid OP code");
      case '3' -> System.out.println("Program interrupt: Invalid number");
      default -> System.out.println("Program interrupt: Unknown (" + pi + ")");
    }
    RM.getInstance().turnOffVM(); // don't need to reset PI = 0 because we are turning off the VM
  }

  // TODO: implement full handling
  private void handleSupervisorInterrupt(char si) {
    switch (si) {
      case '1' -> {
        System.out.println("Supervisor interrupt: Input operation");
        ChannelDevice.getInstance().setST('4');
        ChannelDevice.getInstance().setDT('1');
        ChannelDevice.getInstance().xchg();
      }
      case '2' -> {
        System.out.println("Supervisor interrupt: Output operation");
        ChannelDevice.getInstance().setST('1');
        ChannelDevice.getInstance().setDT('3');
        ChannelDevice.getInstance().setSB(getX()); // source block
        int word = Mapper.toInt(String.valueOf(getY()));
        int byteOffset = Mapper.toInt(String.valueOf(getZ()));
        int length = Mapper.toInt(new String(getB()));
        ChannelDevice.getInstance()
            .setFF(
                String.format("%02X", word * Config.ram_word_size + byteOffset)
                    .toCharArray()); // byte offset in block
        ChannelDevice.getInstance()
            .setBL(
                String.format("%02X", length).toCharArray()); // byte length (number of bytes sent)
        ChannelDevice.getInstance().xchg();
      }
      case '3' -> {
        System.out.println("Supervisor interrupt: HALT operation");
        RM.getInstance().turnOffVM();
      }
      case '4' -> {
        System.out.println("Supervisor interrupt: GOnx");
        // @Ainis need to implement starting a new VM, where block x from old VM is the parameter
        // block for the new VM
      }
      default -> System.out.println("Supervisor interrupt: Unknown (" + si + ")");
    }
    RM.getInstance()
        .setSI('0'); // reset SI to 0 after interrupt, in case of HALT this doesn't matter
  }

  // TODO: implement full handling (in the next part of the project)
  private void handleTimerInterrupt() {
    System.out.println("Timer interrupt occurred");
  }
}
