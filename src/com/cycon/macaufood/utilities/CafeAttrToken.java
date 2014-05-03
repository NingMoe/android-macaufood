package com.cycon.macaufood.utilities;

public enum CafeAttrToken {
	ID("id"),
	NAME("name"),
	PHONE("phone"),
	DISTRICT("district"),
	ADDRESS("address"),
	WEBSITE("website"),
	COORDX("coordx"),
	COORDY("coordy"),
	OPENHOURS("openhours"),
	DESCRIPTION("description"),
	MESSAGE("message"),
	TYPE0("type0"),
	TYPE1("type1"),
	TYPE2("type2"),
	OPTION_PHONEORDER("option_phoneorder"),
	OPTION_BOOKING("option_booking"),
	OPTION_NIGHT("option_night"),
	OPTION_CALL("option_call"),
	OPTION_BUFFET("option_buffet"),
	OPTION_BANQUET("option_banquet"),
	OPTION_PHOTO("option_photo"),
	OPTION_INTRO("option_intro"),
	OPTION_MENU("option_menu"),
	OPTION_RECOMMEND("option_recommend"),
	MENUID("menuid"),
	INTROID("introid"),
	INTROPAGE("intropage"),
	RECOMMENDID("recommendid"),
	RECOMMENDPAGE("recommendpage"),
	MENUPAGE("menupage"),
	OPTION_CONFIRM("option_confirm"),
	PAYMENT("payment"),
	STATUS("status"),
	TIMEADDED("timeadded"),
	
	OPTION_WIFI("option_wifi"),
	OPTION_PARKING("option_parking"),
	BRANCH("branch"),
	PRIORITY("priority"),
	OPTION_MACAUPASS("option_macaupass");
	
	private final String value;
	
	CafeAttrToken(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
}