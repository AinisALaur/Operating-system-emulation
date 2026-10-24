package org.example;

import java.util.Arrays;

public class CPU {
  private static CPU instance = null;

  // Will inicialize fields, when we know their default values
  private char[] r0 = {'0', ' ', ' ', ' '};
  private char[] r1 = {'0', ' ', ' ', ' '};
  private char[] ip = {' ', ' '};
  private char[] sf = {'0', '0', '0', '0'};
  private char[] ptr = {' ', ' '};
  private char mode = '1';
  private char pi = '0';
  private char si = '0';
  private char ti = 'F';

  private CPU() {}

  public static CPU getInstance() {
    if (instance == null) {
      instance = new CPU();
    }
    return instance;
  }

  public char getMODE() {
    return mode;
  }

  public char[] getR0() {
    return r0.clone();
  }

  public void setR0(char[] newR0) {
    r0 = newR0;
  }

  public char[] getR1() {
    return r1.clone();
  }

  public void setR1(char[] newR1) {
    r1 = newR1;
  }

  public char[] getIP() {
    return ip.clone();
  }

  public void setIP(char[] newIP) {
    if (newIP == null
        || newIP.length != ip.length
        || Character.digit(newIP[0], 16) < 0
        || Character.digit(newIP[1], 16) < 0) {
      setPI('1');
      return;
    }

    ip[0] = newIP[0];
    ip[1] = newIP[1];
  }

  public char[] getSF() {
    return sf.clone();
  }

  public void setSF(int idx, char value) {
    if (idx < 0 || idx >= sf.length) {
      throw new IllegalArgumentException("Index out of range");
    }
    if (value < 0 || value > 1) {
      throw new IllegalArgumentException("Value out of range");
    }
    sf[idx] = value;
  }

  public char getPI() {
    return pi;
  }

  public char getSI() {
    return si;
  }

  public void setSI(char newSI) {
    validateInterrupt(newSI, "SI");
    si = newSI;
  }

  public char getTI() {
    return ti;
  }

  public void setMODE(char mode) {
    if (mode != '0' && mode != '1') {
      throw new IllegalArgumentException("MODE must be 0 or 1");
    }
    this.mode = mode;
  }

  public void setPI(char newPI) {
    validateInterrupt(newPI, "PI");
    pi = newPI;
  }

  // The 17th block of each VM is reserved for the PTR table. block saved as hex
  public void setPTR(int vmNum) {
    if (vmNum < 0 || vmNum >= Config.VM_amount) {
      setPI('1');
      return;
    }

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
    return ptr.clone();
  }

  public void clearRegisters() {
    Arrays.fill(r0, ' ');
    Arrays.fill(r1, ' ');
    Arrays.fill(ip, ' ');
    Arrays.fill(sf, ' ');
    Arrays.fill(ptr, ' ');
    mode = '1';
    pi = '0';
    si = '0';
    ti = 'F';
  }

  private static void validateInterrupt(char value, String register) {
    if (value < '0' || value > '3') {
      throw new IllegalArgumentException(register + " must be between 0 and 3");
    }
  }
}
