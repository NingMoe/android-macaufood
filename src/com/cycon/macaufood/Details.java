package com.cycon.macaufood;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.Utilities;

public class Details extends BaseActivity {
	
	private static final String TAG = "Details";
	private ImageLoader imageLoader;
	private TextView branch;
	private TextView name, addr, website, cash, phone, businessHours, infoText;
	private ImageView imageView;
	private ImageView delivery, booking, midnight, party, buffet, banquet;
	private ImageView intro, info, menu;
	private GridView paymentGrid;
	private LinearLayout addrRow, phoneRow, websiteRow;
	private Button favoriteBtn;
	private Button feedbackBtn;
	private TextView alreadyInFavorite;
	private Cafe cafe;
	private ArrayList<Integer> paymentMethods = new ArrayList<Integer>();
	private FileCache fileCache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (Config.deviceWidth > 500) {
			setContentView(R.layout.details_qhd);
		} else {
			setContentView(R.layout.details);
		}
		
		imageView = (ImageView) findViewById(R.id.image);
		delivery = (ImageView) findViewById(R.id.delivery);
		booking = (ImageView) findViewById(R.id.booking);
		midnight = (ImageView) findViewById(R.id.midnight);
		party = (ImageView) findViewById(R.id.party);
		buffet = (ImageView) findViewById(R.id.buffet);
		banquet = (ImageView) findViewById(R.id.banquet);
		intro = (ImageView) findViewById(R.id.intro);
		info = (ImageView) findViewById(R.id.info);
		menu = (ImageView) findViewById(R.id.menu);
		name = (TextView) findViewById(R.id.name);
		branch = (TextView) findViewById(R.id.branch);
		addr = (TextView) findViewById(R.id.addr);
		phone = (TextView) findViewById(R.id.phone);
		website = (TextView) findViewById(R.id.website);
		businessHours = (TextView) findViewById(R.id.businessHours);
		infoText = (TextView) findViewById(R.id.infoText);
		cash = (TextView) findViewById(R.id.cash);
		paymentGrid = (GridView) findViewById(R.id.paymentGrid);
		addrRow = (LinearLayout) findViewById(R.id.addrRow);
		addrRow.setBackgroundDrawable(new ListView(this).getSelector());
		phoneRow = (LinearLayout) findViewById(R.id.phoneRow);
		phoneRow.setBackgroundDrawable(new ListView(this).getSelector());
		websiteRow = (LinearLayout) findViewById(R.id.websiteRow);
		websiteRow.setBackgroundDrawable(new ListView(this).getSelector());
		favoriteBtn = (Button) findViewById(R.id.favoriteBtn);
		feedbackBtn = (Button) findViewById(R.id.feedBackBtn);
		alreadyInFavorite = (TextView) findViewById(R.id.alreadyInFavorite);
		
		String id = getIntent().getStringExtra("id");
		
		if (Config.getInstance().getCafeLists().size() == 0) return;
		try {
			cafe = Config.getInstance().getCafeLists().get(Integer.parseInt(id) - 1);
		} catch (Exception e) {
			e.printStackTrace();
			// when cafe not exists in plist(normally wont happen)
			cafe = new Cafe();
			cafe.setId(id);
		}
		
		if (!cafe.getBranch().equals("0")) {
			name.setPadding(20, 7, 70, 7);
			branch.setVisibility(View.VISIBLE);
			branch.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					if (getIntent().getBooleanExtra("fromBranch", false)) {
						finish();
					} else {
						Intent i = new Intent(Details.this, Branch.class);
						i.putExtra("branch", cafe.getBranch());
						startActivity(i);
					}
				} 	
			});
		}
		

		
		name.setText(cafe.getName());
		addr.setText(cafe.getAddress());
		phone.setText(cafe.getPhone());
		final String websiteStr = cafe.getWebsite().trim();
		if (websiteStr.length() != 0 && !websiteStr.equals("-1")) {
			websiteRow.setVisibility(View.VISIBLE);
			findViewById(R.id.websiteRowSep).setVisibility(View.VISIBLE);
			website.setText(websiteStr);
			website.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteStr));
						startActivity(intent);
					} catch (ActivityNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
		}
		businessHours.setText(cafe.getOpenhours());
		infoText.setText(cafe.getDescription() + 
				(cafe.getMessage().equals("") ? "" : ("\n" + cafe.getMessage())));
		

        fileCache=new FileCache(this, ImageType.REGULAR);
        File f=fileCache.getFile(cafe.getId());
        Bitmap bitmap = decodeFile(f);
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap);
        } else {
        	imageView.setImageResource(R.drawable.nophoto);
        	new FetchImageTask().execute();
        }
		
		
