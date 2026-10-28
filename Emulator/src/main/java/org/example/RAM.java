package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RAM {
  private static char[] ramContent =
      new char[Config.block_size * Config.ram_word_size * Config.block_amount];
  private static RAM instance = null;

  private RAM() {}

  public static RAM getInstance() {
    if (instance == null) {
      instance = new RAM();
      for (int i = 0; i < Config.ram_size * Config.ram_word_size; ++i) {
        ramContent[i] = ' ';
      }
    }
    return instance;
  }

  public String getWord(int block, int word) {
    validateBlock(block);
    if (word < 0 || word >= Config.block_size) {
      throw new IndexOutOfBoundsException(
          "Word must be between 0 and " + (Config.block_size - 1) + ": " + word);
    }

    int pointer = block * Config.block_size * Config.ram_word_size + word * Config.ram_word_size;
    return ""
        + ramContent[pointer]
        + ramContent[pointer + 1]
        + ramContent[pointer + 2]
        + ramContent[pointer + 3];
  }

  public char getByte(int pointer) {
    if (pointer < 0 || pointer >= ramContent.length) {
      throw new IndexOutOfBoundsException("RAM pointer out of bounds: " + pointer);
    }
    return ramContent[pointer];
  }

  public int getPointerFromPTR(int pointer, int vm) {
    validateVm(vm);
    if (pointer < 0 || pointer >= Config.VM_ram_size) {
      throw new IndexOutOfBoundsException(
          "VM pointer must be between 0 and " + (Config.VM_ram_size - 1) + ": " + pointer);
    }

    int block = pointer / (Config.ram_word_size * Config.block_size);
    int word = pointer % (Config.ram_word_size * Config.block_size);

    int start = Config.block_size * Config.ram_word_size;
    // decides where the ptr table is
    switch (vm) {
      case 0:
        start *= 48;
        break;
      case 1:
        start *= 49;
        break;
      case 2:
        start *= 50;
        break;
    }
    String mappedBlock =
        ""
            + ramContent[start + block * Config.ram_word_size]
            + ramContent[start + block * Config.ram_word_size + 1]
            + ramContent[start + block * Config.ram_word_size + 2]
            + ramContent[start + block * Config.ram_word_size + 3];
    int realBlock = Mapper.toInt(mappedBlock);
    if (realBlock < 0 || realBlock >= Config.user_block_amount) {
      throw new IndexOutOfBoundsException("PTR maps to invalid real block: " + realBlock);
    }
    return realBlock * Config.block_size * Config.ram_word_size + word;
  }

  private int insertToRam(String data, int pointer) {
    if (data == null) {
      throw new IllegalArgumentException("Data must not be null");
    }
    data = data.trim();
    if (pointer < 0 || pointer + data.length() > ramContent.length) {
      throw new IndexOutOfBoundsException("Data does not fit in RAM");
    }
    // TODO: check data.lenght() == 4
    for (int i = 0; i < data.length(); ++i) {
      ramContent[pointer] = data.charAt(i);
      ++pointer;
    }
    return pointer;
  }

  // Only used for printing to test the data
  public void printValidData() {
    int charsPerWord = 4;
    int wordsPerBlock = 16;
    int charsPerBlock = wordsPerBlock * charsPerWord;
    int totalChars = 67 * charsPerBlock;

    for (int i = 0; i < totalChars; i += charsPerWord) {
      int block = i / charsPerBlock;
      int word = (i % charsPerBlock) / charsPerWord;

      if (ramContent[i] != ' ') {
        System.out.println(
            Mapper.toHex(block)
                + " "
                + Mapper.toHex(word)
                + " "
                + ramContent[i]
                + ramContent[i + 1]
                + ramContent[i + 2]
                + ramContent[i + 3]);
      }
    }
  }

  public void loadToSupervisor() {
    String hdd = RM.geHDD();
    String fileName = ChannelDevice.getInstance().getFN();

    int pointer = Config.supervisor_first_block * Config.block_size * Config.ram_word_size;
    boolean foundFile = false;
    boolean foundStart = false;
    File myHdd = new File(hdd);

    try (Scanner myReader = new Scanner(myHdd)) {
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();

        if (!foundFile) {
          if (data.equals(fileName)) {
            RM.getInstance().setFileFoundState();
            foundFile = true;
          }
          continue;
        }

        if (!foundStart) {
          if (data.equals(Config.program_start)) {
            foundStart = true;
          }
          continue;
        }

        if (data.equals(Config.program_end)) {
          break;
        }

        pointer = insertToRam(data, pointer);
      }
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't read the file");
      e.printStackTrace();
    }
  }

  private void fillPtrTable(int vm) {
    validateVm(vm);
    int start = 0;
    switch (vm) {
      case 1:
        start = 1;
        break;
      case 2:
        start = 2;
        break;
    }

    int pointer =
        Mapper.toInt(new String(CPU.getInstance().getPTR()))
            * Config.block_size
            * Config.ram_word_size;
    for (int i = 0; i < Config.block_size; ++i) {
      String hex = Mapper.toHex(start);
      for (int x = 0; x < hex.length(); ++x) {
        ramContent[pointer] = hex.charAt(x);
        pointer += 1;
      }
      pointer += (Config.ram_word_size - hex.length());
      start += 3;
    }
  }

  // VM1 gets every third block starting from 0, VM1 gets every third starting from 1, VM3 gets
  // every third starting from 2
  // this function currently only loads to the first VM'S blocks
  public void loadToUser(int vm) {
    validateVm(vm);
    int supervisorPointer =
        Config.supervisor_first_block * Config.block_size * Config.ram_word_size;
    int user = Config.code_segment_start; // points to 5 0
    while (supervisorPointer <= ramContent.length - Config.ram_word_size
        && ramContent[supervisorPointer] != ' ') {
      if (user < 0 || user > Config.VM_ram_size - Config.ram_word_size) {
        throw new IndexOutOfBoundsException(
            "User pointer out of bounds: " + user + ", VM RAM size: " + Config.VM_ram_size);
      }
      int userDataPointer = getPointerFromPTR(user, vm);
      String command =
          ""
              + ramContent[supervisorPointer]
              + ramContent[supervisorPointer + 1]
              + ramContent[supervisorPointer + 2]
              + ramContent[supervisorPointer + 3];

      ramContent[supervisorPointer] = ' ';
      ramContent[supervisorPointer + 1] = ' ';
      ramContent[supervisorPointer + 2] = ' ';
      ramContent[supervisorPointer + 3] = ' ';

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

      ramContent[userDataPointer] = command.charAt(0);
      ramContent[userDataPointer + 1] = command.charAt(1);
      ramContent[userDataPointer + 2] = command.charAt(2);
      ramContent[userDataPointer + 3] = command.charAt(3);
      user += 4;
      supervisorPointer += 4;
    }
  }

  // Fills the vm's PTR table, then loads all the data from supervisor memory to user memory
  public void loadData(int vm) {
    validateVm(vm);
    CPU cpu = CPU.getInstance();
    cpu.setPTR(vm);
    fillPtrTable(vm);
    loadToUser(vm);
  }

  private static void validateBlock(int block) {
    if (block < 0 || block >= Config.block_amount) {
      throw new IndexOutOfBoundsException(
          "Block must be between 0 and " + (Config.block_amount - 1) + ": " + block);
    }
  }

  private static void validateVm(int vm) {
    if (vm < 0 || vm >= Config.VM_amount) {
      throw new IndexOutOfBoundsException(
          "VM number must be between 0 and " + (Config.VM_amount - 1) + ": " + vm);
    }
  }
}
