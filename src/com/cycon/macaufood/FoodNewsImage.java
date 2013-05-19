package com.cycon.macaufood;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FoodNewsImage extends BaseActivity {
	
	private static final String TAG = FoodNewsImage.class.getName();
	private String foodnews_id;
	private boolean isError;
	
	private TextView errorMsg;
	private WebView webView;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.foodnews_image);
		foodnews_id = getIntent().getStringExtra("foodnews_id");
		errorMsg = (TextView) findViewById(R.id.errorMsg);
		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
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
		
		
	}
	
	private String getUrl() {
		return "http://www.cycon.com.mo/appimages/article_content/" + foodnews_id + ".jpg";
	}
	
	
}
