package org.example;

public class Mapper {
  public static String toHex(int number) {
    if (number < 0) {
      throw new IllegalArgumentException("Number must not be negative");
    }
    return Integer.toHexString(number).toUpperCase();
  }

  public static int toInt(String hex) {
    if (hex == null || hex.isBlank()) {
      throw new IllegalArgumentException("Hex value must not be null or blank");
    }

    try {
      return Integer.parseInt(hex.trim(), 16);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid hexadecimal value: " + hex, e);
    }
  }
}
