package org.example;

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

  public char getByte(int pointer) {
    if (pointer < 0 || pointer >= ramContent.length) {
      throw new IndexOutOfBoundsException("RAM pointer out of bounds: " + pointer);
    }
    return ramContent[pointer];
  }

  public String readWordAt(int pointer) {
    if (pointer < 0 || pointer + Config.ram_word_size > ramContent.length) {
      throw new IndexOutOfBoundsException("RAM pointer out of bounds: " + pointer);
    }
    return ""
        + ramContent[pointer]
        + ramContent[pointer + 1]
        + ramContent[pointer + 2]
        + ramContent[pointer + 3];
  }

  public void writeWordAt(int pointer, String data) {
    if (data == null || data.length() != Config.ram_word_size) {
      throw new IllegalArgumentException(
          "Word data must contain exactly " + Config.ram_word_size + " characters");
    }
    if (pointer < 0 || pointer + Config.ram_word_size > ramContent.length) {
      throw new IndexOutOfBoundsException("RAM pointer out of bounds: " + pointer);
    }
    for (int i = 0; i < Config.ram_word_size; ++i) {
      ramContent[pointer + i] = data.charAt(i);
    }
  }

  public void clearWordAt(int pointer) {
    writeWordAt(pointer, "    ");
  }

  public int getPointerFromPTR(int pointer) {
    if (pointer < 0 || pointer >= Config.VM_ram_size) {
      throw new IndexOutOfBoundsException(
          "VM pointer must be between 0 and " + (Config.VM_ram_size - 1) + ": " + pointer);
    }

    int block = pointer / (Config.ram_word_size * Config.block_size);
    int word = pointer % (Config.ram_word_size * Config.block_size);

    int start =
        Config.block_size
            * Config.ram_word_size
            * Mapper.toInt(new String(CPU.getInstance().getPTR()));
    String mappedBlock = readWordAt(start + block * Config.ram_word_size);
    int realBlock = Mapper.toInt(mappedBlock);
    if (realBlock < 0 || realBlock >= Config.user_block_amount) {
      throw new IndexOutOfBoundsException("PTR maps to invalid real block: " + realBlock);
    }
    return realBlock * Config.block_size * Config.ram_word_size + word;
  }

  public void printMemory() {
    System.out.println("-------------------------");
    for (int block = 0; block < Config.block_amount; ++block) {
      for (int word = 0; word < Config.block_size; ++word) {
        int pointer =
            block * Config.block_size * Config.ram_word_size + word * Config.ram_word_size;
        String value = readWordAt(pointer);
        System.out.println(
            Mapper.formatDisplayHex(block) + " " + Mapper.formatDisplayHex(word) + " " + value);
      }
    }
    System.out.println("-------------------------");
  }

  void fillPtrTable() {
    int pointer =
        Mapper.toInt(new String(CPU.getInstance().getPTR()))
            * Config.block_size
            * Config.ram_word_size;

    for (int i = 0; i < Config.block_size; ++i) {
      String block = RM.getInstance().getNextAvailableBlock();
      for (int x = 0; x < block.length(); ++x) {
        ramContent[pointer] = block.charAt(x);
        pointer += 1;
      }
      pointer += (Config.ram_word_size - block.length());
    }
  }
}
