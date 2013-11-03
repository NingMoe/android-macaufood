package com.cycon.macaufood.bean;

public enum ImageType {
	RECOMMEND("Recommend"), COUPON("Coupon"), REGULAR("Regular"), INTRO("Intro"), INFO("Info"), FOODNEWS("FoodNews"), ADV("Adv"), FRONTPAGE("FrontPage"), MAINCOUPON("MainCoupon"), PHOTOSHARE_HOT("PhotoShareHot"),
	/*this line is for non cache image*/ FOODNEWSIMAGE("FoodNewsImage"), PSLOCALAVATAR("PsLocalAvatar");
	
	private final String value;
	
	ImageType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
