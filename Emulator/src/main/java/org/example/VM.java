package org.example;

import java.util.Scanner;

public class VM {
  private int number;

  public VM(int num) {
    this.number = num;
  }

  public void loadDataToMemory() {
    RM.getInstance().loadUserData(this.number);
  }

  public Boolean test(){
    int pi = RM.getInstance().getPI() - '0';
    int si = RM.getInstance().getSI() - '0';
    int ti = Mapper.toInt("" + RM.getInstance().getTI());
    return !((pi + si ) > 0 || ti == 0);
  }

  // prints commands in the code segment, later should set its IP and move along it
  public String getNextCommand() {
    char[] ip = RM.getInstance().getIP();
    int pointer = Mapper.toInt("" + ip[0] + ip[1]) * Config.ram_word_size;
    String command = "";
    if (pointer < Config.VM_ram_size) {
      int realPointer = RM.getInstance().getVmPointer(pointer, this.number);

      int realBlock = realPointer / (Config.block_size * Config.ram_word_size);
      int realWord = realPointer % (Config.block_size * Config.ram_word_size) / 4;

      int vmBlock = pointer / (Config.block_size * Config.ram_word_size);
      int vmWord = pointer % (Config.block_size * Config.ram_word_size) / 4;

      String word = RM.getInstance().getWord(realBlock, realWord);

      if (!word.trim().isEmpty())
        command = (
            Mapper.toHex(vmBlock)
                + " "
                + Mapper.toHex(vmWord)
                + " "
                + RM.getInstance().getWord(realBlock, realWord));

      int newPointer = pointer + Config.ram_word_size >= Config.VM_ram_size ? 0: pointer + Config.ram_word_size;
      String newPointerHex = Mapper.toHex(newPointer / 4);
      char[] newIP = new char[]{' ',' '};
      newIP[0] = newPointerHex.charAt(0);
      if (newPointerHex.length() > 1){
        newIP[1] = newPointerHex.charAt(1);
      }
      RM.getInstance().setIP(newIP);
    }
    return command;
  }

  private void printHelper(int[] blocks){
    System.out.println("-------------------------");
    for(int block : blocks){
      for(int i = 0; i < Config.block_size; ++i){
        int pointer = block * Config.block_size * Config.ram_word_size + i * Config.ram_word_size;
        int realPointer = RM.getInstance().getVmPointer(pointer, this.number);
        int realBlock = realPointer / (Config.block_size * Config.ram_word_size);
        int realWord = realPointer % (Config.block_size * Config.ram_word_size) / Config.ram_word_size;
        String word = RM.getInstance().getWord(realBlock, realWord);
        if(!word.trim().isEmpty()){
          System.out.println(Mapper.toHex(block) + " " + Mapper.toHex(i) + " " + word);
        }
      }
    }
    System.out.println("-------------------------");
  }

  // Blocks [0; 3]
  private void printDS(){
    printHelper(new int[]{0,1,2,3});
  }

  // Block 4
  private void printParameters(){
    printHelper(new int[]{4});
  }

  // Blocks [5; 16]
  private void printCS(){
    printHelper(new int[]{5,6,7,8,9,10,11,12,13,14,15});
  }

  private void printPTR(){
    char[] ptr = RM.getInstance().getPTR();
    int block = Mapper.toInt("" + ptr[0] + ptr[1]);
    System.out.println("-------------------------");
    for(int i = 0; i < Config.block_size; ++i){
      System.out.println(Mapper.toHex(block) + " " + Mapper.toHex(i) + " " + RM.getInstance().getWord(block, i));
    }
    System.out.println("-------------------------");
  }

  // Helper function to get user input
  private int getUserOption(String[] options) {
    Scanner scanner = new Scanner(System.in);
    for (String option : options) {
      System.out.println(option);
    }
    while (true) {
      System.out.print("Enter your option: ");
      if (scanner.hasNextInt()) {
        int choice = scanner.nextInt();
        if (choice >= 1 && choice <= options.length) {
          return choice;
        }
      } else {
        scanner.next();
      }
      System.out.println("Invalid option, try again.");
    }
  }

  public void getMenu() {
    String[] mainOptions = {"1. Show data segment", "2. Show parameters" , "3. Show code segment" , "4. Show PTR", "5. Show all RM registers", "6. Continue", "7. Turn off VM"};
    while(true) {
      int userOption = getUserOption(mainOptions);
      switch (userOption) {
        case 1:
          printDS();
          break;
        case 2:
          printParameters();
          break;
        case 3:
          printCS();
          break;
        case 4:
          printPTR();
          break;
        case 5:
          RM.getInstance().printAllCPURegisters();
          break;
        case 6:
          return;
        case 7:
          RM.getInstance().setSI('3');
          return;
      }
    }
  }

}
