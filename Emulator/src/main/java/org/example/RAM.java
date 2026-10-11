package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RAM {
  public char[] ramContent = new char[Config.ram_size * Config.ram_word_size];
  int userDataPointer = 0;
  int supervisorDataPointer = Config.supervisor_first_block * Config.block_size;

  // initialize the array with empty values
  public RAM() {
    for (int i = 0; i < Config.ram_size * Config.ram_word_size; ++i) {
      ramContent[i] = '\0';
    }
  }

  private int insertToArray(String data, int pointer) {
    data = data.trim();
    for (int i = 0; i < data.length(); ++i) {
      ramContent[pointer] = data.charAt(i);
      ++pointer;
    }
    return pointer;
  }

  public ArrayList<String> getValidData() {
    ArrayList<String> result = new ArrayList<>();
    for (int i = 0; i < Config.ram_size * Config.ram_word_size; ++i) {
      int block = i / Config.block_size;
      int word = i % Config.block_size / 4;

      // each block has 16 words, but we're saving data in one continuous array so we need to
      // extract each 4 letter long words
      // TODO: print block and word in HEX (0 - F)
      if (ramContent[i] != '\0') {
        result.add(
            block
                + " "
                + word
                + " "
                + ramContent[i]
                + ramContent[i + 1]
                + ramContent[i + 2]
                + ramContent[i + 3]);
        i += 3;
      }
    }
    return result;
  }

  // Read data from file and insert it to memory array by mode (user or supervisor)
  // Currently returns for testing purposes
  public ArrayList<String> loadData(String HDD, char[] mode) {
    File myHdd = new File(HDD);
    try (Scanner myReader = new Scanner(myHdd)) {
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        if (mode[0] == 0) {
          userDataPointer = insertToArray(data, userDataPointer);
        } else {
          supervisorDataPointer = insertToArray(data, supervisorDataPointer);
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't read the file");
      e.printStackTrace();
    }
    return getValidData();
  }
}
