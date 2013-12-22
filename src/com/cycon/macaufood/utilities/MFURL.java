package com.cycon.macaufood.utilities;

import android.util.Log;

import com.cycon.macaufood.utilities.MFLog;

import com.cycon.macaufood.bean.ImageType;

public class MFURL {
	
	public static final String RECOMMEND_LIST = "http://www.cycon.com.mo/xml_caferecommend_new.php?key=cafecafe";
	public static final String FOOD_NEWS_LIST = "http://www.cycon.com.mo/xml_article.php?key=cafecafe";
	public static final String NORMAL_COUPON_LIST = "http://www.cycon.com.mo/xml_cafecoupon2_new.php?key=cafecafe&type=0";
	public static final String CREDIT_VIP_COUPON_LIST = "http://www.cycon.com.mo/xml_cafecoupon2_new.php?key=cafecafe&type=1";
	public static final String MAIN_COUPON = "http://www.cycon.com.mo/xml_cafecoupon2_new.php?key=cafecafe&type=2";
	public static final String SMALL_ADV = "http://www.cycon.com.mo/xml_adv.php?code=android-" + MFConfig.DEVICE_ID + "&type=s";
	public static final String BIG_ADV = "http://www.cycon.com.mo/xml_adv.php?code=android-" + MFConfig.DEVICE_ID + "&type=b";
	public static final String NEW_BIG_ADV = "http://www.cycon.com.mo/xml_adv2.php?code=android-" + MFConfig.DEVICE_ID + "&type=b";
	public static final String NEW_SMALL_ADV = "http://www.cycon.com.mo/xml_adv2.php?code=android-" + MFConfig.DEVICE_ID + "&type=s";
	public static final String FAVORITE_LOG = "http://www.cycon.com.mo/xml_favouritelog.php?key=cafecafe&udid=android-" + 
			MFConfig.DEVICE_ID + "&cafeid=";
	public static final String UPDATE_CAFE_LOG = "http://www.cycon.com.mo/xml_updatelogandroid.php?key=cafecafe&lastupdatetime=";
	public static final String CAFE_VERSION_UPDATE = "http://www.cycon.com.mo/cafe_version_update.txt";
	public static final String CAFE_DETAILS_LOG = "http://www.cycon.com.mo/xml_detaillog2.php?key=cafecafe&udid=android-";//more params behind
	public static final String SUBMIT_FEEDBACK = "http://www.cycon.com.mo/xml_submitmsg2.php?key=cafecafe&udid=android-";//more params behind
	public static final String FRONT_PAGE_TIME = "http://www.cycon.com.mo/xml_get_config.php?key=cafecafe&type=front_page";
	public static final String FRONT_PAGE_LINK_URL = "http://www.cycon.com.mo/xml_get_config.php?key=cafecafe&type=front_page_url";
	public static final String PHOTOSHARE_HOT_LIST = "http://www.cycon.com.mo/photo_xml_gethot2.php?key=photo123456&iphone5=1";
	public static final String PHOTOSHARE_HOT_IMAGE = "http://www.cycon.com.mo/displayimage.php?filename=";
	public static final String PHOTOSHARE_REGISTER = "http://www.cycon.com.mo/photo_xml_memberlogin_socialnetwork.php?key=photo123456&logintype=";
	public static final String PHOTOSHARE_FIND_FRIENDS = "http://www.cycon.com.mo/photo_xml_friends2.php?key=photo123456&memberid=";
	public static final String PHOTOSHARE_FOLLOW_FRIENDS = "http://www.cycon.com.mo/photo_xml_changefollow2.php?key=photo123456&leader=%s&follower=%s&tag=%d";
	public static final String PHOTOSHARE_SHOW_PHOTOS = "http://www.cycon.com.mo/photo_xml_showphotos.php?key=photo123456&onlyme=0&memberid=";
	public static final String PHOTOSHARE_LIKE = "http://www.cycon.com.mo/photo_xml_like.php?key=photo123456&memberid=%s&photoid=%s";
	public static final String PHOTOSHARE_UNLIKE = "http://www.cycon.com.mo/photo_xml_unlike.php?key=photo123456&memberid=%s&photoid=%s";
	public static final String PHOTOSHARE_COMMENT = "http://www.cycon.com.mo/photo_xml_addcomment.php?key=photo123456";
	
	private static final String APP_IMAGE = "http://www.cycon.com.mo/appimages/";
	private static final String INTRO_PAGE = "http://www.cycon.com.mo/detail_page.php?id=";
	private static final String INTRO_TEXT = "http://www.cycon.com.mo/detail_text.php?id=";
	public static final String MENU = "http://www.cycon.com.mo/xml_menu.php?id=%s&page=%d&udid=android-%s";
	public static final String CLICK_ADV = "http://www.cycon.com.mo/xml_advclick.php?id= %s&code=%s";

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
		else if (imageType == ImageType.MAINCOUPON) dir = "coupon_click";
		else if (imageType == ImageType.FOODNEWSIMAGE) dir = "article_content";
		else if (imageType == ImageType.PSLOCALAVATAR) dir = "pslocalavatar";
		else if (imageType == ImageType.PHOTOSHARE) return PHOTOSHARE_HOT_IMAGE + id;
		
		return APP_IMAGE + dir + "/" + id + ".jpg";
	}
	
	public static String getIntroPageUrl(String id) {
		return INTRO_PAGE + id;
	}
	
	public static String getIntroTextUrl(String id, int page) {
		return INTRO_TEXT + id + "&page=" + page;
	}
	
}
