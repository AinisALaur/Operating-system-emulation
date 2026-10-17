package org.example;

import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    RM realMachine = RM.getInstance();
    String fileName = realMachine.getMenu();
    realMachine.setFileName(fileName);
    realMachine.loadToSupervisor();
    VM vm = realMachine.createVM();
    realMachine.setIP(new char[]{'5','0'}); // Points to data segment by default
    vm.loadDataToMemory();
    Scanner scanner = new Scanner(System.in);

    while(vm.test()){
      String command = vm.getNextCommand();
      if(!command.isEmpty()){
        System.out.println("Command: " + command);
      }
      vm.getMenu();
    }
  }
}
