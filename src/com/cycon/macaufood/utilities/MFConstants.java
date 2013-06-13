package com.cycon.macaufood.utilities;

public class MFConstants {

	public static String[] regionNames = { "全部", "青洲區", "筷子基區", "台山區(台山-關閘)",
			"黑沙灣區(祐漢-黑沙灣)", "高士德區(紅街市-雅廉訪-高士德)", "新橋區(三盞燈-永樂-白鴿巢)",
			"望廈區(望廈-觀音堂)", "東望洋區(荷蘭園-水坑尾-松山)", "中區(新馬路-大三巴)", "新口岸區(葡京-港澳碼頭)",
			"新口岸區(皇朝)", "沙梨頭區(眾龍軒-十六號碼頭)", "下環區(下環街-媽閣)", "南西灣區(南西灣湖-觀光塔)",
			"氹仔區", "路環區" };

	public static String[] dishesType = { "全部", "澳門特色", "茶餐廳", "麵食", "粥麵",
			"燒味", "火鍋", "酒家", "中國菜", "海鮮小炒", "燒烤", "煲仔飯", "日本菜", "韓國菜", "西餐",
			"葡國菜", "法國菜", "意大利菜", "快餐", "素食", "台灣菜", "泰國菜", "越南菜", "緬甸菜",
			"東南亞菜", "各式美食", "休閒Cafe", "小食", "甜品", "養生滋補", "私房菜", "飲品", "手信",
			"餅店", "酒舖", "酒吧", "酒店餐廳", "酒店宴會", "商場餐廳" };

	public static int[] dishesId = { 0, 39, 1, 2, 3, 4, 6, 9, 10, 8, 7, 5, 13,
			14, 11, 12, 20, 21, 27, 32, 15, 16, 17, 18, 19, 28, 25, 22, 24, 29,
			41, 23, 31, 30, 40, 26, 33, 34, 35 };

	public static String[] serviceType = { "全部", "外送", "訂枱", "宵夜", "到會",
			"自助任食", "飲宴", "WIFI", "泊車" };

	public static final String RECOMMEND_URL = "http://www.cycon.com.mo/xml_caferecommend_new.php?key=cafecafe";
	public static final String FOOD_NEWS_URL = "http://www.cycon.com.mo/xml_article.php?key=cafecafe";
	public static final String NORMAL_COUPON_URL = "http://www.cycon.com.mo/xml_cafecoupon_new.php?key=cafecafe&type=0";
	public static final String CREDIT_COUPON_URL = "http://www.cycon.com.mo/xml_cafecoupon_new.php?key=cafecafe&type=1";
	public static final String VIP_COUPON_URL = "http://www.cycon.com.mo/xml_cafecoupon_new.php?key=cafecafe&type=2";

	public static final String RECOMMEND_XML_FILE_NAME = "recommend_parsed_xml";
	public static final String NORMAL_COUPON_XML_FILE_NAME = "normal_coupon_parsed_xml";
	public static final String CREDIT_COUPON_XML_FILE_NAME = "credit_coupon_parsed_xml";
	public static final String VIP_COUPON_XML_FILE_NAME = "vip_coupon_parsed_xml";
	public static final String FOODNEWS_XML_FILE_NAME = "foodnews_parsed_xml";

	public static final String TIME_STAMP_PREF_KEY = "listTimeStamp";
	public static final String SHOW_DISCLAIMER_PREF_KEY = "disclaimerDialog";
}
