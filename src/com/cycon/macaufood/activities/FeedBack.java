package com.cycon.macaufood.activities;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.MFConfig;

public class FeedBack extends Activity {
	
	private static final String TAG = "FeedBack";
	private Button submit;
	private Button cancel;
	private TextView name;
	private TextView email;
	private TextView content;
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback);
		
		submit = (Button)findViewById(R.id.submitBtn);
		cancel = (Button)findViewById(R.id.cancelBtn);
		name = (TextView)findViewById(R.id.nameText);
		email = (TextView)findViewById(R.id.emailText);
		content = (TextView)findViewById(R.id.contentText);
		
		submit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				String emailStr = email.getText().toString();
				if (!validateEmail(emailStr)) {
					Toast.makeText(FeedBack.this, getString(R.string.emailWrongFormat), Toast.LENGTH_SHORT).show();
					return;
				} else if (content.getText().toString().length() < 5) {
					Toast.makeText(FeedBack.this, getString(R.string.contentWordsTooShort), Toast.LENGTH_SHORT).show();
					return;
				} else {
					AsyncTaskHelper.executeWithResultBoolean(new SubmitFeedBackTask());
				}
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				finish();
			}
		});
	}
	
	private boolean validateEmail(String emailText) {
		String emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}"; 
		return emailText.matches(emailRegex);
	}
	
	private class SubmitFeedBackTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(FeedBack.this, null,
					getString(R.string.sending), false, true);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			String urlStr = "http://www.cycon.com.mo/xml_submitmsg2.php?key=cafecafe&udid=android-" + 
					MFConfig.DEVICE_ID + "&cafeid=" + getIntent().getStringExtra("id") 
					+ "&name=" + name.getText().toString() + "&content=" 
					+ content.getText().toString() + "&email=" + email.getText().toString();
			
            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	client.execute(request);
            	
			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				Log.e(TAG, "exception");
				e.printStackTrace();
				return false;
			}
			

			return true;
		}
		
    	@Override
    	protected void onPostExecute(Boolean success) {
    		super.onPostExecute(success);
    		
    		if (pDialog != null) {
    			pDialog.dismiss();
    		}
    		
    		if (success) {
				Toast.makeText(FeedBack.this, getString(R.string.sendSucceed), Toast.LENGTH_SHORT).show();
				finish();
    		} else {
				Toast.makeText(FeedBack.this, getString(R.string.sendFailed), Toast.LENGTH_SHORT).show();
    		}

    	}
	}
}
