package com.cycon.macaufood.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Hashtable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PreferenceHelper;
import com.cycon.macaufood.widget.GalleryNavigator;
import com.cycon.macaufood.widget.OneFlingGallery;

public class Info extends BaseActivity implements ViewSwitcher.ViewFactory {

	private static final String TAG = Info.class.getName();
	private ProgressDialog pDialog;
	// private Gallery gallery;
	private GalleryNavigator navi;
	private TextSwitcher textSwitcher;
	private String infoid;
	private int totalPages;
	private int serverTotalPages;
	private Hashtable<Integer, Bitmap> imageMap;
	private Hashtable<Integer, String> textMap;
	private ImageAdapter imageAdapter;
	private ViewPager viewPager;
	private boolean finishLoadingFirstImage;
	private FileCache fileCache;
	private static final long REFRESH_TIME_PERIOD = 3600 * 1000 * 24 * 7; // 2
																			// days

	private Hashtable<Integer, ImageView> imageViewsMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.intro);
		setTitle(getString(R.string.information) + " ~ "
				+ getIntent().getStringExtra("name"));
		infoid = getIntent().getStringExtra("infoid");
		totalPages = Integer.parseInt(getIntent().getStringExtra("page"));
		if (totalPages == 0)
			totalPages = 1;
		viewPager = (ViewPager) findViewById(R.id.pager);
		// gallery = (Gallery) findViewById(R.id.gallery);
		navi = (GalleryNavigator) findViewById(R.id.navi);
		textSwitcher = (TextSwitcher) findViewById(R.id.text);
		textSwitcher.setFactory(this);

		Animation in = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in);
		Animation out = AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out);
		textSwitcher.setInAnimation(in);
		textSwitcher.setOutAnimation(out);
		fileCache = new FileCache(this, ImageType.INFO);

		textMap = new Hashtable<Integer, String>(totalPages);
		imageMap = new Hashtable<Integer, Bitmap>(totalPages);
		imageViewsMap = new Hashtable<Integer, ImageView>(totalPages);
		imageAdapter = new ImageAdapter(this);

		// gallery.setAdapter(imageAdapter);
		// gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
		//
		// public void onItemSelected(AdapterView<?> arg0, View arg1, int id,
		// long arg3) {
		//
		//
		// }

		// public void onNothingSelected(AdapterView<?> arg0) {
		//
		// }
		// });

		boolean cacheError = false;

		try {
			File f = fileCache.getFile(infoid + "-page");
			FileInputStream fis = new FileInputStream(f);
			BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
			String pageStr = rd.readLine().trim();

			serverTotalPages = Integer.parseInt(pageStr);
			navi.setSize(serverTotalPages);
			navi.setVisibility(View.GONE);
			navi.setVisibility(View.VISIBLE);

			for (int i = 1; i <= serverTotalPages; i++) {
				f = fileCache.getFile(infoid + "-" + i + "-image");
				fis = new FileInputStream(f);
				imageMap.put(i,
						BitmapFactory.decodeStream(new FlushedInputStream(fis)));
				imageAdapter.notifyDataSetChanged();
			}
			for (int i = 1; i <= serverTotalPages; i++) {
				StringBuilder sb = new StringBuilder();
				f = fileCache.getFile(infoid + "-" + i + "-text");
				fis = new FileInputStream(f);
				rd = new BufferedReader(new InputStreamReader(fis));
				String line = null;
				while ((line = rd.readLine()) != null) {
					sb.append(line + "\n");
				}
				textMap.put(i, sb.toString());
			}

		} catch (Exception e) {
			cacheError = true;
		}

		if (cacheError
				|| imageMap.isEmpty()
				|| textMap.isEmpty()
				|| System.currentTimeMillis()
						- PreferenceHelper.getPreferenceValueLong(
								getApplicationContext(), "infoTimeStamp", 0) > REFRESH_TIME_PERIOD) {
			if (!MFConfig.isOnline(Info.this)) {
				if (imageMap.isEmpty() || textMap.isEmpty())
					textSwitcher.setText(getString(R.string.noInternetMsg));
			} else {
				AsyncTaskHelper.executeWithResultString(new FetchPageTask());
				// load first 2 photos first
				for (int i = 1; i <= 2; i++) {
					AsyncTaskHelper.executeWithResultBitmap(new FetchImageTask(
							i));
					AsyncTaskHelper
							.executeWithResultString(new FetchTextTask(i));
				}
			}
		}
		
		viewPager.setAdapter(imageAdapter);
		viewPager.setOnPageChangeListener(imageAdapter);
		imageAdapter.onPageSelected(0);
	}

	public void refresh() {
		if (MFConfig.isOnline(Info.this)) {
			AsyncTaskHelper.executeWithResultString(new FetchPageTask());
			// load first 2 photos first
			for (int i = 1; i <= 2; i++) {
				AsyncTaskHelper.executeWithResultBitmap(new FetchImageTask(i));
				AsyncTaskHelper.executeWithResultString(new FetchTextTask(i));
			}
		}
	}

	public class FetchPageTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			String urlStr = "http://www.cycon.com.mo/detail_page.php?id="
					+ infoid;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpParams httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpGet request = new HttpGet(urlStr);
				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();

				File f = fileCache.getFile(infoid + "-page");
				OutputStream os = new FileOutputStream(f);
				MFUtil.CopyStream(is, os);
				os.close();

				FileInputStream fis = new FileInputStream(f);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						fis));
				String pageStr = rd.readLine().trim();
				rd.close();

				try {
					serverTotalPages = Integer.parseInt(pageStr);
				} catch (NumberFormatException e) {
					serverTotalPages = totalPages;
					e.printStackTrace();
				}

			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} catch (Exception e) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			navi.setSize(serverTotalPages);
			navi.setVisibility(View.GONE);
			navi.setVisibility(View.VISIBLE);
			imageAdapter.notifyDataSetChanged();
			if (finishLoadingFirstImage) {
				for (int i = 3; i <= serverTotalPages; i++) {
					AsyncTaskHelper.executeWithResultBitmap(new FetchImageTask(
							i));
					AsyncTaskHelper
							.executeWithResultString(new FetchTextTask(i));
				}
			}
		}
	}

	public class FetchTextTask extends AsyncTask<Void, Void, String> {

		private int page;

		public FetchTextTask(int page) {
			this.page = page;
		}

		@Override
		protected String doInBackground(Void... params) {

			StringBuilder sb = new StringBuilder();

			String urlStr = "http://www.cycon.com.mo/detail_text.php?id="
					+ infoid + "&page=" + page;

			try {
				HttpClient client = new DefaultHttpClient();
				HttpParams httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpGet request = new HttpGet(urlStr);
				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();

				File f = fileCache.getFile(infoid + "-" + page + "-text");
				OutputStream os = new FileOutputStream(f);
				MFUtil.CopyStream(is, os);
				os.close();

				FileInputStream fis = new FileInputStream(f);
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						fis));
				String line = null;
				while ((line = rd.readLine()) != null) {
					sb.append(line + "\n");
				}
				rd.close();
				return sb.toString();

			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			} catch (Exception e) {

			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			// if 2nd image is null, do not show internet msg coz there may only
			// be 1
			if (result == null) {
				if (page != 2 && (imageMap.isEmpty() || textMap.isEmpty()))
					textSwitcher.setText(getString(R.string.noInternetMsg));
			} else {
				if (page == navi.getPosition() + 1) {
					textSwitcher.setText(result);

				}

				textMap.put(page, result);
			}

		}
	}

	public class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {

		private int page;

		public FetchImageTask(int page) {
			this.page = page;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (page == 1)
				pDialog = ProgressDialog.show(Info.this, null, "載入資料中...",
						false, true);
		}

		@Override
		protected Bitmap doInBackground(Void... params) {

			String urlStr = "http://www.cycon.com.mo/appimages/intro/" + infoid
					+ "-" + page + ".jpg";
			try {
				HttpClient client = new DefaultHttpClient();
				HttpParams httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpGet request = new HttpGet(urlStr);
				HttpResponse response = client.execute(request);
				InputStream is = response.getEntity().getContent();

				File f = fileCache.getFile(infoid + "-" + page + "-image");
				OutputStream os = new FileOutputStream(f);
				MFUtil.CopyStream(is, os);
				os.close();

				FileInputStream fis = new FileInputStream(f);
				return BitmapFactory.decodeStream(new FlushedInputStream(fis));

			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);

			if (pDialog != null && page == 1) {
				pDialog.dismiss();
			}

			if (result == null) {
				if (page != 2 && (imageMap.isEmpty() || textMap.isEmpty()))
					textSwitcher.setText(getString(R.string.noInternetMsg));
			} else {
				imageMap.put(page, result);
				ImageView imageView = imageViewsMap.get(page);
				if (imageView != null) {
					int height = MFConfig.deviceWidth * result.getHeight()
							/ result.getWidth();
					viewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
							height));
					imageView.setImageBitmap(result);
				}

				// load rest photos
				if (page == 1) {
					PreferenceHelper.savePreferencesLong(
							getApplicationContext(), "infoTimeStamp",
							System.currentTimeMillis());
					imageAdapter.notifyDataSetChanged();
					finishLoadingFirstImage = true;

					for (int i = 3; i <= serverTotalPages; i++) {
						AsyncTaskHelper
								.executeWithResultBitmap(new FetchImageTask(i));
						AsyncTaskHelper
								.executeWithResultString(new FetchTextTask(i));
					}
				}
			}

		}
	}

	@Override
	protected void onDestroy() {
		imageMap.clear();
		System.gc();
		super.onDestroy();
	}

	public class ImageAdapter extends PagerAdapter implements
			ViewPager.OnPageChangeListener {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return serverTotalPages;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView imageView = new ImageView(mContext);
			imageViewsMap.put(position + 1, imageView);
			Bitmap bmp = imageMap.get(position + 1);
			if (bmp == null) {
				((ViewPager) container).addView(imageView, 0);
				return imageView;
			}
			int height = MFConfig.deviceWidth * bmp.getHeight()
					/ bmp.getWidth();
			viewPager.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					height));
			imageView.setImageBitmap(bmp);
			((ViewPager) container).addView(imageView, 0);
			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}


		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		public void onPageSelected(int position) {
			String s = textMap.get(position + 1);
			// prevent sliding text even when text is same
			if (s != null) {
				if (!((TextView) textSwitcher.getCurrentView()).getText()
						.toString().equals(s)) {
					textSwitcher.setText(s);
				}
			}
			navi.setPosition(position);
			navi.invalidate();
		}
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int bytes = read();
					if (bytes < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	public View makeView() {
		TextView t = new TextView(this);
		t.setTextSize(17);
		int px = MFUtil.getPixelsFromDip(10f, getResources());
		t.setPadding(px, px, px, px);
		return t;
	}
}
