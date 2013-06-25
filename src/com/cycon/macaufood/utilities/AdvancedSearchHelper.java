package com.cycon.macaufood.utilities;

import java.util.ArrayList;

import com.cycon.macaufood.bean.Cafe;

public class AdvancedSearchHelper {

	public static void search(int regionIndex, int dishesId, int servicesIndex) {

		MFConfig.getInstance().getSearchResultList().clear();
		
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
					if (cafe.getOption_phoneorder().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 2:
					if (cafe.getOption_booking().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 3:
					if (cafe.getOption_night().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 4:
					if (cafe.getOption_call().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 5:
					if (cafe.getOption_buffet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 6:
					if (cafe.getOption_banquet().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 7:
					if (cafe.getOption_wifi().equals("1")) {
						matchServices = true;
					} else {
						matchServices = false;
					}
					break;
				case 8:
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
					MFConfig.getInstance().getSearchResultList().add(cafe);
				} else {
					int priority = Integer.parseInt(cafe.getPriority());
					if (priorityList.size() == 0) {
						priorityList.add(cafe);
					} else {
						boolean added = false;
						for (int i = 0; i < priorityList.size(); i++) {
							if (Integer.parseInt(priorityList.get(i).getPriority())
									< priority) {
								priorityList.add(i, cafe);
								added = true;
								break;
							}
						}
						if (!added) {
							priorityList.add(cafe);
						}
						
					}
				}
			}
			
			
			
		}
		

    	MFConfig.getInstance().getSearchResultList().clear();
    	MFConfig.getInstance().getSearchResultList().addAll(0, priorityList);
	}
	
}
