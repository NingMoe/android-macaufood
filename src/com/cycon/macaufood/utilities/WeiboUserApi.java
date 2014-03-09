package com.cycon.macaufood.utilities;

import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.AbsOpenAPI;
import com.sina.weibo.sdk.utils.LogUtil;

public class WeiboUserApi extends AbsOpenAPI {
	
	private final String USER_URL = "https://api.weibo.com/2/users/show.json";

	public WeiboUserApi(Oauth2AccessToken accessToken) {
		super(accessToken);
		// TODO Auto-generated constructor stub
	}

    public void getUserInfo(RequestListener listener) {
        if (mAccessToken != null && mAccessToken.isSessionValid() && listener != null) {
            WeiboParameters params = new WeiboParameters();
            params.add("access_token", mAccessToken.getToken());
            params.add("uid", mAccessToken.getUid());
            request(USER_URL, params, "GET", listener);
        } else {

        	MFLog.e("WeiboUserApi", "invite error");
        }
    }
	
}
