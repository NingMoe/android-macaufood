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

public class PSHeaderView extends LinearLayout {
	
	private int mCafeId;
	
	public PSHeaderView(Context context) {
		super(context);
		init();
	}
	
	public PSHeaderView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init();
	}
	
	private void init() {
		
	}
	
	public ViewHolder initView() {
		ViewHolder holder = new ViewHolder();
    	holder.profilePic = (ImageView) findViewById(R.id.profilePic);
    	holder.userName = (TextView) findViewById(R.id.userName);
    	holder.cafeName = (TextView) findViewById(R.id.cafeName);
    	holder.time = (TextView) findViewById(R.id.time);
		return holder;
	}
	
	public void loadInfo(ParsedPSHolder pInfo, ViewHolder holder) {
		
		try {
			mCafeId = Integer.parseInt(pInfo.getCafeid());
		} catch (NumberFormatException e) {
			mCafeId = -1;
			e.printStackTrace();
		}
		
		MFService.loadImage(getContext().getApplicationContext(), ImageType.PSLOCALAVATAR, pInfo.getMemberid(), holder.profilePic, false, false);
		
		holder.userName.setText(pInfo.getName());
		holder.cafeName.setText(pInfo.getPlace());
		holder.time.setText(pInfo.getUploaddate());
	}
	
    public static class ViewHolder {
    	ImageView profilePic;
    	TextView userName;
    	TextView cafeName;
    	TextView time;
    }
	
}
