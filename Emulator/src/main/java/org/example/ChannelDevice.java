package org.example;

public class ChannelDevice {
  private char[] sb = new char[1];
  private char[] db = new char[1];
  private char[] fn = new char[4];
  private char[] ff = new char[2];
  private char[] bl = new char[2];
  private char[] st = new char[1];
  private char[] dt = new char[1];

  // TODO: Check filename length
  public void setFn(String fileName) {
    for (int i = 0; i < fileName.length(); ++i) {
      fn[i] = fileName.charAt(i);
    }
  }

  public String getFn() {
    return "" + fn[0] + fn[1] + fn[2] + fn[3];
  }
}
