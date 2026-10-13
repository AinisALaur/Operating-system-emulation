package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RAM {
  public char[] ramContent =
      new char[Config.block_size * Config.ram_word_size * Config.block_amount];

  // initialize the array with empty values, currently only used for data printing
  public RAM() {
    for (int i = 0; i < Config.ram_size * Config.ram_word_size; ++i) {
      ramContent[i] = ' ';
    }
  }

  private int insertToRam(String data, int pointer) {
    data = data.trim();
    for (int i = 0; i < data.length(); ++i) {
      ramContent[pointer] = data.charAt(i);
      ++pointer;
    }
    return pointer;
  }

  // Only used for printing to test the data
  public ArrayList<String> getValidData() {
    ArrayList<String> result = new ArrayList<>();

    int charsPerWord = 4;
    int wordsPerBlock = 16;
    int charsPerBlock = wordsPerBlock * charsPerWord;
    int totalChars = 67 * charsPerBlock;

    for (int i = 0; i < totalChars; i += charsPerWord) {
      int block = i / charsPerBlock;
      int word = (i % charsPerBlock) / charsPerWord;

      if (ramContent[i] != ' ') {
        result.add(
            block
                + " "
                + word
                + " "
                + ramContent[i]
                + ramContent[i + 1]
                + ramContent[i + 2]
                + ramContent[i + 3]);
      }
    }

    return result;
  }

  // TODO: add guards
  public void loadToSupervisor(String hdd, CPU cpu) {
    int pointer = Config.supervisor_first_block * Config.block_size * Config.ram_word_size;
    boolean foundFile = false;
    boolean foundStart = false;
    String fileName = cpu.getFileName();
    File myHdd = new File(hdd);
    String prev = "";
    try (Scanner myReader = new Scanner(myHdd)) {
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();

        if (data.equals(Config.file_end)) {
          break;
        }

        if (data.equals(fileName) && prev.equals("----")) {
          foundFile = true;
        }

        if (foundFile && data.equals(Config.file_start)) {
          foundStart = true;
          continue;
        }

        if (foundStart) {
          pointer = insertToRam(data, pointer);
        }

        prev = data;
      }
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't read the file");
      e.printStackTrace();
    }
  }

  private void fillPtrTable(int vm, CPU cpu) {
    int start = 0;
    switch (vm) {
      case 2:
        start = 1;
        break;
      case 3:
        start = 2;
        break;
    }

    char[] ptr = cpu.getPTR();
    int pointer = ((ptr[0] - '0') * 16 + ptr[1] - '0') * 16 * 4;
    for (int i = 0; i < Config.block_size; ++i) {
      String hex = Integer.toHexString(start);
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
  public void loadToUser(CPU cpu) {}

  // Currently reads the HDD.txt file, finds the fileName (set inside ChannelDevice's FN flag),
  // loads all the commands to supervisor memory
  public ArrayList<String> loadData(String hdd, CPU cpu) {
    if (cpu.getMODE() == 1) {
      loadToSupervisor(hdd, cpu);
      cpu.setMODE('0');
      loadData(hdd, cpu);
    } else {
      cpu.setPTR(1);
      fillPtrTable(1, cpu);
    }

    return getValidData();
  }
}
