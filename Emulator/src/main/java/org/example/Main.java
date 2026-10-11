package org.example;

public class Main {
  public static void main(String[] args) {
    RM realMachine = new RM();

    while (realMachine.test()) {
      realMachine.getMenu();
    }
  }
}
