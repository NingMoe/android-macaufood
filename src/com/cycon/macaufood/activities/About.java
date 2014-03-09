package com.cycon.macaufood.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cycon.macaufood.R;

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
		
		setContentView(R.layout.about);
		
		setTitle(getString(R.string.app_name) + " v" + getString(R.string.versionNo));
		
		disclaimer = (TextView) findViewById(R.id.disclaimer);
		macauWifi = (TextView) findViewById(R.id.macauWifi);
		ourWebsite = (TextView) findViewById(R.id.ourWebsite);
		emailUs = (TextView) findViewById(R.id.emailUs);
		callUs1 = (TextView) findViewById(R.id.callUs1);
		callUs2 = (TextView) findViewById(R.id.callUs2);
		
		disclaimer.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showDisclaimerDialog();
			}
		});
		
		macauWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(About.this, MacauWifi.class);
				startActivity(intent);
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
	
	private void showDisclaimerDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
		.setTitle(R.string.disclaimer)
		.setMessage(R.string.disclaimerText)
		.setCancelable(false)
		.setPositiveButton(getString(R.string.agreeDisclaimer),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();

		TextView textView = (TextView) dialog.findViewById(android.R.id.message);
		textView.setTextSize(15);
	}
}
