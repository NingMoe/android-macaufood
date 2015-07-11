package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.cycon.macaufood.R;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.LogoutAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoginHelper {
	//this class cannnot be singleton
	private static final String TAG = "LoginHelper";
	
	//Weibo
	private static final String WEIBO_APP_KEY = "1904455053";
	public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
	public static final String SCOPE =
			"email,direct_messages_read,direct_messages_write,"
			+ "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
			+ "follow_app_official_microblog," + "invitation_write";
	
//	private boolean isFacebook;
	private Context mContext;
	private ProgressDialog pDialog;
	private Dialog mLoginDialog;
	private com.sina.weibo.sdk.widget.LoginoutButton mWeiboLoginButton;
	
    public enum PendingAction {
        NONE,
        FRIENDS,
        CAMERA,
        SETTINGS,
        LIKE, 
        COMMENT, 
        DELETE
    }

	public interface RegisterPSCallBack {
		void onCompleteRegistered(PendingAction pa);
		void onErrorRegistered();
	}
	
	
	public LoginHelper(Context context) {
		this.mContext = context;
	}
	
	public boolean isLogin() {
		return isFacebookLogin() || isWeiboLogin();
	}
	
	public boolean isFacebookLogin() {
		Session session = Session.getActiveSession();
		return session != null && session.isOpened();
	}
	
	public boolean isWeiboLogin() {
		Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
        return accessToken != null && accessToken.isSessionValid();
	}
	
//	public void loginFacebook(Session session, Activity activity, StatusCallback statusCallback) {
//	    if (!session.isOpened() && !session.isClosed()) {
//	        session.openForRead(new Session.OpenRequest(activity)
//	        .setPermissions(Arrays.asList("basic_info"))
//	            .setCallback(statusCallback));
//	    } else {
//	        Session.openActiveSession(activity, true, statusCallback);
//	    }
//	}
	
	public SsoHandler loginWeibo(Activity activity, WeiboAuthListener listener) {
		WeiboAuth authInfo = new WeiboAuth(mContext, WEIBO_APP_KEY, REDIRECT_URL, SCOPE);
		SsoHandler ssoHandler = new SsoHandler(activity, authInfo);
		ssoHandler.authorize(listener);
		return ssoHandler;
	}
	
	public void showLoginDialog(Fragment fragment, final PendingAction pa, final RegisterPSCallBack callback) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.login_dialog, null);
		if (Build.VERSION.SDK_INT < 11) {
			view.setBackgroundColor(Color.WHITE);
		}
		
		LoginButton fbLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
		fbLoginButton.setReadPermissions(Arrays.asList("email"));
		if (fragment != null) {
			fbLoginButton.setFragment(fragment);
		}
		fbLoginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			
			@Override
            public void onUserInfoFetched(GraphUser user) {
            	if (user != null) {
        			Object email = user.asMap().get("email");
        			Object gender = user.asMap().get("gender");
        			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        			pairs.add(new BasicNameValuePair("udid", MFConfig.DEVICE_ID));
        			pairs.add(new BasicNameValuePair("email", email == null ? "" : email.toString()));
        			pairs.add(new BasicNameValuePair("name", user.getName()));
        			pairs.add(new BasicNameValuePair("fbid", user.getId()));
        			pairs.add(new BasicNameValuePair("gender", gender == null ? "" : gender.toString()));
        			pairs.add(new BasicNameValuePair("fbtoken", Session.getActiveSession().getAccessToken()));
        			pairs.add(new BasicNameValuePair("fbexpire", Session.getActiveSession().getExpirationDate().toString()));
        			pairs.add(new BasicNameValuePair("pic_link", "https://graph.facebook.com/" + user.getId() + "/picture"));
        			pairs.add(new BasicNameValuePair("devicetoken", "0")); //this device token is for push notification
            		AsyncTaskHelper.executeWithResultString(new RegisterPS(mContext, pairs, user.getName(), true, pa, callback));
            	}
            }
        });
		
		AuthInfo authInfo = new AuthInfo(mContext, WEIBO_APP_KEY, REDIRECT_URL, SCOPE);
		mWeiboLoginButton = (com.sina.weibo.sdk.widget.LoginoutButton) view.findViewById(R.id.weibo_login_button);
		mWeiboLoginButton.setText(R.string.weiboLogin);
		mWeiboLoginButton.setWeiboAuthInfo(authInfo, new WeiboAuthListener() {
			
			@Override
			public void onComplete(Bundle values) {
	            final Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
	            if (accessToken != null && accessToken.isSessionValid()) {
	                AccessTokenKeeper.writeAccessToken(mContext.getApplicationContext(), accessToken);
	                pDialog = ProgressDialog.show(mContext, null,
            				mContext.getString(R.string.loginProcess), false, false);
	                
	                new WeiboUserApi(accessToken).getUserInfo(new RequestListener() {
						@Override
						public void onIOException(IOException e) {
						}
						@Override
						public void onError(WeiboException e) {
						}
						@Override
						public void onComplete4binary(ByteArrayOutputStream responseOS) {
						}
						@Override
						public void onComplete(String response) {
				            if (!TextUtils.isEmpty(response)) {
				                try {
				                	
				                    JSONObject obj = new JSONObject(response);
				                    String screename = obj.getString("screen_name");
				                    String gender = obj.getString("gender");
				                    String profile_image_url = obj.getString("profile_image_url");
				                    
				                    List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				        			pairs.add(new BasicNameValuePair("udid", MFConfig.DEVICE_ID));
				        			pairs.add(new BasicNameValuePair("email", ""));
				        			pairs.add(new BasicNameValuePair("name", screename));
				        			pairs.add(new BasicNameValuePair("fbid", accessToken.getUid()));
				        			pairs.add(new BasicNameValuePair("gender", gender));
				        			pairs.add(new BasicNameValuePair("fbtoken", accessToken.getToken()));
				        			pairs.add(new BasicNameValuePair("fbexpire", accessToken.getExpiresTime() + ""));
				        			pairs.add(new BasicNameValuePair("pic_link", profile_image_url));
				        			pairs.add(new BasicNameValuePair("devicetoken", "0")); //this device token is for push notification
				            		AsyncTaskHelper.executeWithResultString(new RegisterPS(mContext, pairs, screename, false, pa, callback));
				            		
				                } catch (JSONException e) {
				                    e.printStackTrace();
				                }
				            }
						}
					});
	    			
	        		
	    			if (mLoginDialog != null) {
	    				mLoginDialog.dismiss();
	    			}
	            } else {
	            	String code = values.getString("code");
	            }
			}
			@Override
			public void onWeiboException(WeiboException arg0) {
			}
			@Override
			public void onCancel() {
			}
		});
		
		mLoginDialog = new AlertDialog.Builder(mContext)
		.setView(view)
		.setPositiveButton(mContext.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
		
	}
	
	public void callLogout(final boolean showToast) {
		    Session session = Session.getActiveSession();
		    if (session != null && !session.isClosed()) {
	            session.closeAndClearTokenInformation();
		    }
		    
		    Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(mContext);
	        if (accessToken != null && accessToken.isSessionValid()) {
		    
			    new LogoutAPI(accessToken).logout(new RequestListener() {
					@Override
					public void onIOException(IOException e) {
					}
					@Override
					public void onError(WeiboException e) {
					}
					@Override
					public void onComplete4binary(ByteArrayOutputStream responseOS) {
					}
					@Override
					public void onComplete(String response) {
//			            if (!TextUtils.isEmpty(response)) {
//			                try {
//			                    JSONObject obj = new JSONObject(response);
//			                    String value = obj.getString("result");
//			                    
//			                    if ("true".equalsIgnoreCase(value)) {
//			                        AccessTokenKeeper.clear(mContext);
//			        	            
//			                    }
//			                } catch (JSONException e) {
//			                    e.printStackTrace();
//			                }
//			            }
					}
				});
	        }
	        
	        AccessTokenKeeper.clear(mContext);
	        
	        if (showToast) {
            	Toast.makeText(mContext, R.string.logoutMessage, Toast.LENGTH_SHORT).show();
			}
            PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERID_PREF_KEY, null);
    		PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERNAME_PREF_KEY, null);
	}
	

