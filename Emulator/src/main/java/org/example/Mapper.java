package org.example;

public class Mapper {
    public static String toHex(int number){
        return Integer.toHexString(number).toUpperCase();
    }

    public static int toInt(String hex){
        hex = hex.trim().toUpperCase();
        int base = 1;
        int sum = 0;
        for(int i = hex.length() - 1; i >= 0; --i){
            char c = hex.charAt(i);
            if (c >= '0' && c <= '9'){
                sum += (c - '0') * base;
            }else{
                switch (c){
                    case 'A':
                        sum += 10 * base;
                        break;
                    case 'B':
                        sum += 11 * base;
                        break;
                    case 'C':
                        sum += 12 * base;
                        break;
                    case 'D':
                        sum += 13 * base;
                        break;
                    case 'E':
                        sum += 14 * base;
                        break;
                    case 'F':
                        sum += 15 * base;
                        break;
                }
            }
            base *= 16;
        }
        return sum;
    }
}
