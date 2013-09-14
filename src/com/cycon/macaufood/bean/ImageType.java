package com.cycon.macaufood.bean;

public enum ImageType {
	RECOMMEND("Recommend"), COUPON("Coupon"), REGULAR("Regular"), INTRO("Intro"), INFO("Info"), FOODNEWS("FoodNews"), ADV("Adv"), FRONTPAGE("FrontPage"), COUPONCLICK("CouponClick"),
	/*this line is for non cache image*/ FOODNEWSIMAGE("FoodNewsImage");
	
	private final String value;
	
	ImageType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
