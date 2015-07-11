package com.cycon.macaufood.utilities;

import java.util.ArrayList;

public class PhoneUtils {

	public static ArrayList<String> getPhoneStr(String str) {
		ArrayList<String> phoneStrs = new ArrayList<String>(2);
		
		if (str.contains(",")) {
			String[] numbers = str.split(",");
			String number1 = numbers[0].trim();
			if (checkValid(number1)) phoneStrs.add(number1);
			String number2 = numbers[1].trim();
			if (checkValid(number2)) phoneStrs.add(number2);
		} else {
			String number1 = str.trim();
			if (checkValid(number1)) phoneStrs.add(number1);
		}
		return phoneStrs;
	}
	
	private static boolean checkValid(String str) {
		if (str.contains("-") && str.length() == 9 || !str.contains("-") && str.length() == 8) {
			return checkIfAllNumbers(str);
		} else {
			return false;
		}
	}
	
	private static boolean checkIfAllNumbers(String str) {
		try {
			Integer.parseInt(str.replace("-", ""));
			return true;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}

}

