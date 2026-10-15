package org.example;

public class Main {
  public static void main(String[] args) {
    RM realMachine = RM.getInstance();
    while (realMachine.test()) {
      realMachine.getMenu();
    }
  }
}
