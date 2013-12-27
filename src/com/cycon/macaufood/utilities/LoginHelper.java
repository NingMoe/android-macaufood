package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import com.cycon.macaufood.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class LoginHelper {
//	private boolean isFacebook;
	private Context mContext;
	private ProgressDialog pDialog;
	private Dialog mLoginDialog;
	
    public enum PendingAction {
        NONE,
        FRIENDS,
        CAMERA,
        SETTINGS,
        LIKE, 
        COMMENT
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
		return false;
	}
	
	public void showLoginDialog(Fragment fragment, final PendingAction pa, final RegisterPSCallBack callback) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.login_dialog, null);
		TextView weiboTv = (TextView) view.findViewById(R.id.weiboLogin);
		
		LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
		loginButton.setReadPermissions(Arrays.asList("email"));
		if (fragment != null) {
			loginButton.setFragment(fragment);
		}
		loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			
			@Override
            public void onUserInfoFetched(GraphUser user) {
            	if (user != null) {
            		AsyncTaskHelper.executeWithResultString(new RegisterPS(mContext, user, pa, callback));
            	}
            }
        });
		weiboTv.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
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
	
	public void callLogout(boolean showToast) {
//		if (isFacebook) {
		    Session session = Session.getActiveSession();
		    if (session != null && !session.isClosed()) {
	            session.closeAndClearTokenInformation();
	            if (showToast) {
	            	Toast.makeText(mContext, R.string.logoutMessage, Toast.LENGTH_SHORT).show();
				}
	            PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERID_PREF_KEY, null);
	    		PreferenceHelper.savePreferencesStr(mContext, MFConstants.PS_MEMBERNAME_PREF_KEY, null);
		    }
//		} else {
//			
//		}
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
    	if (exception != null) {
    		Log.e("ZZZ", "exception= " + exception.getMessage());
    	}
    }
    
    // ------ Weibo only-------------------
    
    
    
    //-----------------------------------------
    
    
    private class RegisterPS extends AsyncTask<Void, Void, String> {
    	
    	private GraphUser user;
    	private PendingAction pa;
    	private Context context;
    	private RegisterPSCallBack callback;
    	
    	public RegisterPS(Context context, GraphUser user, PendingAction pa, RegisterPSCallBack callback) {
    		this.context = context;
    		this.user = user;
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
    			InputStream is = MFService.executeRequestWithHttpParams(MFURL.PHOTOSHARE_REGISTER + "f", pairs);
    			StringBuilder sb = new StringBuilder();
    			BufferedReader rd = new BufferedReader(new InputStreamReader(
    					is));
    			String line = null;
    			while ((line = rd.readLine()) != null) {
    				sb.append(line + "\n");
    			}
    			rd.close();
    			
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
    			callback.onErrorRegistered();
    			return;
    		}
    		
    		Toast.makeText(context, context.getResources().getString(R.string.loginMessage, user.getName()), Toast.LENGTH_SHORT).show();

    		MFConfig.memberId = result;
    		MFConfig.memberName = user.getName();
    		PreferenceHelper.savePreferencesStr(context, MFConstants.PS_MEMBERID_PREF_KEY, result);
    		PreferenceHelper.savePreferencesStr(context, MFConstants.PS_MEMBERNAME_PREF_KEY, user.getName());
    		
    		//if facebook
//    		boolean isFacebook = true;
//    		LoginHelper.getInstance(context).setFacebook(isFacebook);
    		callback.onCompleteRegistered(pa);
    	}
    	
    }
	
	
}
