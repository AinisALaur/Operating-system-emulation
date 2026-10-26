package org.example;

import java.util.Arrays;

public class ChannelDevice {
  private static ChannelDevice instance = null;

  private char[] sb = new char[1]; // source block
  private char[] db = new char[1]; // destination block
  private char[] fn = new char[4]; // file name
  private char[] ff = new char[2]; // byte offset in block
  private char[] bl = new char[2]; // byte length (number of bytes sent)
  private char[] st = new char[1]; // source object number
  private char[] dt = new char[1]; // destination object number

  public static ChannelDevice getInstance() {
    if (instance == null) {
      instance = new ChannelDevice();
    }
    return instance;
  }

  public char getSB() {
    return sb[0];
  }

  public void setSB(char newSB) {
    sb[0] = newSB;
  }

  public char getDB() {
    return db[0];
  }

  public void setDB(char newDB) {
    db[0] = newDB;
  }

  // Should return char[] for consistency
  public String getFN() {
    return new String(fn).trim();
  }

  // Should use char[] as a parameter for consistency
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

  public char[] getFF() {
    return ff.clone();
  }

  public void setFF(char[] newFF) {
    if (newFF == null
        || newFF.length != ff.length
        || Character.digit(newFF[0], 16) < 0
        || Character.digit(newFF[1], 16) < 0) {
      throw new IllegalArgumentException("Invalid FF value");
    }

    ff[0] = newFF[0];
    ff[1] = newFF[1];
  }

  public char[] getBL() {
    return bl.clone();
  }

  public void setBL(char[] newBL) {
    if (newBL == null
        || newBL.length != bl.length
        || Character.digit(newBL[0], 16) < 0
        || Character.digit(newBL[1], 16) < 0) {
      throw new IllegalArgumentException("Invalid BL value");
    }

    bl[0] = newBL[0];
    bl[1] = newBL[1];
  }

  public char getST() {
    return st[0];
  }

  public void setST(char newST) {
    st[0] = newST;
  }

  public char getDT() {
    return dt[0];
  }

  public void setDT(char newDT) {
    dt[0] = newDT;
  }

  // TODO: xchg based on Channel Device's registers
  // currently suggested function signatures, though probably logical
  public void xchg() {
    if (st[0] == '1' && dt[0] == '3') { // user memory -> output stream
      // TODO: func moveUserOutput(char[] sb, char[] ff, char[] bl);
    } else if (st[0] == '2' && dt[0] == '1') { // supervisor memory -> user memory
      // TODO: func moveSupervisorUser(char[] sb = supervisor block, char[] db = vm memory block,
      // char[] bl = block size in bytes);
    } else if (st[0] == '3' && dt[0] == '2') { // hdd -> supervisor memory
      // TODO: func moveDiskSupervisor(char[] db, char[] fn);
    } else if (st[0] == '4' && dt[0] == '1') { // input stream -> user memory
      // TODO: moveInputUser(); should read and put read decimal number in valid range as hex in R1
    } else {
      throw new IllegalArgumentException(
          "Invalid source and destination object pairs: ST = " + st[0] + ", DT = " + dt[0]);
    }
  }
}
