package com.cycon.macaufood.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;

public class About extends BaseActivity {
	
	private TextView ourWebsite;
	private TextView emailUs;
	private TextView callUs1;
	private TextView callUs2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about);
		
		ourWebsite = (TextView) findViewById(R.id.ourWebsite);
		emailUs = (TextView) findViewById(R.id.emailUs);
		callUs1 = (TextView) findViewById(R.id.callUs1);
		callUs2 = (TextView) findViewById(R.id.callUs2);
		
		ourWebsite.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.cycon.com.mo"));
				startActivity(intent);
			}
		});
		emailUs.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:info@cycon.com.mo"));
				startActivity(intent);
			}
		});
		
		callUs1.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:63573095"));
				startActivity(intent); 
			}
		});
		callUs2.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:63611387"));
				startActivity(intent); 
			}
		});
	}
}
