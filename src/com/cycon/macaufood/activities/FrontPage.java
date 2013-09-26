package com.cycon.macaufood.activities;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class FrontPage extends Activity {
	
	private ImageView cross;
	private ImageView frontPage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.front_page);
		
		cross = (ImageView) findViewById(R.id.cross);
		cross.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				finish();
			}
		});
		frontPage = (ImageView) findViewById(R.id.frontPage);
		FileCache fileCache = new FileCache(this, ImageType.FRONTPAGE);
		Bitmap bitmap = MFUtil.getBitmapFromCache(fileCache, "1");
		if (bitmap != null) {
			frontPage.setImageBitmap(bitmap);
			frontPage.setOnClickListener(new OnClickListener() {
				
				public void onClick(View arg0) {
					String frontPageLink = PreferenceHelper.getPreferenceValueStr(FrontPage.this, MFConstants.FRONT_PAGE_LINK_PREF_KEY, "");
					if (frontPageLink.length() > 10) {
						Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(frontPageLink));
						startActivity(i);
					}
				}
			});
		}
		
		MFService.fetchFrontPage(getApplicationContext());
		
	}
	
}
