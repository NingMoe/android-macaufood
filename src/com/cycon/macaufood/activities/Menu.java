package com.cycon.macaufood.activities;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.PhoneUtils;

public class Menu extends BaseActivity {
	
	private static final String TAG = Menu.class.getName();
	private static final int PREV_PAGE_MENU_ID = 1;
	private static final int NEXT_PAGE_MENU_ID = 2;
	private static final int PHONE_CALL_MENU_ID = 3;
	private String menuid;
	private int totalPages;
	private int currentPage = 1;
	private boolean isError;
	
	private TextView errorMsg;
	private WebView webView;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		menuid = getIntent().getStringExtra("menuid");
		totalPages = Integer.parseInt(getIntent().getStringExtra("page"));
		if (totalPages == 0) totalPages = 1;
		errorMsg = (TextView) findViewById(R.id.errorMsg);
		webView = (WebView) findViewById(R.id.webView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		setHeaderText();
		
		webView.loadUrl(MFURL.getMenuUrl(menuid, currentPage));
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		webView.getSettings().setUseWideViewPort(true) ; 
		webView.setInitialScale(140);
		
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
	
	private void showPhoneCallDialog() {
		ArrayList<String> phoneNos = PhoneUtils.getPhoneStr(getIntent().getStringExtra("phone"));
		if (phoneNos.size() == 0) return;
		
		final String number1 = phoneNos.get(0);
		final String number2 = phoneNos.size() == 1 ? null : phoneNos.get(1);
		
		AlertDialog dialog = new AlertDialog.Builder(Menu.this)
		.setTitle("撥打電話")
		.setMessage(number1)
		.setNeutralButton(number2 == null ? "撥打" : "打電話1",
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
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "打電話2", 
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
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		if (totalPages > 1) {
			menu.add(0, PREV_PAGE_MENU_ID, 1, R.string.prevPage).setIcon(R.drawable.ic_prev_page).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(0, NEXT_PAGE_MENU_ID, 2, R.string.nextPage).setIcon(R.drawable.ic_next_page).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
		}
		if (!getIntent().getStringExtra("phone").equals(""))
			menu.add(0, PHONE_CALL_MENU_ID, 3, R.string.callPhone).setIcon(R.drawable.ic_call).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case PREV_PAGE_MENU_ID:
			errorMsg.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			if (currentPage == 1) currentPage = totalPages;
			else currentPage--;
			setHeaderText();
			webView.loadUrl(MFURL.getMenuUrl(menuid, currentPage));
			return true;
		case NEXT_PAGE_MENU_ID:
			errorMsg.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			if (currentPage == totalPages) currentPage = 1;
			else currentPage++;
			setHeaderText();
			webView.loadUrl(MFURL.getMenuUrl(menuid, currentPage));
			return true;
		case PHONE_CALL_MENU_ID:
			showPhoneCallDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void setHeaderText() {
		setTitle(getString(R.string.menu) + currentPage + "/" + totalPages + " ~ " + getIntent().getStringExtra("name"));
	}
	
	
}
