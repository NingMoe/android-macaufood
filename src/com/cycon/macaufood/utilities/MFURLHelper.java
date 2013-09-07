package com.cycon.macaufood.utilities;

import com.cycon.macaufood.bean.ImageType;

public class MFURLHelper {
	
	private static final String APP_IMAGE_URL = "http://www.cycon.com.mo/appimages/";

	public static String getImageUrl(ImageType imageType, String id) {
		String dir = null;
		if (imageType == ImageType.RECOMMEND) dir = "recommend_new";
		else if (imageType == ImageType.COUPON) dir = "coupon_new";
		else if (imageType == ImageType.REGULAR) dir = "cafephoto";
		else if (imageType == ImageType.INTRO) dir = "intro";
		else if (imageType == ImageType.INFO) dir = "intro";
		else if (imageType == ImageType.FOODNEWS) dir = "article_thumbnail";
		else if (imageType == ImageType.ADV) dir = "adv_rotate_banner";
		else if (imageType == ImageType.FRONTPAGE) dir = "front_page";
		else if (imageType == ImageType.COUPONCLICK) dir = "coupon_click";
		
		return APP_IMAGE_URL + dir + "/" + id + ".jpg";
	}
	
}
