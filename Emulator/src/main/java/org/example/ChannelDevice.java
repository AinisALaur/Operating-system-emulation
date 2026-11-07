package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ChannelDevice {
  private static ChannelDevice instance = null;

  private char[] sb = {'0', '0'}; // source block
  private char[] db = {'0', '0'}; // destination block
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

  public char[] getSB() {
    return sb.clone();
  }

  public void setSB(char newSB) {
    sb[0] = '0';
    sb[1] = newSB;
  }

  public void setSB(char[] newSB) {
    if (newSB == null || newSB.length == 0 || newSB.length > sb.length) {
      throw new IllegalArgumentException("Invalid SB value");
    }
    for (char value : newSB) {
      if (Character.digit(value, 16) < 0) {
        throw new IllegalArgumentException("Invalid SB value");
      }
    }

    Arrays.fill(sb, '0');
    System.arraycopy(newSB, 0, sb, sb.length - newSB.length, newSB.length);
  }

  public char[] getDB() {
    return db.clone();
  }

  public void setDB(char[] newDB) {
    if (newDB == null || newDB.length == 0 || newDB.length > db.length) {
      throw new IllegalArgumentException("Invalid DB value");
    }
    for (char value : newDB) {
      if (Character.digit(value, 16) < 0) {
        throw new IllegalArgumentException("Invalid DB value");
      }
    }

    Arrays.fill(db, '0');
    System.arraycopy(newDB, 0, db, db.length - newDB.length, newDB.length);
  }

  public char[] getFN() {
    return fn.clone();
  }

  public void setFN(char[] newFN) {
    if (newFN == null || newFN.length == 0 || newFN.length > fn.length) {
      throw new IllegalArgumentException(
          "File name must contain between 1 and " + fn.length + " characters");
    }

    Arrays.fill(fn, ' ');
    System.arraycopy(newFN, 0, fn, 0, newFN.length);
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

  public void setST(char newST) {
    st[0] = newST;
  }

  public void setDT(char newDT) {
    dt[0] = newDT;
  }

  public void xchg() {
    if (st[0] == '1' && dt[0] == '3') {
      moveUserOutput(); // user memory -> output stream
    } else if (st[0] == '2' && dt[0] == '1') {
      moveSupervisorUser(); // supervisor memory -> user memory
    } else if (st[0] == '3' && dt[0] == '2') {
      moveDiskSupervisor(); // hdd -> supervisor memory
    } else if (st[0] == '4' && dt[0] == '1') {
      moveInputUser(); // input stream -> user memory (actually register R1)
    } else if (st[0] == '1' && dt[0] == '1') { // user memory -> user memory
      moveUserToUser();
    } else {
      throw new IllegalArgumentException(
          "Invalid source and destination object pairs: ST = " + st[0] + ", DT = " + dt[0]);
    }
  }

  // Copies a whole block inside user memory. SB and DB are REAL block numbers
  // (the supervisor translates virtual -> real through each VM's PTR before calling XCHG).
  private void moveUserToUser() {
    RAM ram = RAM.getInstance();
    int blockBytes = Config.block_size * Config.ram_word_size;
    setBL(
        String.format("%02X", blockBytes)
            .toCharArray()); // whole block: 16 words * 4 bytes = 64 (0x40)

    int sourceBlock = Mapper.toInt(new String(getSB()));
    int destinationBlock = Mapper.toInt(new String(getDB()));
    if (sourceBlock < 0
        || sourceBlock >= Config.user_block_amount
        || destinationBlock < 0
        || destinationBlock >= Config.user_block_amount) {
      throw new IndexOutOfBoundsException(
          "User block out of range: SB = " + sourceBlock + ", DB = " + destinationBlock);
    }

    int sourcePointer = sourceBlock * blockBytes;
    int destinationPointer = destinationBlock * blockBytes;
    int length = Mapper.toInt(new String(getBL()));
    for (int i = 0; i < length; i += Config.ram_word_size) {
      ram.writeWordAt(destinationPointer + i, ram.readWordAt(sourcePointer + i));
    }
  }

  private void moveDiskSupervisor() {
    String hdd = RM.geHDD();
    String fileName = new String(getFN()).trim();

    int pointer =
        Mapper.toInt(new String(getDB()).trim()) * Config.block_size * Config.ram_word_size;
    File myHdd = new File(hdd);

    List<String> program = readProgramBlock(myHdd, fileName);
    if (program == null) {
      return;
    }

    RM.getInstance().setFileFoundState();
    for (String data : program) {
      RAM.getInstance().writeWordAt(pointer, data);
      pointer += Config.ram_word_size;
    }
  }

  public static void validateHddFormat(File hdd, String fileName) {
    if (readProgramBlock(hdd, fileName) == null) {
      throw new IllegalArgumentException("File not found on HDD: " + fileName);
    }
  }

  private static List<String> readProgramBlock(File hdd, String fileName) {
    List<String> lines = readHddLines(hdd);
    List<String> block = new ArrayList<>();
    boolean insideBlock = false;

    for (String line : lines) {
      if (line.equals(Config.hdd_separator)) {
        if (!block.isEmpty() && block.get(0).equals(fileName)) {
          return validateBlock(block);
        }
        block.clear();
        insideBlock = true;
      } else if (insideBlock) {
        block.add(line);
      }
    }

    if (!block.isEmpty() && block.get(0).equals(fileName)) {
      throw new IllegalArgumentException(
          "Invalid HDD format for '" + fileName + "': missing closing separator");
    }
    return null;
  }

  private static List<String> validateBlock(List<String> block) {
    String fileName = block.get(0);
    String prefix = "Invalid HDD format for '" + fileName + "': ";

    if (fileName.length() != Config.ram_word_size) {
      throw new IllegalArgumentException(prefix + "filename must contain 4 characters");
    }
    if (block.size() < 2 || !block.get(1).equals(Config.program_start)) {
      throw new IllegalArgumentException(prefix + "expected '" + Config.program_start + "'");
    }

    int end = block.indexOf(Config.program_end);
    if (end < 0) {
      throw new IllegalArgumentException(prefix + "expected '" + Config.program_end + "'");
    }
    if (end != block.size() - 1) {
      throw new IllegalArgumentException(
          prefix + "unexpected content after '" + Config.program_end + "'");
    }

    for (int i = 2; i < end; ++i) {
      if (block.get(i).length() != Config.ram_word_size) {
        throw new IllegalArgumentException(
            prefix
                + "program line must be exactly "
                + Config.ram_word_size
                + " characters, got '"
                + block.get(i)
                + "'");
      }
    }

    return new ArrayList<>(block.subList(2, end));
  }

  public static void validateHddFormat(File hdd) {
    List<String> lines = readHddLines(hdd);
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("HDD file is empty");
    }
    if (!lines.get(0).equals(Config.hdd_separator)) {
      throw new IllegalArgumentException(
          "Invalid HDD format: expected opening '" + Config.hdd_separator + "'");
    }

    List<String> block = new ArrayList<>();
    int blockCount = 0;

    for (String line : lines) {
      if (line.equals(Config.hdd_separator)) {
        if (!block.isEmpty()) {
          validateBlock(block);
          blockCount += 1;
          block.clear();
        }
      } else {
        block.add(line);
      }
    }

    if (!block.isEmpty()) {
      throw new IllegalArgumentException(
          "Invalid HDD format for '" + block.get(0) + "': missing closing separator");
    }
    if (blockCount == 0) {
      throw new IllegalArgumentException("HDD file must contain at least one program block");
    }
  }

  private static List<String> readHddLines(File hdd) {
    if (hdd == null || !hdd.isFile()) {
      throw new IllegalArgumentException("HDD file does not exist: " + hdd);
    }

    List<String> lines;
    try {
      lines = new ArrayList<>(Files.readAllLines(hdd.toPath()));
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read HDD file: " + hdd, e);
    }

    while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
      lines.remove(lines.size() - 1);
    }

    if (lines.isEmpty()) {
      return lines;
    }
    return lines;
  }

  private void moveSupervisorUser() {
    RAM ram = RAM.getInstance();
    int supervisorPointer =
        Mapper.toInt(new String(getSB()).trim()) * Config.block_size * Config.ram_word_size;
    int user = Mapper.toInt(new String(getDB()).trim()) * Config.block_size * Config.ram_word_size;

    while (supervisorPointer <= Config.ram_size * Config.ram_word_size - Config.ram_word_size
        && ram.getByte(supervisorPointer) != ' ') {
      if (user < 0 || user > Config.VM_ram_size - Config.ram_word_size) {
        throw new IndexOutOfBoundsException(
            "User pointer out of bounds: " + user + ", VM RAM size: " + Config.VM_ram_size);
      }

      int userDataPointer = ram.getPointerFromPTR(user);
      String command =
          ""
              + ram.getByte(supervisorPointer)
              + ram.getByte(supervisorPointer + 1)
              + ram.getByte(supervisorPointer + 2)
              + ram.getByte(supervisorPointer + 3);

      ram.clearWordAt(supervisorPointer);

      if (command.charAt(0) == '$' && command.charAt(3) == '$') {
        int x;
        int y;
        try {
          x = Mapper.toInt(String.valueOf(command.charAt(1)));
          y = Mapper.toInt(String.valueOf(command.charAt(2)));
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid pointer format in command: " + command, e);
        }

        int pointer = x * Config.block_size * Config.ram_word_size + y * Config.ram_word_size;
        if (pointer < 0 || pointer > Config.VM_ram_size - Config.ram_word_size) {
          throw new IndexOutOfBoundsException(
              "Pointer out of bounds: " + pointer + ", VM RAM size: " + Config.VM_ram_size);
        } else {
          user = pointer;
          supervisorPointer += Config.ram_word_size;
          continue;
        }
      }

      ram.writeWordAt(userDataPointer, command);
      user += 4;
      supervisorPointer += 4;
    }
  }

  private void moveInputUser() {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.print(
          "Enter a number between "
              + Config.min_number_in_register
              + " and "
              + Config.max_number_in_register
              + ": ");

      String input = scanner.nextLine().trim();
      try {
        int number = Integer.parseInt(input);
        CPU.getInstance().setR1(Mapper.toHex(number).toCharArray());
        return;
      } catch (IllegalArgumentException e) {
        System.out.println("Invalid number, try again.");
      }
    }
  }

  private void moveUserOutput() {
    VM currentVM = RM.getInstance().getCurrentVM();
    if (currentVM == null) {
      throw new IllegalStateException("Cannot output memory without a current VM");
    }

    int sourceBlock = Mapper.toInt(new String(getSB()).trim());
    int byteOffset = Mapper.toInt(new String(getFF()));
    int byteLength = Mapper.toInt(new String(getBL()));
    int blockByteSize = Config.block_size * Config.ram_word_size;

    int virtualPointer = sourceBlock * blockByteSize + byteOffset;
    int bytesAvailable = Config.VM_ram_size - virtualPointer;
    int outputLength = Math.min(byteLength, bytesAvailable);
    StringBuilder output = new StringBuilder(outputLength);
    for (int i = 0; i < outputLength; ++i) {
      int realPointer = RM.getInstance().getVmPointer(virtualPointer + i);
      output.append(RAM.getInstance().getByte(realPointer));
    }
    System.out.println(output);
  }
}
