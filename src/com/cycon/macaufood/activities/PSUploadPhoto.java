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
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;

public class PSUploadPhoto extends BaseActivity {
	
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
	private int SELECT_CAFE_REQUEST_CODE = 7001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_upload_photo);
		
		mImageView = (ImageView) findViewById(R.id.imageView);
		mCaptionText = (EditText) findViewById(R.id.captionText);
		mCafeName = (TextView) findViewById(R.id.cafeName);
		mCafeNameLayout = findViewById(R.id.cafeNameLayout);
		mFbToggleButton = (ToggleButton) findViewById(R.id.toggleButtonFb);
		mWeiboToggleButton = (ToggleButton) findViewById(R.id.toggleButtonWeibo);
		
		mCafeNameLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(PSUploadPhoto.this, PSCafeLocation.class);
				startActivityForResult(i, SELECT_CAFE_REQUEST_CODE);
			}
		});
		
		Uri imageUri = getIntent().getData();
		Bitmap bitmap = null;
		try {
			bitmap = MFUtil.getThumbnail(imageUri, this);
			mPhotoRotation = MFUtil.getOrientation(this, imageUri, (File)getIntent().getSerializableExtra("photoFile"));
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
	}
	
	private void uploadPhoto() {
		//check logout
		
		if (mSelectedCafe == null) {
			Toast.makeText(this, R.string.chooseCafe, Toast.LENGTH_SHORT).show();
			return;
		}
		
		AsyncTaskHelper.executeWithResultString(new UploadPhotoTask());	
		
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
	
	
	private class UploadPhotoTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(PSUploadPhoto.this, null, getString(R.string.uploadingPhotoProgress));
		}
		
		@Override
		protected String doInBackground(Void... params) {
			
            try {
        		HttpClient client = new DefaultHttpClient();
        		HttpParams httpParams = client.getParams();
        		HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
        		String url = MFURL.PHOTOSHARE_UPLOAD + "&userid=" + MFConfig.memberId + "&photocaption=" + mCaptionText.getText().toString() + 
        				"&cafename=" + mSelectedCafe.getName() + "&coordx=" + mSelectedCafe.getCoordx() + 
        				"&coordy=" + mSelectedCafe.getCoordy() + "&cafeid=" + mSelectedCafe.getId() + 
        				"&cafeaddress=" + mSelectedCafe.getAddress() + "&cafephone=" + mSelectedCafe.getPhone();
        		URLEncoder.encode(url, "UTF-8");
        		HttpPost request = new HttpPost(url);
        		
        		InputStream imageIs = getContentResolver().openInputStream(getIntent().getData());
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
                byte[] byteData = byteArrayOutputStream.toByteArray();
                ByteArrayBody byteArrayBody = new ByteArrayBody(byteData, "image");
        		
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
			
			Log.e("ZZZ", "result = " + result);
			
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
	
}