//		imageLoader = new ImageLoader(this, 0, ImageType.REGULAR);
//		ArrayList<Cafe> list = new ArrayList<Cafe>(1);
//		list.add(cafe);
//		imageLoader.setImagesToLoadFromCafe(list);
//		imageLoader.displayImage(id, imageView, 0);
		
		if (Config.getInstance().getFavoriteLists().contains(cafe.getId())) {
			favoriteBtn.setVisibility(View.GONE);
			alreadyInFavorite.setVisibility(View.VISIBLE);
		} else {
			favoriteBtn.setVisibility(View.VISIBLE);
			alreadyInFavorite.setVisibility(View.GONE);
		}
		
		if (!cafe.getOption_phoneorder().equals("1")) delivery.setAlpha(50); 
		if (!cafe.getOption_booking().equals("1")) booking.setAlpha(50); 
		if (!cafe.getOption_night().equals("1")) midnight.setAlpha(50); 
		if (!cafe.getOption_call().equals("1")) party.setAlpha(50); 
		if (!cafe.getOption_buffet().equals("1")) buffet.setAlpha(50); 
		if (!cafe.getOption_banquet().equals("1")) banquet.setAlpha(50); 
		
		
		if (!cafe.getOption_intro().equals("1")) {
			intro.setAlpha(75); 
		} else {
			intro.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent i = new Intent(Details.this, Intro.class);
					i.putExtra("introid", cafe.getIntroid());
					i.putExtra("name", cafe.getName());
					i.putExtra("page", cafe.getIntropage());
					startActivity(i);
				}
			});
		}
		if (!cafe.getOption_recommend().equals("1")) {
			info.setAlpha(75); 
		} else {
			info.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent i = new Intent(Details.this, Info.class);
					i.putExtra("infoid", cafe.getRecommendid());
					i.putExtra("name", cafe.getName());
					i.putExtra("page", cafe.getRecommendpage());
					startActivity(i);
				}
			});
		}
		if (!cafe.getOption_menu().equals("1")) {
			menu.setAlpha(75); 
		} else {
			menu.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent i = new Intent(Details.this, Menu.class);
					i.putExtra("menuid", cafe.getMenuid());
					i.putExtra("name", cafe.getName());
					i.putExtra("page", cafe.getMenupage());
					i.putExtra("phone", cafe.getPhone());
					startActivity(i);
				}
			});
		}
		

		favoriteBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Toast.makeText(Details.this, "成功加入", Toast.LENGTH_SHORT).show();
				favoriteBtn.setVisibility(View.GONE);
				alreadyInFavorite.setVisibility(View.VISIBLE);
				SharedPreferences prefs = getSharedPreferences(
						"macaufood.preferences", 0);
				Editor prefsPrivateEditor = prefs.edit();
				String str = prefs.getString("favorites", "");
				prefsPrivateEditor.putString("favorites", str + cafe.getId() + ",");
				prefsPrivateEditor.commit();
				Config.getInstance().getFavoriteLists().add(cafe.getId());
//				if (Config.isOnline(Details.this))
//					new SendFavoriteLogTask().execute();
			}
		});
		
		feedbackBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {
				Intent i = new Intent(Details.this, FeedBack.class);
				i.putExtra("id", cafe.getId());
				startActivity(i);
			}
		});
		

		if (cafe.getAddress().trim().length() != 0) {
			addrRow.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					if (getIntent().getBooleanExtra("fromMap", false)) {
						finish();
					} else {
						Intent i = new Intent(Details.this, Map.class);
						i.putExtra("coordx", cafe.getCoordx());
						i.putExtra("coordy", cafe.getCoordy());
						i.putExtra("name", cafe.getName());
						i.putExtra("id", cafe.getId());
						startActivity(i);
					}
				}
			});
		}
		
		if (cafe.getPhone().trim().length() != 0) {
			phoneRow.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					
					ArrayList<String> phoneNos = PhoneUtils.getPhoneStr(cafe.getPhone());
					if (phoneNos.size() == 0) return;
					
					final String number1 = phoneNos.get(0);
					final String number2 = phoneNos.size() == 1 ? null : phoneNos.get(1);
					
					AlertDialog dialog = new AlertDialog.Builder(Details.this)
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
					.setNegativeButton("取消",
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
		
		if (cafe.getPayment().contains(",")) {
			cash.setVisibility(View.GONE);
			paymentGrid.setVisibility(View.VISIBLE);
			String[] strs = cafe.getPayment().split(",");
			for (String str : strs) {
				paymentMethods.add(Integer.parseInt(str));
			}
		
			if (paymentMethods.size() > 8) {
				LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) paymentGrid.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, (float) 80,
						getResources().getDisplayMetrics());
				paymentGrid.setLayoutParams(params);
			} else if (paymentMethods.size() > 4) {
				LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) paymentGrid.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, (float) 53,
						getResources().getDisplayMetrics());
				paymentGrid.setLayoutParams(params);
			}
			paymentGrid.setAdapter(new PaymentAdapter());
		
		}
		

		if (Config.isOnline(Details.this))
			new SendDetailsLogTask().execute();
		
	}
	
