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
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class PSDetailsView extends LinearLayout {
	
	private int mCafeId;
	public ImageLoader imageLoader; 
	
	public PSDetailsView(Context context) {
		super(context);
		init(context);
	}
	
	public PSDetailsView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}
	
	private void init(Context context) {
		imageLoader=new ImageLoader(context, 2, ImageType.PHOTOSHARE);
	}
	
	public ViewHolder initView() {
		ViewHolder holder = new ViewHolder();
    	holder.photoImage = (ImageView) findViewById(R.id.photoImage);
		return holder;
	}
	
	public void loadInfo(ParsedPSHolder pInfo, ViewHolder holder) {
		
		try {
			mCafeId = Integer.parseInt(pInfo.getCafeid());
		} catch (NumberFormatException e) {
			mCafeId = -1;
			e.printStackTrace();
		}
		
		int width = Integer.parseInt(pInfo.getImgwidth());
		int height = Integer.parseInt(pInfo.getImgheight());
		int padding = MFUtil.getPixelsFromDip(22, getResources());
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.photoImage.getLayoutParams();
		int imageHeight = (MFConfig.deviceWidth - padding) * height / width;
		params.height = imageHeight;
		holder.photoImage.setLayoutParams(params);
		
//		MFService.loadImage(getContext().getApplicationContext(), ImageType.PHOTOSHARE, "image-" + pInfo.getPhotoid() + "-1.jpg", holder.photoImage, true, false);
		imageLoader.displayImage("image-" + pInfo.getPhotoid() + "-1.jpg", holder.photoImage, 0);
	}
	
    public static class ViewHolder {
    	ImageView photoImage;
    }
	
}
