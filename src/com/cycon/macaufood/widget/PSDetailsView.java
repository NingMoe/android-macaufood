package com.cycon.macaufood.widget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.cycon.macaufood.utilities.MFConstants;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.cycon.macaufood.utilities.PhoneUtils;
import com.cycon.macaufood.utilities.PreferenceHelper;

public class PSDetailsView extends LinearLayout {

	private final static int MAX_LIKE = 7;
	private Context mContext;
	
	public PSDetailsView(Context context) {
		super(context);
		init(context);
	}
	
	public PSDetailsView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
	}
	
	public ViewHolder initView() {
		ViewHolder holder = new ViewHolder();
    	holder.photoImage = (ImageView) findViewById(R.id.photoImage);
    	holder.caption = (TextView) findViewById(R.id.caption);
    	holder.like = (TextView) findViewById(R.id.like);
    	holder.comment = (LinearLayout) findViewById(R.id.comment);
    	holder.likeButton = (Button) findViewById(R.id.likeButton);
    	holder.commentButton = (Button) findViewById(R.id.commentButton);
    	holder.infoButton = (Button) findViewById(R.id.infoButton);
    	holder.deleteButton = (Button) findViewById(R.id.deleteButton);
		return holder;
	}
	
	public void loadInfo(ParsedPSHolder pInfo, ViewHolder holder, ImageLoader imageLoader, int pos) {
		final String cafeId = pInfo.getCafeid();
		
		int width = Integer.parseInt(pInfo.getImgwidth());
		int height = Integer.parseInt(pInfo.getImgheight());
		int padding = MFUtil.getPixelsFromDip(16, getResources());
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.photoImage.getLayoutParams();
		int imageHeight = (MFConfig.deviceWidth - padding) * height / width;
		params.height = imageHeight;
		holder.photoImage.setLayoutParams(params);
		
		imageLoader.displayImage("image-" + pInfo.getPhotoid() + "-1.jpg", holder.photoImage, pos);
		
		String captionText = pInfo.getCaption();
		if (captionText.length() > 0) {
			holder.caption.setText(pInfo.getCaption());
			holder.caption.setVisibility(View.VISIBLE);
		} else {
			holder.caption.setVisibility(View.GONE);
		}
		
		
		//display like ------------------------------------
		int numOfLike;
		try {
			numOfLike = Integer.parseInt(pInfo.getNumoflike());
		} catch (NumberFormatException e1) {
			numOfLike = 0;
		}
		if (numOfLike > MAX_LIKE) {
			holder.like.setText(mContext.getResources().getString(R.string.peopleLikedThis, numOfLike));
			holder.like.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
		} else {
			List<PSLike> likeList = extractLikeList(pInfo.getLikes());
			if (likeList.size() > 0) {
				holder.like.setVisibility(View.VISIBLE);
				StringBuilder sb = new StringBuilder();
				int i;
				for (i = 0; i < likeList.size(); i++) {
					sb.append(likeList.get(i).name);
					if (i != likeList.size() - 1) {
						sb.append(',');
					}
					sb.append(' ');
				}
				
				if (i > 1) {
					sb.append(mContext.getResources().getString(R.string.alsoLikedThis));
				} else {
					sb.append(mContext.getResources().getString(R.string.likedThis));
				}
				holder.like.setText(sb.toString());
			} else {
				holder.like.setVisibility(View.GONE);
			}
		}
		
		
		//display comment---------------------------------------
		holder.comment.removeAllViews();
		List<PSComment> commentList = extractCommentList(pInfo.getComments());
		if (commentList.size() > 0) {
			holder.comment.setVisibility(View.VISIBLE);
			for (PSComment psComment : commentList) {
				String displayStr = psComment.name + " " + psComment.comment;
				SpannableString spannable = new SpannableString(displayStr);
	            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_text)), 0, psComment.name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				TextView tv = new TextView(mContext);
				tv.setText(spannable);
				holder.comment.addView(tv);
			}
		} else {
			holder.comment.setVisibility(View.GONE);
		}
		
		
		
		
		
		
		//buttons------------------------------------------------
		
		
		
		if (!cafeId.equals("0") && MFConfig.getInstance().getCafeLists().size() >= Integer.parseInt(cafeId)) {
			try {
				holder.infoButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Intent i = new Intent(mContext, Details.class);
						i.putExtra("id", cafeId);
						mContext.startActivity(i);
					}
				});
				holder.infoButton.setVisibility(View.VISIBLE);
				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} else {
			holder.infoButton.setVisibility(View.INVISIBLE);
		}
		
		
		if (MFConfig.memberId == null) {
			MFConfig.memberId = PreferenceHelper.getPreferenceValueStr(mContext, MFConstants.PS_MEMBERID_PREF_KEY, null);
		}
		if (pInfo.getMemberid().equals(MFConfig.memberId)) {
			holder.deleteButton.setVisibility(View.VISIBLE);
		} else {
			holder.deleteButton.setVisibility(View.INVISIBLE);
		}
	}
	
    public static class ViewHolder {
    	ImageView photoImage;
    	TextView caption;
    	TextView like;
    	LinearLayout comment;
    	Button likeButton;
    	Button commentButton;
    	Button infoButton;
    	Button deleteButton;
    }
    
    private List<PSLike> extractLikeList(String str) {
    	List<PSLike> likeList = new ArrayList<PSLike>();
    	String[] tokens = str.split("@@@");
    	for (String tokenStr : tokens) {
			String[] strArr = tokenStr.split("\\|\\|\\|");
			if (strArr.length > 1) {
				PSLike like = new PSLike();
				like.id = strArr[0];
				like.name = strArr[1];
				likeList.add(like);
			}
		}
    	return likeList;
    }
    
    private List<PSComment> extractCommentList(String str) {
    	List<PSComment> commentList = new ArrayList<PSComment>();
    	String[] tokens = str.split("@@@");
    	for (String tokenStr : tokens) {
			String[] strArr = tokenStr.split("\\|\\|\\|");
			if (strArr.length > 3) {
				PSComment comment = new PSComment();
				comment.comment = strArr[0];
				comment.id = strArr[1];
				comment.name = strArr[2];
				comment.timeStamp = strArr[3];
				commentList.add(comment);
			}
		}
    	return commentList;
    }
    
    private static class PSLike {
    	String id;
    	String name;
    }
    
    private static class PSComment {
    	String comment;
    	String id;
    	String name;
    	String timeStamp;
    }
	
}
