package com.cycon.macaufood.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;

public class FoodNewsImage extends BaseActivity {

	private static final String TAG = FoodNewsImage.class.getName();
	private static final int DETAILS_MENU_ID = 1;
	private String foodnews_id;
	private boolean isError;

	private TextView errorMsg;
	private WebView webView;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.foodnews_image);
		setTitle(getString(R.string.foodNews) + " ~ " + getIntent().getStringExtra("foodnews_name"));
		foodnews_id = getIntent().getStringExtra("foodnews_id");
		errorMsg = (TextView) findViewById(R.id.errorMsg);
		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		webView.loadUrl(getUrl());
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setUseWideViewPort(true) ; 
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
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(0, DETAILS_MENU_ID, 0, R.string.cafeDetails).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case DETAILS_MENU_ID:
			Intent i = new Intent(FoodNewsImage.this, Details.class);
			i.putExtra("id", getIntent().getStringExtra("cafe_id"));
			startActivity(i);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private String getUrl() {
		return "http://www.cycon.com.mo/appimages/article_content/"
				+ foodnews_id + ".jpg";
	}

}
