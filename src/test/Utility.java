package test;

import java.util.Arrays;

public class Utility {

	
	public static String centerPad(String str, int size, char padChar) {
	    if (str == null) {
	        str = "";
	    }
	    if (size <= str.length()) return str;

	    int totalPadding = size - str.length();
	    int leftPadding  = totalPadding / 2;
	    int rightPadding = totalPadding - leftPadding;

	    return repeat(padChar, leftPadding) + str + repeat(padChar, rightPadding);
	}

	private static String repeat(char c, int count) {
	    char[] arr = new char[count];
	    Arrays.fill(arr, c);
	    return new String(arr);
	}
	
	
	public static String rpad(String str, int size, char padChar) {
	    // 1️⃣ null 안전 처리
	    if (str == null) {
	        str = "";
	    }

	    int len = str.length();
	    if (len >= size) return str;

	    StringBuilder sb = new StringBuilder(str);
	    for (int i = len; i < size; i++) {
	        sb.append(padChar);
	    }
	    return sb.toString();
	}
	
	
	
	public static boolean isBitSet(byte value, int pos) {
		return isBitSet(value, pos, "R");
	}

	/*
     * @param value 검사할 byte
     * @param pos 비트 위치 (0~7)
     * @param direction from LEFT 또는 RIGHT
     * @return 해당 비트가 1이면 true
     */
    public static boolean isBitSet(byte value, int pos, String direction) {
        if (pos < 0 || pos > 7) {
            throw new IllegalArgumentException("pos must be between 0 and 7");
        }
        int bitPos;
        if ("L".equalsIgnoreCase(direction)) {
            bitPos = 7 - pos;
        } else if ("R".equalsIgnoreCase(direction)) {
            bitPos = pos;
        } else {
            throw new IllegalArgumentException("Invalid direction: " + direction);
        }
        return ((value & 0xFF) & (1 << bitPos)) != 0;
    }
    
    // "0020" (0x0020) 문자열 데이터를 byte[] 로 반환한다.
    public static byte[] hexStringToBytes(String hex) {
    	int word = Integer.parseInt(hex, 16); // 0x0020, 십진수 32
    	byte[] bytes = {(byte) (word >>> 8), (byte) word};
    	// int bit5 = (word >>> 5) & 1; // 결과: 1
    	return bytes;
    }
}
