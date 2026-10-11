package org.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class RAM {
  public char[] ramContent = new char[Config.ram_size * Config.ram_word_size];
  int userDataPointer = 0;
  int supervisorDataPointer = Config.supervisor_first_block * Config.block_size;

  // initialize the array with empty values, currently only used for data printing
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

  // Only used for printing to test the data
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

  // Currently reads the HDD.txt file, finds the fileName (set inside ChannelDevice's FN flag),
  // loads all the commands to supervisor memory
  public ArrayList<String> loadData(String HDD, CPU cpu) {
    File myHdd = new File(HDD);
    try (Scanner myReader = new Scanner(myHdd)) {
      String prev = "";
      boolean storeToSupervisor = false;
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        // TODO: parsing logic:
        // 1. We need to find the fileName
        // 2. Upload all of its contents to supervisor memory
        // 3. Add checks and update flags accordingly

        if (data.equals("----") && storeToSupervisor) {
          break;
        }

        if (storeToSupervisor) {
          supervisorDataPointer = insertToArray(data, supervisorDataPointer);
        }

        // We found the file name, and we need to start loading all
        // the data to supervisor memory
        if (prev.equals("----") && data.equals(cpu.getFileName())) storeToSupervisor = true;

        prev = data;
      }
    } catch (FileNotFoundException e) {
      System.out.println("Couldn't read the file");
      e.printStackTrace();
    }
    return getValidData();
  }
}
