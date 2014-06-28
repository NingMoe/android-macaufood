package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.cycon.macaufood.utilities.MFLog;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FeedBackDialogHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class Details extends BaseActivity {
	
	private static final String TAG = "Details";
	private static final int FAVORITE_MENU_ID = 0;
	private static final int SHARE_MENU_ID = 1;
	private static final int BRANCH_MENU_ID = 2;
	private static final int FEEDBACK_MENU_ID = 3;
	private TextView name, addr, website, cash, phone, businessHours, infoText;
	private ImageView imageView;
	private ImageView delivery, booking, midnight, party, buffet, banquet;
	private ImageView intro, info, menu;
	private GridView paymentGrid;
	private LinearLayout addrRow, phoneRow, websiteRow;
	private View websiteRowSep;
	private Cafe cafe;
	private ArrayList<Integer> paymentMethods = new ArrayList<Integer>();
	private FileCache fileCache;
	private boolean isFavorite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (MFConfig.deviceWidth > 500) {
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
		addr = (TextView) findViewById(R.id.addr);
		phone = (TextView) findViewById(R.id.phone);
		website = (TextView) findViewById(R.id.website);
		businessHours = (TextView) findViewById(R.id.businessHours);
		infoText = (TextView) findViewById(R.id.infoText);
		cash = (TextView) findViewById(R.id.cash);
		paymentGrid = (GridView) findViewById(R.id.paymentGrid);
		addrRow = (LinearLayout) findViewById(R.id.addrRow);
		phoneRow = (LinearLayout) findViewById(R.id.phoneRow);
		websiteRow = (LinearLayout) findViewById(R.id.websiteRow);
		websiteRowSep = findViewById(R.id.websiteRowSep);
		
		String id = getIntent().getStringExtra("id");
		
		if (MFConfig.getInstance().getCafeLists().size() == 0) return;
		try {
			cafe = MFConfig.getInstance().getCafeLists().get(Integer.parseInt(id) - 1);
		} catch (Exception e) {
			e.printStackTrace();
			// when cafe not exists in plist(normally wont happen)
			cafe = new Cafe();
			cafe.setId(id);
		}
		
		
		setTitle(cafe.getName());
		name.setText(cafe.getName());
		if (cafe.getName().length() >= 19) {
			name.setTextSize(20f);
		}
		addr.setText(cafe.getAddress());
		phone.setText(cafe.getPhone());
		final String websiteStr = cafe.getWebsite().trim();
		if (websiteStr.length() != 0 && !websiteStr.equals("-1")) {
			websiteRow.setVisibility(View.VISIBLE);
			websiteRowSep.setVisibility(View.VISIBLE);
			website.setText(websiteStr);
			websiteRow.setOnClickListener(new OnClickListener() {
				
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
		

		imageView.setImageResource(R.drawable.nophoto);
        MFService.loadImage(getApplicationContext(), ImageType.REGULAR, cafe.getId(), imageView, true, false);
		
		
		if (MFConfig.getInstance().getFavoriteLists().contains(cafe.getId())) {
			isFavorite = true;
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
					.setNeutralButton(number2 == null ? "撥打" : "打電話1",
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
			});
		
		}
		
		if (cafe.getPayment().contains(",")) {
			cash.setVisibility(View.GONE);
			paymentGrid.setVisibility(View.VISIBLE);
			String[] strs = cafe.getPayment().split(",");
			for (String str : strs) {
				try {
					paymentMethods.add(Integer.parseInt(str));
				} catch (NumberFormatException e) {
				}
			}
		
			if (paymentMethods.size() > 8) {
				LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) paymentGrid.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, (float) 85,
						getResources().getDisplayMetrics());
				paymentGrid.setLayoutParams(params);
			} else if (paymentMethods.size() > 4) {
				LinearLayout.LayoutParams params = (android.widget.LinearLayout.LayoutParams) paymentGrid.getLayoutParams();
				params.height = (int) TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, (float) 58,
						getResources().getDisplayMetrics());
				paymentGrid.setLayoutParams(params);
			} 
			paymentGrid.setAdapter(new PaymentAdapter());
		
		}
		

		int idValue = Integer.parseInt(cafe.getId()) - 1;
		
		String urlStr = MFURL.CAFE_DETAILS_LOG + 
				MFConfig.DEVICE_ID + "&cafeid=" + idValue + "&source=&type0=" + cafe.getType0() + 
				"&type1=" + cafe.getType1() + "&type2=" + cafe.getType2() + "&district=" + cafe.getDistrict();
		MFService.sendRequest(urlStr, getApplicationContext());
	}
	
	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(0, FAVORITE_MENU_ID, 0, R.string.addFavorite).setIcon(isFavorite ? R.drawable.ic_bookmark : R.drawable.ic_bookmark_empty).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.add(0, SHARE_MENU_ID, 1, R.string.share).setIcon(R.drawable.ic_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		if (!cafe.getBranch().equals("0")) {
			menu.add(0, BRANCH_MENU_ID, 2, R.string.branch).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
		}
		menu.add(0, FEEDBACK_MENU_ID, 3, R.string.feedBack).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case FAVORITE_MENU_ID:
			toggleAddFavorite(item);
			return true;
		case BRANCH_MENU_ID:
			if (getIntent().getBooleanExtra("fromBranch", false)) {
				finish();
			} else {
				Intent i = new Intent(Details.this, Branch.class);
				i.putExtra("branch", cafe.getBranch());
				startActivity(i);
			}
			return true;
		case FEEDBACK_MENU_ID:
			FeedBackDialogHelper.showFeedBackDialog(this, getLayoutInflater(), getIntent().getStringExtra("id"));
			return true;
		case SHARE_MENU_ID:
			View v = findViewById(android.R.id.content).getRootView();
		    v.setDrawingCacheEnabled(true);
		    Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
		    v.setDrawingCacheEnabled(false);
		    int statusBarHeight = MFUtil.getStatusBarHeight(this);
		    bitmap = Bitmap.createBitmap(bitmap, 0, statusBarHeight, bitmap.getWidth(), bitmap.getHeight() - statusBarHeight, null, true);
			File file = MFUtil.getOutputMediaFile(MFUtil.MEDIA_TYPE_IMAGE);
			if (file == null) {
				Toast.makeText(this, R.string.shareFailed,
						Toast.LENGTH_LONG).show();
				return false;
			}
			FileOutputStream fout;
			try {
				fout = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
				fout.flush();
				fout.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Intent shareIntent = new Intent();
			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, cafe.getName() + "\nhttp://www.ifoodmacau.com/cafe/" + cafe.getId());
			shareIntent.putExtra(Intent.EXTRA_STREAM, 
					Uri.fromFile(file));
			shareIntent.setType("image/*");
			startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.shareTo)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void toggleAddFavorite(MenuItem item) {
		
			if (isFavorite) {
				isFavorite = false;
				item.setIcon(R.drawable.ic_bookmark_empty);
				MFConfig.getInstance().getFavoriteLists().remove(cafe.getId());
				Toast.makeText(Details.this, getString(R.string.alreadyRemoveFromFavorite), Toast.LENGTH_SHORT).show();
			} else {
				isFavorite = true;
				item.setIcon(R.drawable.ic_bookmark);
				MFConfig.getInstance().getFavoriteLists().add(cafe.getId());
				Toast.makeText(Details.this, getString(R.string.alreadyInFavorite), Toast.LENGTH_SHORT).show();
			}
			StringBuilder sb = new StringBuilder();
			for (String id : MFConfig.getInstance().getFavoriteLists()) {
				sb.append(id);
				sb.append(',');
			}
			PreferenceHelper.savePreferencesStr(this, "favorites", sb.toString());
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
			case 10:
				image.setImageResource(R.drawable.payment10);
				break;
			default:
				image.setImageResource(0);
			}
			
			return image;
			
		}
		
	}
}
