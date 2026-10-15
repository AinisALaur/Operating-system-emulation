package org.example;

public class CPU {
  private static CPU instance = null;

  // Will inicialize fields, when we know their default values
  private char[] r0 = new char[4];
  private char[] r1 = new char[4];
  private char[] ip = new char[2];
  private char[] sf = new char[4];
  private char[] ptr = new char[2];
  private char[] mode = {'1'};
  private char[] pi = new char[1];
  private char[] si = new char[1];
  private char[] ti = new char[1];

  private CPU() {}

  public static CPU getInstance() {
    if (instance == null) {
      instance = new CPU();
    }
    return instance;
  }

  public char getMODE() {
    return mode[0];
  }

  public void setMODE(char mode) {
    this.mode[0] = mode;
  }

  // The 17th block of each VM is reserved for the PTR table. block saved as hex
  public void setPTR(int vmNum) {
    switch (vmNum) {
      case 0: // block 48 in dec
        ptr[0] = '3';
        ptr[1] = '0';
        break;
      case 1: // block 49 in dec
        ptr[0] = '3';
        ptr[1] = '1';
        break;
      case 2: // block 50 in dec
        ptr[0] = '3';
        ptr[1] = '2';
        break;
    }
  }

  // Curent VM's PTR table location
  public char[] getPTR() {
    return ptr;
  }
}