//	private class SendFavoriteLogTask extends AsyncTask<Void, Void, Void> {
//		
//		@Override
//		protected Void doInBackground(Void... params) {
//			
//			int idValue = Integer.parseInt(cafe.getId()) - 1;
//			
//			String urlStr = "http://www.cycon.com.mo/xml_favouritelog.php?key=cafecafe&udid=android-" + 
//				Config.DEVICE_ID + "&cafeid=" + idValue;
//			
//            try {
//            	HttpClient client = new DefaultHttpClient();
//            	HttpParams httpParams = client.getParams();
//            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
//            	HttpGet request = new HttpGet(urlStr);
//            	client.execute(request);
//            	
//			} catch (MalformedURLException e) {
//				ETLog.e(TAG, "malformed url exception");
//				e.printStackTrace();
//				return null;
//			} catch (IOException e) {
//				ETLog.e(TAG, "io exception");
//				e.printStackTrace();
//				return null;
//			} catch (Exception e) {
//				ETLog.e(TAG, "exception");
//				e.printStackTrace();
//				return null;
//			}
//			
//			return null;
//		}
//	}
	
	private class SendDetailsLogTask extends AsyncTask<Void, Void, Void> {
		
		@Override
		protected Void doInBackground(Void... params) {
			
			int idValue = Integer.parseInt(cafe.getId()) - 1;
			
			String urlStr = "http://www.cycon.com.mo/xml_detaillog2.php?key=cafecafe&udid=android-" + 
					Config.DEVICE_ID + "&cafeid=" + idValue + "&source=&type0=" + cafe.getType0() + 
					"&type1=" + cafe.getType1() + "&type2=" + cafe.getType2() + "&district=" + cafe.getDistrict();
			
            try {
            	HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            	HttpGet request = new HttpGet(urlStr);
            	client.execute(request);
            	
			} catch (MalformedURLException e) {
				ETLog.e(TAG, "malformed url exception");
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				ETLog.e(TAG, "io exception");
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				ETLog.e(TAG, "exception");
				e.printStackTrace();
				return null;
			}
			
			return null;
		}
	}
	
	private class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {

		private boolean noConnection;
		
		@Override
		protected Bitmap doInBackground(Void... arg0) {
			
	        //from web
	        try {
	            Bitmap bitmap=null;

				String urlStr = "http://www.cycon.com.mo/appimages/cafephoto/" + cafe.getId() + ".jpg";
				
				HttpClient client = new DefaultHttpClient();
            	HttpParams httpParams = client.getParams();
            	HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            	HttpGet request = new HttpGet(urlStr);
            	HttpResponse response = client.execute(request);
            	InputStream is= response.getEntity().getContent();
	            File f=fileCache.getFile(cafe.getId());
	            OutputStream os = new FileOutputStream(f);
	            Utilities.CopyStream(is, os);
	            os.close();
	            bitmap = decodeFile(f);

	            if (bitmap == null) ETLog.e(TAG, "decode returns null");
	            return bitmap;
	        } catch (FileNotFoundException ex){
	        	ETLog.e(TAG, "no photo");
	           ex.printStackTrace();
	        	   return null;
	        } catch (Exception e) {
	        	noConnection = true;
	        	//socket error here
	        	ETLog.e(TAG, "error = " + e.getMessage());
	        	return null;
	        }
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				imageView.setImageBitmap(result);
			} else {
				if (noConnection) {
					imageView.setImageResource(R.drawable.nointernet);
				} 
			}
		}
		
		
	}
	
    private Bitmap decodeFile(File f){
        try {
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
        	ETLog.e(TAG, "filenotfounde");
        }
        return null;
    }
	
	
	private class PaymentAdapter extends BaseAdapter {

		public int getCount() {
			return paymentMethods.size();
		}

		public Object getItem(int arg0) {
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		public View getView(int position, View convertView, ViewGroup arg2) {
			int paymentValue = paymentMethods.get(position);
			if (position == 0) {
				TextView text = new TextView(Details.this);
				text.setText(" 現金");
				text.setTextSize(17);
				return text; 
			}
			
			ImageView image = null;
			
			if (convertView != null && convertView instanceof ImageView) {
				image = (ImageView) convertView;
			} else {
				image = new ImageView(Details.this);
			}
			
			switch(paymentValue) {
			case 1:
				image.setImageResource(R.drawable.payment1);
				break;
			case 2:
				image.setImageResource(R.drawable.payment2);
				break;
			case 3:
				image.setImageResource(R.drawable.payment3);
				break;
			case 4:
				image.setImageResource(R.drawable.payment4);
				break;
			case 5:
				image.setImageResource(R.drawable.payment5);
				break;
			case 6:
				image.setImageResource(R.drawable.payment6);
				break;
			case 7:
				image.setImageResource(R.drawable.payment7);
				break;
			case 8:
				image.setImageResource(R.drawable.payment8);
				break;
			case 9:
				image.setImageResource(R.drawable.payment9);
				break;
			default:
				image.setImageResource(0);
			}
			
			return image;
			
		}
		
	}
}
