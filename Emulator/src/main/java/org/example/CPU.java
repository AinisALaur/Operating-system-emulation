package org.example;

import java.util.ArrayList;

public class CPU {
  private RAM ram;
  private String HDD;
  ChannelDevice channelDevice;

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
    channelDevice = new ChannelDevice();
  }

  public void halt() {
    si[0] = 3;
  }

  public char getSI() {
    return si[0];
  }

  public char getMODE() {
    return mode[0];
  }

  public void setMODE(char mode) {
    this.mode[0] = mode;
  }

  // Saved as hex
  public void setPTR(int vmNum) {
    switch (vmNum) {
      case 1:
        ptr[0] = '3';
        ptr[1] = '0';
        break;
      case 2:
        ptr[0] = '3';
        ptr[1] = '1';
        break;
      case 3:
        ptr[0] = '3';
        ptr[1] = '2';
        break;
    }
  }

  public char[] getPTR() {
    return ptr;
  }

  public void setFileName(String fileName) {
    channelDevice.setFn(fileName);
  }

  public String getFileName() {
    return channelDevice.getFn();
  }

  public ArrayList<String> loadDataToRam() {
    return ram.loadData(HDD, this);
  }
}