//	public boolean isFacebook() {
//		return isFacebook;
//	}
//
//	public void setFacebook(boolean isFacebook) {
//		this.isFacebook = isFacebook;
//	}
	
	
	
	//-----------FB Only---------------------
	
    
    private Session.StatusCallback mStatusCallback = new Session.StatusCallback() {
    	@Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };
    
    public Session.StatusCallback getFBSessionCallback() {
    	return mStatusCallback;
    }
	
	
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    	if (state == SessionState.OPENING) {
    		pDialog = ProgressDialog.show(mContext, null,
    				mContext.getString(R.string.loginProcess), false, false);
    	} else if (state == SessionState.CLOSED_LOGIN_FAILED){
    		if (pDialog != null) {
    			pDialog.dismiss();
    		}
    	}
		if (session.isOpened()) {
			if (mLoginDialog != null) {
				mLoginDialog.dismiss();
			}
		} 
    }
    
    // ------ Weibo only-------------------
    private WeiboAuthListener mAuthListener = new WeiboAuthListener() {
        @Override
        public void onComplete(Bundle values) {

        }

        @Override
        public void onWeiboException(WeiboException e) {
        	MFLog.e(TAG, "weibo exception = " + e.getMessage());
        }

        @Override
        public void onCancel() {
        	MFLog.e(TAG, "weibo on Cancel");
        }
    };
    
    public com.sina.weibo.sdk.widget.LoginoutButton getWeiboLoginButton() {
    	return mWeiboLoginButton;
    }
    
    //-----------------------------------------
    
    
    private class RegisterPS extends AsyncTask<Void, Void, String> {
    	
    	private List<NameValuePair> pairs;
    	private String userName;
    	private boolean isFb;
    	private PendingAction pa;
    	private Context context;
    	private RegisterPSCallBack callback;
    	
    	public RegisterPS(Context context, List<NameValuePair> pairs, String userName, boolean isFb, PendingAction pa, RegisterPSCallBack callback) {
    		this.context = context;
    		this.pairs = pairs;
    		this.userName = userName;
    		this.isFb = isFb;
    		this.pa = pa;
    		this.callback = callback;
		}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    	}
    	
    	@Override
    	protected String doInBackground(Void... params) {
    		// TODO Auto-generated method stub
    		
    		try {
    			InputStream is = MFService.executeRequestWithHttpParams(MFURL.PHOTOSHARE_REGISTER + (isFb ? "f" : "w"), pairs);
    			StringBuilder sb = new StringBuilder();
    			BufferedReader rd = new BufferedReader(new InputStreamReader(
    					is));
    			String line = null;
    			while ((line = rd.readLine()) != null) {
    				sb.append(line + "\n");
    			}
    			rd.close();
    			is.close();
    			
    			String returnMemberId = sb.toString().trim();
    			Integer.parseInt(returnMemberId);
    			
    			return returnMemberId;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {

			}
    		
    		return null;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
    		super.onPostExecute(result);
			if (pDialog != null) {
				pDialog.dismiss();
			}
    		if (result == null) {
    			Toast.makeText(context, R.string.errorMsg, Toast.LENGTH_SHORT).show();
    			callLogout(false);
    			callback.onErrorRegistered();
    			return;
    		}
    		
    		Toast.makeText(context, context.getResources().getString(R.string.loginMessage, userName), Toast.LENGTH_SHORT).show();
    		MFConfig.memberId = result;
    		MFConfig.memberName = userName;
    		PreferenceHelper.savePreferencesStr(context, MFConstants.PS_MEMBERID_PREF_KEY, result);
    		PreferenceHelper.savePreferencesStr(context, MFConstants.PS_MEMBERNAME_PREF_KEY, userName);
    		
    		callback.onCompleteRegistered(pa);
    	}
    	
    }
	
	
}
