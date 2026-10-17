package org.example;

public class CPU {
  private static CPU instance = null;

  // Will inicialize fields, when we know their default values
  private char[] r0 = {' ', ' ', ' ', ' '};
  private char[] r1 = {' ', ' ', ' ', ' '};
  private char[] ip = {' ', ' '};
  private char[] sf = {' ', ' ', ' ', ' '};
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
    return r0;
  }

  public char[] getR1() {
    return r1;
  }

  public char[] getIP() {
    return ip;
  }

  public void setIP(char[] newIP) {
    if (newIP.length > 2) {
      setPI('1');
    }

    ip[0] = newIP[0];
    ip[1] = newIP[1];
  }

  public char[] getSF() {
    return sf;
  }

  public char getPI() {
    return pi;
  }

  public char getSI() {
    return si;
  }

  public void setSI(char newSI) {
    si = newSI;
  }

  public char getTI() {
    return ti;
  }

  public void setMODE(char mode) {
    this.mode = mode;
  }

  public void setPI(char newPI) {
    pi = newPI;
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
