package com.cycon.macaufood.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.cycon.macaufood.utilities.MFLog;

import com.cycon.macaufood.bean.Cafe;

public class AdvancedSearchHelper {

	public static void search(int regionIndex, int dishesId, int servicesIndex, ArrayList<Cafe> storeList) {

		storeList.clear();
		
		ArrayList<Cafe> priorityList = new ArrayList<Cafe>(); 
		
		for (Cafe cafe : MFConfig.getInstance().getCafeLists()) {
			if (cafe.getStatus().equals("0")) continue;
			boolean matchDistrict;
			boolean matchDishes;
			boolean matchServices;
			
			
			if (regionIndex == 0 || regionIndex == Integer.parseInt(cafe.getDistrict())) {
				matchDistrict = true;
			} else {
				matchDistrict = false;
			}
			
			if (dishesId == 0 || dishesId == Integer.parseInt(cafe.getType0())
					|| dishesId == Integer.parseInt(cafe.getType1()) 
					|| dishesId == Integer.parseInt(cafe.getType2())) {
				matchDishes = true;
			} else {
				matchDishes = false;
			}
			
			switch(servicesIndex) {
				case 0:
					matchServices = true;
					break;
				case 1:
					if (cafe.getOption_macaupass().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 2:
					if (cafe.getOption_phoneorder().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 3:
					if (cafe.getOption_booking().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 4:
					if (cafe.getOption_night().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 5:
					if (cafe.getOption_call().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 6:
					if (cafe.getOption_buffet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 7:
					if (cafe.getOption_banquet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 8:
					if (cafe.getOption_wifi().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 9:
					if (cafe.getOption_parking().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				default:
					matchServices = false;
			}
			
			
			if (matchDishes && matchDistrict && matchServices) {
				if (cafe.getPriority().equals("0")) {
					storeList.add(cafe);
				} else {
					priorityList.add(cafe);
				}
			}
			
		}
		
		Collections.sort(priorityList, new Comparator<Cafe>() {
			public int compare(Cafe cafe1, Cafe cafe2) {
				
				return Integer.parseInt(cafe2.getPriority()) - Integer.parseInt(cafe1.getPriority());
			};
			
		});
		
		storeList.addAll(0, priorityList);
	}
	
}
