package org.example;

public class RM {
  RAM ram = new RAM();
  String HDD = "../HDD.txt";
  CPU cpu = new CPU(ram, HDD);

  public RM() {
    cpu.loadDataToRam();
  }
}
