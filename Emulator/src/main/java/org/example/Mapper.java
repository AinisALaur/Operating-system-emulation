package org.example;

// TODO: now printing in getMenu() we get 4 hex characters instead of 1
public class Mapper {
  public static String toHex(int number) {
    if (number < Config.min_number_in_register || number > Config.max_number_in_register) {
      throw new IllegalArgumentException("Number out of range");
    }

    if (number < 0) {
      return String.format("-%03X", -number);
    }

    return String.format("%04X", number);
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

  public static String formatDisplayHex(int value) {
    return Integer.toHexString(value).toUpperCase();
  }
}
