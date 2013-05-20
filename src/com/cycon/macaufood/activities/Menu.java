package com.cycon.macaufood.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.PhoneUtils;

public class Menu extends BaseActivity {
	
	private static final String TAG = Menu.class.getName();
	private String menuid;
	private int totalPages;
	private int currentPage = 1;
	private boolean isError;
	
	private TextView header;
	private TextView errorMsg;
	private WebView webView;
	private ProgressBar progressBar;
	private ImageButton prevPage;
	private ImageButton nextPage;
	private ImageButton phoneCall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.menu);
		menuid = getIntent().getStringExtra("menuid");
		totalPages = Integer.parseInt(getIntent().getStringExtra("page"));
		if (totalPages == 0) totalPages = 1;
		header = (TextView) findViewById(R.id.header);
		errorMsg = (TextView) findViewById(R.id.errorMsg);
		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		prevPage = (ImageButton) findViewById(R.id.prevPage);
		nextPage = (ImageButton) findViewById(R.id.nextPage);
		phoneCall = (ImageButton) findViewById(R.id.phoneCall);
		
		if (totalPages == 1) {
			prevPage.setEnabled(false);
			nextPage.setEnabled(false);
		}
		setHeaderText();
		
		webView.loadUrl(getUrl());
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		webView.setInitialScale(60);
		
		webView.setWebViewClient(new WebViewClient() {
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
				if (isError) {
					errorMsg.setVisibility(View.VISIBLE);
					webView.setVisibility(View.GONE);
				} else {
					errorMsg.setVisibility(View.GONE);
					webView.setVisibility(View.VISIBLE);
				}
				isError = false;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				isError = true;
			}
			
		});
		
		prevPage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				errorMsg.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				if (currentPage == 1) currentPage = totalPages;
				else currentPage--;
				setHeaderText();
				webView.loadUrl(getUrl());
			}
		});
		nextPage.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				errorMsg.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				if (currentPage == totalPages) currentPage = 1;
				else currentPage++;
				setHeaderText();
				webView.loadUrl(getUrl());
			}
		});
		
		if (getIntent().getStringExtra("phone").equals("")) phoneCall.setEnabled(false);
		else {
			
			phoneCall.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					ArrayList<String> phoneNos = PhoneUtils.getPhoneStr(getIntent().getStringExtra("phone"));
					if (phoneNos.size() == 0) return;
					
					final String number1 = phoneNos.get(0);
					final String number2 = phoneNos.size() == 1 ? null : phoneNos.get(1);
					
					AlertDialog dialog = new AlertDialog.Builder(Menu.this)
					.setTitle("撥打電話")
					.setMessage(number1)
					.setPositiveButton(number2 == null ? "撥打" : "打電話1",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int whichButton) {
									Intent intent = new Intent(Intent.ACTION_DIAL);
									String phoneUri = "tel:" + number1.replace("-", "");
									intent.setData(Uri.parse(phoneUri));
									startActivity(intent); 
								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int whichButton) {
									dialog.dismiss();
								}
							}).create();
					
					
					if (number2 != null) {
						dialog.setMessage("電話1:  " + number1 + "\n" + "電話2:  " + number2);
						dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "打電話2", 
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Intent.ACTION_DIAL);
								String phoneUri = "tel:" + number2.replace("-", "");
								intent.setData(Uri.parse(phoneUri));
								startActivity(intent); 
							}
						});
					}
					
					dialog.show();
				}
			});
		
		}
		
		
	}
	
	private String getUrl() {
		return "http://www.cycon.com.mo/xml_menu.php?id=" + menuid + "&page=" + currentPage + "&udid=android-" + MFConfig.DEVICE_ID;
	}
	
	private void setHeaderText() {
		header.setText("�?�單 " + currentPage + "/" + totalPages + " ~ " + getIntent().getStringExtra("name"));
	}
	
//	  private void checkLandscape() {
//		if (Config.isLandscape(this)) {
//			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			menu_panel.setVisibility(View.GONE);
//			header.setVisibility(View.GONE);
//			RelativeLayout.LayoutParams params = (LayoutParams) webView.getLayoutParams();
//			params.topMargin = 0;
//			webView.setLayoutParams(params);
//		}
//		else {
//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//			menu_panel.setVisibility(View.VISIBLE);
//			header.setVisibility(View.VISIBLE);
//			RelativeLayout.LayoutParams params = (LayoutParams) webView.getLayoutParams();
//			params.topMargin = 51;
//			webView.setLayoutParams(params);
//		}
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		checkLandscape();
//	
//	}
	
	
}
