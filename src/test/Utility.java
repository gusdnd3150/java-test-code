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

}
