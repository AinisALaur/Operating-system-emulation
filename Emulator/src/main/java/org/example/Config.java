package org.example;

public class Config {
  public static final int ram_size = 1072;
  public static final int ram_word_size = 4;
  public static final int block_size = 16;
  public static final int block_amount = 67;
  public static final int user_block_amount = 51;
  public static final int supervisor_block_amount = 16;
  public static final int supervisor_first_block = 51;
  public static final String outer_disk_file_name = "src/main/java/org/example/HDD.txt";
  public static final String program_start = "$RJG";
  public static final String program_end = "$END";
  public static final int code_segment_start = 320;
  public static final int VM_ram_size = 1024;
  public static final int VM_amount = 3;
  public static final int max_number_in_register = 65535;
  public static final int min_number_in_register = -4095;
}
