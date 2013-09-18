package com.cycon.macaufood.bean;

import java.io.Serializable;

import com.cycon.macaufood.utilities.MFLog;

import com.cycon.macaufood.utilities.CafeAttrToken;


public class Cafe implements Comparable<Cafe>, Serializable{
	private String id;
	private String name;
	private String phone = "";
	private String district;
	private String address = "";
	private String website = "";
	private String coordx = "0";
	private String coordy = "0";
	private String openhours = "";
	private String description = "";
	private String message= "";
	private String type0;
	private String type1 = "0";
	private String type2 = "0";
	private String option_phoneorder = "0";
	private String option_booking = "0";
	private String option_night = "0";
	private String option_call = "0"; //option_party
	private String option_buffet = "0";
	private String option_banquet = "0";
	private String option_photo = "0"; //if photo exists in server
	private String option_intro = "0"; 
	private String option_menu = "0";
	private String option_recommend = "0";
	private String menuid = "0";
	private String introid = "0";
	private String intropage = "0";
	private String recommendid = "0";
	private String recommendpage = "0";
	private String menupage = "0";
	private String option_confirm = "0";
	private String payment = "0";
	private String status = "1";
	private String timeadded;
	
	private String option_wifi = "0";
	private String option_parking = "0";
	private String branch = "0";
	private String priority = "0";
	
