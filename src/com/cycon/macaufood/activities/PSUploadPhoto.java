package com.cycon.macaufood.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFLog;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;

public class PSUploadPhoto extends BaseActivity {
	
	private final int POST_MENU_ID = 1;
	private final String TAG = "PSUploadPhoto";
	private ImageView mImageView;
	private TextView mCafeName;
	private View mCafeNameLayout;
	private ToggleButton mFbToggleButton;
	private ToggleButton mWeiboToggleButton;
	private int SELECT_CAFE_REQUEST_CODE = 7001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_upload_photo);
		
		mImageView = (ImageView) findViewById(R.id.imageView);
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		mImageView.setImageBitmap(bitmap);
	}
	
	private void uploadPhoto() {
		//check logout
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("userid", MFConfig.memberId));
		pairs.add(new BasicNameValuePair("photocaption", "123"));
		pairs.add(new BasicNameValuePair("cafename", "321"));
		pairs.add(new BasicNameValuePair("coordx", ""));
		pairs.add(new BasicNameValuePair("coordy", ""));
		pairs.add(new BasicNameValuePair("cafeid", "2"));
		pairs.add(new BasicNameValuePair("cafeaddress", ""));
		pairs.add(new BasicNameValuePair("cafephone", ""));
		
		AsyncTaskHelper.executeWithResultString(new UploadPhotoTask(MFURL.PHOTOSHARE_UPLOAD, pairs, getPath(getIntent().getData())));	
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, POST_MENU_ID, 1, R.string.post).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case POST_MENU_ID:
			uploadPhoto();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    public String getPath(Uri uri) 
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }
	
	
	private class UploadPhotoTask extends AsyncTask<Void, Void, String> {
		
		private String filePath;
		private String url;
		private List<NameValuePair> pairs;
		
		private UploadPhotoTask(String url, List<NameValuePair> pairs, String filePath) {
			this.url = url;
			this.pairs = pairs;
			this.filePath = filePath;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			
            try {
        		HttpClient client = new DefaultHttpClient();
        		HttpParams httpParams = client.getParams();
        		HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
        		//&userid=%@&photocaption=%@&cafename=%@&coordx=%@&coordy=%@&cafeid=%d@&cafeaddress=%@&cafephone=%@
        		url = url + "&userid=" + MFConfig.memberId + "&photocaption=" + "1234" + 
        		"&cafename=Testing&coordx=0&coordy=0&cafeid=12&cafeaddress=123&cafephone=123";
        		HttpPost request = new HttpPost(url);
        		
        		File file = new File(filePath);
//        		FileBody fileBody = new FileBody(file);
//        		ContentBody encFile = new FileBody(file, "image/jpg");
        		
        		MultipartEntityBuilder multipartEntity = MultipartEntityBuilder.create();  
        		multipartEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//        		multipartEntity.addBinaryBody("picture", file);
        		
        		ContentBody cbFile = new FileBody(file, "image/jpeg");
        		multipartEntity.addPart("userfile", cbFile); 
        		
        		
//        		ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        		mThumbNali.compress(CompressFormat.JPEG, 100, bao);
//
//        		ByteArrayBody body = new ByteArrayBody(bao.toByteArray(), "image/jpeg", "picture");
        		
                for(int index=0; index < pairs.size(); index++) {
//                	multipartEntity.addPart(pairs.get(index).getName(), new StringBody(pairs.get(index).getValue()));
                }
                
                multipartEntity.setBoundary("---------------------------14737809831466499882746641449");
        		
//        		FileInputStream fileInputStream = new FileInputStream(file);
//        		InputStreamEntity entity = new InputStreamEntity(fileInputStream, file.length());
//        		entity.setContentType("binary/octet-stream");
//        		entity.setChunked(true);
        		
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
			} catch (Exception e) {
				MFLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
			Log.e("ZZZ", "string = " + result);
		}
	}
	
}
