package com.cycon.macaufood.activities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AccessTokenKeeper;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.LoginHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.WeiboPostApi;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;

public class PSUploadPhoto extends BaseActivity {
	
	private static int SELECT_CAFE_REQUEST_CODE = 7001;
	private final int POST_MENU_ID = 1;
	private final static String TAG = "PSUploadPhoto";
	private Cafe mSelectedCafe;
	private ImageView mImageView;
	private EditText mCaptionText;
	private TextView mCafeName;
	private View mCafeNameLayout;
	private ToggleButton mFbToggleButton;
	private ToggleButton mWeiboToggleButton;
	private ProgressDialog pDialog;
	private int mPhotoRotation;
	private byte[] mByeData;
	private Uri mImageUri;
	private File mCaptureImageFile;
	
	//sharing to fb / weibo code--------------
	
	private LoginHelper mLoginHelper;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	
    private Session.StatusCallback mStatusCallback = new Session.StatusCallback() {
    	@Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_upload_photo);
		
		mLoginHelper = new LoginHelper(this);
		uiHelper = new UiLifecycleHelper(this, mStatusCallback);
		uiHelper.onCreate(savedInstanceState);
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		mCaptionText = (EditText) findViewById(R.id.captionText);
		mCafeName = (TextView) findViewById(R.id.cafeName);
		mCafeNameLayout = findViewById(R.id.cafeNameLayout);
		mFbToggleButton = (ToggleButton) findViewById(R.id.toggleButtonFb);
		mWeiboToggleButton = (ToggleButton) findViewById(R.id.toggleButtonWeibo);
		
		mFbToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1) {
					Session session = Session.getActiveSession();
		            // Check for publish permissions    
		            List<String> permissions = session.getPermissions();
		            if (!isSubsetOf(PERMISSIONS, permissions)) {
		                Session.NewPermissionsRequest newPermissionsRequest = new Session
		                        .NewPermissionsRequest(PSUploadPhoto.this, PERMISSIONS);
		                session.requestNewPublishPermissions(newPermissionsRequest);
		            }
				}
			}
		});
		
		mCafeNameLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(PSUploadPhoto.this, PSCafeLocation.class);
				startActivityForResult(i, SELECT_CAFE_REQUEST_CODE);
			}
		});
		
		mImageUri = getIntent().getData();
		Object file = getIntent().getSerializableExtra("photoFile");
		if (file != null) {
			mCaptureImageFile = (File) file;
		}
		Bitmap bitmap = null;
		try {
			bitmap = MFUtil.getThumbnail(mImageUri, this);
			mPhotoRotation = MFUtil.getOrientation(this, mImageUri, mCaptureImageFile);
			if (mPhotoRotation != 0) {
				bitmap = MFUtil.rotateBitmap(bitmap, mPhotoRotation);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mImageView.setImageBitmap(bitmap);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_CAFE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				mSelectedCafe = (Cafe) data.getSerializableExtra("cafe");
				mCafeName.setText(mSelectedCafe.getName());
			}
		}
		
		uiHelper.onActivityResult(requestCode, resultCode, data);
		if (mLoginHelper.getWeiboLoginButton() != null) {
			mLoginHelper.getWeiboLoginButton().onActivityResult(requestCode,
					resultCode, data);
		}
	}
	
	private Handler mHandler = new Handler(Looper.myLooper()) {
		public void handleMessage(Message msg) {
			Log.e("ZZZ", "receive message");
			if (mFbToggleButton.isChecked()) {
				publishStoryToFb(getStatusString());
			}
			AsyncTaskHelper.executeWithResultString(new UploadPhotoTask());	
		}; 
	};
	
	private void uploadPhoto() {
		
		if (mSelectedCafe == null) {
			Toast.makeText(this, R.string.chooseCafe, Toast.LENGTH_SHORT).show();
			return;
		}
		
		pDialog = ProgressDialog.show(PSUploadPhoto.this, null, getString(R.string.uploadingPhotoProgress), false, false);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				setByteDataBitmap();
			}
		}).start();
		

		if (mWeiboToggleButton.isChecked()) {
			Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(this);
	        if (accessToken != null && accessToken.isSessionValid()) {
	        	new WeiboPostApi(accessToken).post(getStatusString(), mCaptureImageFile == null ? MFUtil.getPath(this, mImageUri) : mCaptureImageFile.getAbsolutePath());
	        }
		}
		
		
	}
	
	private String getStatusString() {
		String status = getString(R.string.sharePhotoCaption, mCaptionText
				.getText().toString(), mSelectedCafe.getName(),
				mSelectedCafe.getAddress(), mSelectedCafe
						.getPhone(), mSelectedCafe.getOpenhours());
		return status;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, POST_MENU_ID, 1, R.string.post).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			showConfirmExitDialog();
			return true;
		case POST_MENU_ID:
			uploadPhoto();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    
    @Override
    public void onBackPressed() {
    	showConfirmExitDialog();
    }
    
    private void showConfirmExitDialog() {
		new AlertDialog.Builder(this)
		.setMessage(this.getString(R.string.confirmExitUpload))
		.setPositiveButton(this.getString(R.string.confirmed),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						dialog.dismiss();
						finish();
					}
				})
		.setNegativeButton(this.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						dialog.dismiss();
					}
				}).show();
    }
    
    private void setByteDataBitmap() {
    		InputStream imageIs = null;
			try {
				imageIs = getContentResolver().openInputStream(mImageUri);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
    		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(imageIs);
			if (mPhotoRotation != 0) {
				bitmap = MFUtil.rotateBitmap(bitmap, mPhotoRotation);
			}
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 600) {
            	height =  (int) (height * 600.0 / width);
				width = 600;
			}
            Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            finalBitmap.compress(CompressFormat.JPEG, 50, byteArrayOutputStream); 
            mByeData = byteArrayOutputStream.toByteArray();
            
            mHandler.sendMessage(mHandler.obtainMessage());
    }
    
    private void publishStoryToFb(String status) {
        Session session = Session.getActiveSession();

        if (session != null){

			Bundle postParams = new Bundle();
			postParams.putString("name", status);
			postParams.putByteArray("photo", mByeData);

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
//					JSONObject graphResponse = response.getGraphObject()
//							.getInnerJSONObject();
//					String postId = null;
//					try {
//						postId = graphResponse.getString("id");
//					} catch (JSONException e) {
//						Log.i(TAG, "JSON error " + e.getMessage());
//					}
//					FacebookRequestError error = response.getError();
//					if (error != null) {
//						Toast.makeText(getApplicationContext(),
//								error.getErrorMessage(), Toast.LENGTH_SHORT)
//								.show();
//					} else {
//						Toast.makeText(getApplicationContext(), postId,	
//								Toast.LENGTH_LONG).show();
//					}
				}
			};

			Request request = new Request(session, "me/photos", postParams,
					HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
        }

    }
	
	
	private class UploadPhotoTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {
			
            try {
        		HttpClient client = new DefaultHttpClient();
        		HttpParams httpParams = client.getParams();
        		HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
				String url = MFURL.PHOTOSHARE_UPLOAD + "&userid="
						+ MFConfig.memberId + "&photocaption="
						+ URLEncoder.encode(mCaptionText.getText().toString(), "UTF-8")
						+ "&cafename="
						+ URLEncoder.encode(mSelectedCafe.getName(), "UTF-8")
						+ "&coordx=" + mSelectedCafe.getCoordx() + "&coordy="
						+ mSelectedCafe.getCoordy() + "&cafeid="
						+ mSelectedCafe.getId() + "&cafeaddress="
						+ URLEncoder.encode(mSelectedCafe.getAddress(), "UTF-8")
						+ "&cafephone="
						+ URLEncoder.encode(mSelectedCafe.getPhone(), "UTF-8");
        		HttpPost request = new HttpPost(url);

                ByteArrayBody byteArrayBody = new ByteArrayBody(mByeData, "image");
        		
        		MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();  
        		multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        		
        		multipartEntity.addPart("userfile", byteArrayBody); 
        		request.setEntity(multipartEntity.build());
        		HttpResponse response = client.execute(request);
        		InputStream is = response.getEntity().getContent();
        		
        		StringBuilder sb = new StringBuilder();
    			BufferedReader rd = new BufferedReader(new InputStreamReader(
    					is));
    			String line = null;
    			while ((line = rd.readLine()) != null) {
    				sb.append(line + "\n");
    			}
    			rd.close();

    			return sb.toString().trim();
            	
			} catch (MalformedURLException e) {
				MFLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				MFLog.e(TAG, "io exception" + e.getMessage());
				e.printStackTrace();
				return null;
			} 
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.e("ZZZ", "result =" + result);
			
			pDialog.dismiss();
			if (result != null) {
				Toast.makeText(PSUploadPhoto.this, R.string.uploadSucceed, Toast.LENGTH_SHORT).show();
				setResult(Activity.RESULT_OK);
			} else {
				Toast.makeText(PSUploadPhoto.this, R.string.uploadFailure, Toast.LENGTH_SHORT).show();
			}
			finish();
		}
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
	
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	
	
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
//    	if (state == SessionState.OPENING) {
//    		pDialog = ProgressDialog.show(mContext, null,
//    				mContext.getString(R.string.loginProcess), false, false);
//    	} else if (state == SessionState.CLOSED_LOGIN_FAILED){
//    		if (pDialog != null) {
//    			pDialog.dismiss();
//    		}
//    	}
//		if (session.isOpened()) {
//			if (mLoginDialog != null) {
//				mLoginDialog.dismiss();
//			}
//		} 
    	
    	if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
    	} else {
    		mFbToggleButton.setChecked(false);
    	}
    	
    }
	
}
