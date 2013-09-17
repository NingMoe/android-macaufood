package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFUtil;

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
		}
		//fetch new front page after show front page
		MFService.fetchFrontPage(getApplicationContext());
	}
}
