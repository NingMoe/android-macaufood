package com.cycon.macaufood;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class About extends BaseActivity {
	
	private TextView disclaimer;
	private TextView macauWifi;
	private TextView ourWebsite;
	private TextView emailUs;
	private TextView callUs1;
	private TextView callUs2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		needMenu = false;
		setContentView(R.layout.about);
		
		disclaimer = (TextView) findViewById(R.id.disclaimer);
		macauWifi = (TextView) findViewById(R.id.macauWifi);
		ourWebsite = (TextView) findViewById(R.id.ourWebsite);
		emailUs = (TextView) findViewById(R.id.emailUs);
		callUs1 = (TextView) findViewById(R.id.callUs1);
		callUs2 = (TextView) findViewById(R.id.callUs2);
		
		disclaimer.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(About.this, Disclaimer.class);
				startActivity(i);
			}
		});
		macauWifi.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(About.this, MacauWifi.class);
				startActivity(i);
			}
		});
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
