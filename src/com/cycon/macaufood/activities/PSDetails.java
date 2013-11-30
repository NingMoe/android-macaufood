package com.cycon.macaufood.activities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

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
import com.cycon.macaufood.utilities.MFLog;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FeedBackDialogHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class PSDetails extends BaseActivity {
	
	private static final String TAG = "PSDetails";
//	private static final int FAVORITE_MENU_ID = 1;
//	private static final int BRANCH_MENU_ID = 2;
//	private static final int FEEDBACK_MENU_ID = 3;
//	private TextView name, addr, website, cash, phone, businessHours, infoText;
//	private ImageView imageView;
//	private ImageView delivery, booking, midnight, party, buffet, banquet;
//	private ImageView intro, info, menu;
//	private GridView paymentGrid;
//	private LinearLayout addrRow, phoneRow, websiteRow;
//	private View websiteRowSep;
//	private Cafe cafe;
//	private ArrayList<Integer> paymentMethods = new ArrayList<Integer>();
//	private FileCache fileCache;
//	private boolean isFavorite;
	
	private ParsedPSHolder mPsHotInfo;
	private ImageView profilePic;
	private ImageView photoImage;
	private TextView userName;
	private TextView cafeName;
	private TextView time;
	private int cafeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ps_detail);
		
		int pos = getIntent().getIntExtra("ps_hot_position", 0);
		mPsHotInfo = MFConfig.getInstance().getPsHotList().get(pos);
		
		String title = mPsHotInfo.getCaption();
		if (title.trim().length() == 0) title = getString(R.string.psHot);
		setTitle(title);
		
		try {
			cafeId = Integer.parseInt(mPsHotInfo.getCafeid());
		} catch (NumberFormatException e) {
			cafeId = -1;
			e.printStackTrace();
		}
		
		profilePic = (ImageView) findViewById(R.id.profilePic);
		photoImage = (ImageView) findViewById(R.id.photoImage);
		int width = Integer.parseInt(mPsHotInfo.getImgwidth());
		int height = Integer.parseInt(mPsHotInfo.getImgheight());
		int padding = MFUtil.getPixelsFromDip(22, getResources());
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) photoImage.getLayoutParams();
		int imageHeight = (MFConfig.deviceWidth - padding) * height / width;
		params.height = imageHeight;
		photoImage.setLayoutParams(params);
		
		MFService.loadImage(getApplicationContext(), ImageType.PHOTOSHARE_HOT, "image-" + mPsHotInfo.getPhotoid() + "-1.jpg", photoImage, true, false);
		MFService.loadImage(getApplicationContext(), ImageType.PSLOCALAVATAR, mPsHotInfo.getMemberid(), profilePic, false, false);
		userName = (TextView) findViewById(R.id.userName);
		cafeName = (TextView) findViewById(R.id.cafeName);
		time = (TextView) findViewById(R.id.time);
		
		userName.setText(mPsHotInfo.getName());
		cafeName.setText(mPsHotInfo.getPlace());
		time.setText(mPsHotInfo.getUploaddate());
		
	}
	
}
