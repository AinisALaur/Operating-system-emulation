package org.example;

import java.util.ArrayList;

public class VM {
  private int number;

  public VM(int num) {
    this.number = num;
  }

  public void loadDataToMemory() {
    RM.getInstance().loadUserData(this.number);
  }

  // prints commands in the code segment, later should set its IP and move along it
  public ArrayList<String> getMyCommands() {
    int pointer = 320;
    ArrayList<String> commands = new ArrayList<>();
    while (pointer < Config.VM_ram_size) {
      int realPointer = RM.getInstance().getVmPointer(pointer, this.number);

      int realBlock = realPointer / (Config.block_size * Config.ram_word_size);
      int realWord = realPointer % (Config.block_size * Config.ram_word_size) / 4;

      int vmBlock = pointer / (Config.block_size * Config.ram_word_size);
      int vmWord = pointer % (Config.block_size * Config.ram_word_size) / 4;

      String word = RM.getInstance().getWord(realBlock, realWord);

      if (!word.trim().isEmpty())
        commands.add(
            Mapper.toHex(vmBlock)
                + " "
                + Mapper.toHex(vmWord)
                + " "
                + RM.getInstance().getWord(realBlock, realWord));

      pointer += 4;
    }
    return commands;
  }
}
