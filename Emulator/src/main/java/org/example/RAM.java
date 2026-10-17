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
    int pointer = block * Config.block_size * Config.ram_word_size + word * Config.ram_word_size;
    return ""
        + ramContent[pointer]
        + ramContent[pointer + 1]
        + ramContent[pointer + 2]
        + ramContent[pointer + 3];
  }

  public int getPointerFromPTR(int pointer, int vm) {
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
        start *= 40;
        break;
    }
    return Mapper.toInt(
                ""
                    + ramContent[start + block * Config.ram_word_size]
                    + ramContent[start + block * Config.ram_word_size + 1]
                    + ramContent[start + block * Config.ram_word_size + 2]
                    + ramContent[start + block * Config.ram_word_size + 3])
            * Config.block_size
            * Config.ram_word_size
        + word;
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

  // TODO: add guards
  public void loadToSupervisor() {
    String hdd = RM.geHDD();
    String fileName = ChannelDevice.getInstance().getFN();

    int pointer = Config.supervisor_first_block * Config.block_size * Config.ram_word_size;
    boolean foundFile = false;
    boolean foundStart = false;
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

  private void fillPtrTable(int vm) {
    int start = 0;
    switch (vm) {
      case 1:
        start = 1;
        break;
      case 2:
        start = 2;
        break;
    }

    char[] ptr = CPU.getInstance().getPTR();
    int pointer = Mapper.toInt("" + ptr[0] + ptr[1]) * Config.block_size * Config.ram_word_size;
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
    int supervisorPointer =
        Config.supervisor_first_block * Config.block_size * Config.ram_word_size;
    int user = Config.code_segment_start; // points to 5 0
    while (supervisorPointer < Config.ram_size * Config.ram_word_size
        && ramContent[supervisorPointer] != ' ') {
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

      // TODO: add guards so it doesn't jump to where its not supposed to
      if(command.charAt(0) == '$' && command.charAt(3) == '$'){
       try{
         int x = command.charAt(1) - '0';
         int y = command.charAt(2) - '0';

         int pointer = x * Config.block_size * Config.ram_word_size + y * Config.ram_word_size;

         if(pointer > Config.VM_ram_size){
           RM.getInstance().setPI('1');
           return;
         }else{
           user = pointer;
           supervisorPointer += 4;
           continue;
         }
       } catch (Exception e){
          RM.getInstance().setPI('3');
          return;
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
    CPU cpu = CPU.getInstance();
    cpu.setPTR(vm);
    fillPtrTable(vm);
    loadToUser(vm);
  }
}
