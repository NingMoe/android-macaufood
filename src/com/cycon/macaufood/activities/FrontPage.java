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
		File f = fileCache.getFile("1");
		Bitmap bitmap = decodeFile(f);
		if (bitmap != null) {
			frontPage.setImageBitmap(bitmap);
		}
			
	}
	
	
    private Bitmap decodeFile(File f){
        try {
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
        }
        return null;
    }

}
