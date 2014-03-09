package com.cycon.macaufood.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.util.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;

public class WeiboPostApi extends AbsOpenAPI {
	
	private final String UPLOAD_URL = "https://upload.api.weibo.com/2/statuses/upload.json";

	public WeiboPostApi(Oauth2AccessToken accessToken) {
		super(accessToken);
		// TODO Auto-generated constructor stub
	}

    public void post(String status, String path) {
        if (mAccessToken != null && mAccessToken.isSessionValid()) {
            WeiboParameters params = new WeiboParameters();
            params.add("access_token", mAccessToken.getToken());
            try {
				params.add("status", URLEncoder.encode(status, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
            params.add("pic", path);
            request(UPLOAD_URL, params, "POST", null);
        } else {
        	MFLog.e("WeiboPostApi", "invite error");
        }
    }
	
}
