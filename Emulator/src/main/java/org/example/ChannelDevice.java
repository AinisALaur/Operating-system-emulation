package org.example;

import java.util.Arrays;

public class ChannelDevice {
  private char[] sb = new char[1];
  private char[] db = new char[1];
  private char[] fn = new char[4];
  private char[] ff = new char[2];
  private char[] bl = new char[2];
  private char[] st = new char[1];
  private char[] dt = new char[1];

  private static ChannelDevice instance;

  public static ChannelDevice getInstance() {
    if (instance == null) {
      instance = new ChannelDevice();
    }
    return instance;
  }

  public void setFN(String fileName) {
    if (fileName == null || fileName.isBlank() || fileName.length() > fn.length) {
      throw new IllegalArgumentException(
          "File name must contain between 1 and " + fn.length + " characters");
    }

    Arrays.fill(fn, ' ');
    for (int i = 0; i < fileName.length(); ++i) {
      fn[i] = fileName.charAt(i);
    }
  }

  public String getFN() {
    return ("" + fn[0] + fn[1] + fn[2] + fn[3]).trim();
  }
}
