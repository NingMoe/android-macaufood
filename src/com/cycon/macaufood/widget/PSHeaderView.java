package com.cycon.macaufood.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import com.cycon.macaufood.utilities.MFLog;

import android.util.AttributeSet;
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
import com.cycon.macaufood.activities.Details;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.bean.ImageType;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.AsyncTaskHelper;
import com.cycon.macaufood.utilities.FeedBackDialogHelper;
import com.cycon.macaufood.utilities.FileCache;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class PSHeaderView extends RelativeLayout {
	
	private Context mContext;
	
	public PSHeaderView(Context context) {
		super(context);
		init(context);
	}
	
	public PSHeaderView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
	}
	
	public ViewHolder initView() {
		ViewHolder holder = new ViewHolder();
    	holder.profilePic = (ImageView) findViewById(R.id.profilePic);
    	holder.userName = (TextView) findViewById(R.id.userName);
    	holder.cafeName = (TextView) findViewById(R.id.cafeName);
    	holder.time = (TextView) findViewById(R.id.time);
		return holder;
	}
	
	public void loadInfo(ParsedPSHolder pInfo, ViewHolder holder, ImageLoader imageLoader, int pos) {
		imageLoader.displayImage(pInfo.getMemberid(), holder.profilePic, pos);
		
		holder.userName.setText(pInfo.getName());
		long time = Long.parseLong(pInfo.getUploaddate());
		holder.time.setText(MFUtil.getPastTime(time, mContext) + mContext.getResources().getString(R.string.before));
		
		String cafeNameText = pInfo.getPlace();
		if (cafeNameText.equals("(null)")) {
			holder.cafeName.setVisibility(View.GONE);
		} else {
			holder.cafeName.setVisibility(View.VISIBLE);
			holder.cafeName.setText(pInfo.getPlace());
			holder.cafeName.setTextColor(mContext.getResources().getColor(R.color.dark_gray_text));
			holder.cafeName.setTypeface(null, Typeface.NORMAL);
			
			final String cafeId = pInfo.getCafeid();
			if (!cafeId.equals("0") && MFConfig.getInstance().getCafeLists().size() >= Integer.parseInt(cafeId)) {
				try {
					holder.cafeName.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							Intent i = new Intent(mContext, Details.class);
							i.putExtra("id", cafeId);
							mContext.startActivity(i);
						}
					});
					
					holder.cafeName.setTextColor(mContext.getResources().getColor(R.color.green_text));
					holder.cafeName.setTypeface(null, Typeface.BOLD);
					
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} 
		}
		
		
	}
	
    public static class ViewHolder {
    	public ImageView profilePic;
    	TextView userName;
    	TextView cafeName;
    	TextView time;
    }
	
}
