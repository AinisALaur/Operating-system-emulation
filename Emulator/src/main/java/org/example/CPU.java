package org.example;

public class CPU {
  private RAM ram;
  private String HDD;

  // Will inicialize fields, when we know their default values
  private char[] r0 = new char[4];
  private char[] r1 = new char[4];
  private char[] ip = new char[2];
  private char[] sf = new char[4];
  private char[] ptr = new char[2];
  private char[] mode = {1};
  private char[] pi = new char[1];
  private char[] si = new char[1];
  private char[] ti = new char[1];

  public CPU(RAM ram, String HDD) {
    this.ram = ram;
    this.HDD = HDD;
  }

  public void loadDataToRam() {
    ram.loadData(HDD, mode);
  }
}