	private double distance;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getCoordx() {
		return coordx;
	}
	public void setCoordx(String coordx) {
		this.coordx = coordx;
	}
	public String getCoordy() {
		return coordy;
	}
	public void setCoordy(String coordy) {
		this.coordy = coordy;
	}
	public String getOpenhours() {
		return openhours;
	}
	public void setOpenhours(String openhours) {
		this.openhours = openhours;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getType0() {
		return type0;
	}
	public void setType0(String type0) {
		this.type0 = type0;
	}
	public String getType1() {
		return type1;
	}
	public void setType1(String type1) {
		this.type1 = type1;
	}
	public String getType2() {
		return type2;
	}
	public void setType2(String type2) {
		this.type2 = type2;
	}
	public String getOption_phoneorder() {
		return option_phoneorder;
	}
	public void setOption_phoneorder(String option_phoneorder) {
		this.option_phoneorder = option_phoneorder;
	}
	public String getOption_booking() {
		return option_booking;
	}
	public void setOption_booking(String option_booking) {
		this.option_booking = option_booking;
	}
	public String getOption_night() {
		return option_night;
	}
	public void setOption_night(String option_night) {
		this.option_night = option_night;
	}
	public String getOption_call() {
		return option_call;
	}
	public void setOption_call(String option_call) {
		this.option_call = option_call;
	}
	public String getOption_buffet() {
		return option_buffet;
	}
	public void setOption_buffet(String option_buffet) {
		this.option_buffet = option_buffet;
	}
	public String getOption_banquet() {
		return option_banquet;
	}
	public void setOption_banquet(String option_banquet) {
		this.option_banquet = option_banquet;
	}
	public String getOption_photo() {
		return option_photo;
	}
	public void setOption_photo(String option_photo) {
		this.option_photo = option_photo;
	}
	public String getOption_intro() {
		return option_intro;
	}
	public void setOption_intro(String option_intro) {
		this.option_intro = option_intro;
	}
	public String getOption_menu() {
		return option_menu;
	}
	public void setOption_menu(String option_menu) {
		this.option_menu = option_menu;
	}
	public String getOption_recommend() {
		return option_recommend;
	}
	public void setOption_recommend(String option_recommend) {
		this.option_recommend = option_recommend;
	}
	public String getMenuid() {
		return menuid;
	}
	public void setMenuid(String menuid) {
		this.menuid = menuid;
		if (!menuid.equals("0")) option_menu = "1";
	}
	public String getIntroid() {
		return introid;
	}
	public void setIntroid(String introid) {
		this.introid = introid;
		if (!introid.equals("0")) option_intro = "1";
	}
	public String getIntropage() {
		return intropage;
	}
	public void setIntropage(String intropage) {
		this.intropage = intropage;
	}
	public String getRecommendid() {
		return recommendid;
	}
	public void setRecommendid(String recommendid) {
		this.recommendid = recommendid;
		if (!recommendid.equals("0")) option_recommend = "1";
	}
	public String getRecommendpage() {
		return recommendpage;
	}
	public void setRecommendpage(String recommendpage) {
		this.recommendpage = recommendpage;
	}
	public String getMenupage() {
		return menupage;
	}
	public void setMenupage(String menupage) {
		this.menupage = menupage;
	}
	public String getOption_confirm() {
		return option_confirm;
	}
	public void setOption_confirm(String option_confirm) {
		this.option_confirm = option_confirm;
	}
	public String getPayment() {
		return payment;
	}
	public void setPayment(String payment) {
		this.payment = payment;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTimeadded() {
		return timeadded;
	}
	public void setTimeadded(String timeadded) {
		this.timeadded = timeadded;
	}
	public String getOption_wifi() {
		return option_wifi;
	}
	public void setOption_wifi(String option_wifi) {
		this.option_wifi = option_wifi;
	}
	public String getOption_parking() {
		return option_parking;
	}
	public void setOption_parking(String option_parking) {
		this.option_parking = option_parking;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public void setAnyField(String field, String value) throws Exception {
		if (field.equals(CafeAttrToken.ID.getValue())) id = value;
		else if (field.equals(CafeAttrToken.NAME.getValue())) name = value;
		else if (field.equals(CafeAttrToken.PHONE.getValue())) phone = value;
		else if (field.equals(CafeAttrToken.DISTRICT.getValue())) district = value;
		else if (field.equals(CafeAttrToken.ADDRESS.getValue())) address = value;
		else if (field.equals(CafeAttrToken.WEBSITE.getValue())) website = value;
		else if (field.equals(CafeAttrToken.COORDX.getValue())) coordx = value;
		else if (field.equals(CafeAttrToken.COORDY.getValue())) coordy = value;
		else if (field.equals(CafeAttrToken.OPENHOURS.getValue())) openhours = value;
		else if (field.equals(CafeAttrToken.DESCRIPTION.getValue())) description = value;
		else if (field.equals(CafeAttrToken.MESSAGE.getValue())) message = value;
		else if (field.equals(CafeAttrToken.TYPE0.getValue())) type0 = value;
		else if (field.equals(CafeAttrToken.TYPE1.getValue())) type1 = value;
		else if (field.equals(CafeAttrToken.TYPE2.getValue())) type2 = value;
		else if (field.equals(CafeAttrToken.OPTION_PHONEORDER.getValue())) option_phoneorder = value;
		else if (field.equals(CafeAttrToken.OPTION_BOOKING.getValue())) option_booking = value;
		else if (field.equals(CafeAttrToken.OPTION_NIGHT.getValue())) option_night = value;
		else if (field.equals(CafeAttrToken.OPTION_CALL.getValue())) option_call = value;
		else if (field.equals(CafeAttrToken.OPTION_BUFFET.getValue())) option_buffet = value;
		else if (field.equals(CafeAttrToken.OPTION_BANQUET.getValue())) option_banquet = value;
		else if (field.equals(CafeAttrToken.OPTION_PHOTO.getValue())) option_photo = value;
		else if (field.equals(CafeAttrToken.OPTION_INTRO.getValue())) option_intro = value;
		else if (field.equals(CafeAttrToken.OPTION_MENU.getValue())) option_menu = value;
		else if (field.equals(CafeAttrToken.OPTION_RECOMMEND.getValue())) option_recommend = value;
		else if (field.equals(CafeAttrToken.MENUID.getValue())) { menuid = value; option_menu = "1";}
		else if (field.equals(CafeAttrToken.INTROID.getValue())) { introid = value; option_intro = "1";}
		else if (field.equals(CafeAttrToken.INTROPAGE.getValue())) intropage = value;
		else if (field.equals(CafeAttrToken.RECOMMENDID.getValue())) { recommendid = value; option_recommend = "1"; }
		else if (field.equals(CafeAttrToken.RECOMMENDPAGE.getValue())) recommendpage = value;
		else if (field.equals(CafeAttrToken.MENUPAGE.getValue())) menupage = value;
		else if (field.equals(CafeAttrToken.OPTION_CONFIRM.getValue())) option_confirm = value;
		else if (field.equals(CafeAttrToken.PAYMENT.getValue())) payment = value;
		else if (field.equals(CafeAttrToken.STATUS.getValue())) status = value;
		else if (field.equals(CafeAttrToken.TIMEADDED.getValue())) timeadded = value;
		else if (field.equals(CafeAttrToken.OPTION_WIFI.getValue())) option_wifi = value;
		else if (field.equals(CafeAttrToken.OPTION_PARKING.getValue())) option_parking = value;
		else if (field.equals(CafeAttrToken.PRIORITY.getValue())) priority = value;
		else if (field.equals(CafeAttrToken.BRANCH.getValue())) branch = value;
		else {MFLog.e("Cafe", "no field" + field); throw new Exception("No such field");}
	}
	
	public String getAnyField(String field) throws Exception {
		if (field.equals(CafeAttrToken.ID.getValue())) return id;
		else if (field.equals(CafeAttrToken.NAME.getValue())) return name;
		else if (field.equals(CafeAttrToken.PHONE.getValue())) return phone;
		else if (field.equals(CafeAttrToken.DISTRICT.getValue())) return district;
		else if (field.equals(CafeAttrToken.ADDRESS.getValue())) return address;
		else if (field.equals(CafeAttrToken.WEBSITE.getValue())) return website;
		else if (field.equals(CafeAttrToken.COORDX.getValue())) return coordx;
		else if (field.equals(CafeAttrToken.COORDY.getValue())) return coordy;
		else if (field.equals(CafeAttrToken.OPENHOURS.getValue())) return openhours;
		else if (field.equals(CafeAttrToken.DESCRIPTION.getValue())) return description;
		else if (field.equals(CafeAttrToken.MESSAGE.getValue())) return message;
		else if (field.equals(CafeAttrToken.TYPE0.getValue())) return type0;
		else if (field.equals(CafeAttrToken.TYPE1.getValue())) return type1;
		else if (field.equals(CafeAttrToken.TYPE2.getValue())) return type2;
		else if (field.equals(CafeAttrToken.OPTION_PHONEORDER.getValue())) return option_phoneorder;
		else if (field.equals(CafeAttrToken.OPTION_BOOKING.getValue())) return option_booking;
		else if (field.equals(CafeAttrToken.OPTION_NIGHT.getValue())) return option_night;
		else if (field.equals(CafeAttrToken.OPTION_CALL.getValue())) return option_call;
		else if (field.equals(CafeAttrToken.OPTION_BUFFET.getValue())) return option_buffet;
		else if (field.equals(CafeAttrToken.OPTION_BANQUET.getValue())) return option_banquet;
		else if (field.equals(CafeAttrToken.OPTION_PHOTO.getValue())) return option_photo;
		else if (field.equals(CafeAttrToken.OPTION_INTRO.getValue())) return option_intro;
		else if (field.equals(CafeAttrToken.OPTION_MENU.getValue())) return option_menu;
		else if (field.equals(CafeAttrToken.OPTION_RECOMMEND.getValue())) return option_recommend;
		else if (field.equals(CafeAttrToken.MENUID.getValue())) return menuid;
		else if (field.equals(CafeAttrToken.INTROID.getValue())) return introid;
		else if (field.equals(CafeAttrToken.INTROPAGE.getValue())) return intropage;
		else if (field.equals(CafeAttrToken.RECOMMENDID.getValue())) return recommendid;
		else if (field.equals(CafeAttrToken.RECOMMENDPAGE.getValue())) return recommendpage;
		else if (field.equals(CafeAttrToken.MENUPAGE.getValue())) return menupage;
		else if (field.equals(CafeAttrToken.OPTION_CONFIRM.getValue())) return option_confirm;
		else if (field.equals(CafeAttrToken.PAYMENT.getValue())) return payment;
		else if (field.equals(CafeAttrToken.STATUS.getValue())) return status;
		else if (field.equals(CafeAttrToken.TIMEADDED.getValue())) return timeadded;
		else if (field.equals(CafeAttrToken.OPTION_WIFI.getValue())) return option_wifi;
		else if (field.equals(CafeAttrToken.OPTION_PARKING.getValue())) return option_parking;
		else if (field.equals(CafeAttrToken.PRIORITY.getValue())) return priority;
		else if (field.equals(CafeAttrToken.BRANCH.getValue())) return branch;
		else {MFLog.e("Cafe", "no field" + field); throw new Exception("No such field");}
	}
	
	public int compareTo(Cafe another) {
		if (distance < another.distance) return -1;
		else if (distance > another.distance) return 1;
		return 0;
	}
	
//	public void setHashMap(HashMap<String, PListObject> map) {
//		id = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.ID.getValue())).getValue();
//		name = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.NAME.getValue())).getValue();
//		phone = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.PHONE.getValue())).getValue();
//		district = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.DISTRICT.getValue())).getValue();
//		address = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.ADDRESS.getValue())).getValue();
//		website = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.WEBSITE.getValue())).getValue();
//		coordx = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.COORDX.getValue())).getValue();
//		coordy = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.COORDY.getValue())).getValue();
//		openhours = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.OPENHOURS.getValue())).getValue();
//		description = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.DESCRIPTION.getValue())).getValue();
//		message = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.MESSAGE.getValue())).getValue();
//		type0 = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.TYPE0.getValue())).getValue();
//		type1 = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.TYPE1.getValue())).getValue();
//		type2 = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.TYPE2.getValue())).getValue();
//		option_phoneorder = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_PHONEORDER.getValue())).getValue();
//		option_booking = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_BOOOKING.getValue())).getValue();
//		option_night = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_NIGHT.getValue())).getValue();
//		option_call = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_CALL.getValue())).getValue();
//		option_buffet = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_BUFFET.getValue())).getValue();
//		option_banquet = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_BANQUET.getValue())).getValue();
//		option_photo = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_PHOTO.getValue())).getValue();
//		option_intro = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_INTRO.getValue())).getValue();
//		option_menu = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_MENU.getValue())).getValue();
//		menuid = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.MENUID.getValue())).getValue();
//		introid = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.INTROID.getValue())).getValue();
//		intropage = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.INTROPAGE.getValue())).getValue();
//		recommendid = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.RECOMMENDID.getValue())).getValue();
//		recommendpage = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.RECOMMENDPAGE.getValue())).getValue();
//		menupage = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.MENUPAGE.getValue())).getValue();
//		option_confirm = ((com.longevitysoft.android.xml.plist.domain.Integer) map.get(CafeAttrToken.OPTION_CONFIRM.getValue())).getValue();
//		status = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.STATUS.getValue())).getValue();
//		timeadded = ((com.longevitysoft.android.xml.plist.domain.String) map.get(CafeAttrToken.TIMEADDED.getValue())).getValue();
//	}
	
}

