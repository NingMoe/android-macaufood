package com.cycon.macaufood.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cycon.macaufood.R;
import com.cycon.macaufood.activities.Details;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFUtil;

public class PSDetailsView extends LinearLayout {

	private final static int MAX_LIKE = 6;
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
    	holder.commentLayout = (LinearLayout) findViewById(R.id.commentLayout);
    	holder.likeLayout = (LinearLayout) findViewById(R.id.likeLayout);
    	holder.likeButton = (Button) findViewById(R.id.likeButton);
    	holder.commentButton = (Button) findViewById(R.id.commentButton);
    	holder.infoButton = (Button) findViewById(R.id.infoButton);
    	holder.deleteButton = (Button) findViewById(R.id.deleteButton);
		return holder;
	}
	
	public void loadInfo(final ParsedPSHolder pInfo, final ViewHolder holder, ImageLoader imageLoader, int pos) {
		final String cafeId = pInfo.getCafeid();

		Log.e("ZZZ", "loadInfo");
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
		
		loadLikeInfo(pInfo, holder);
		
		loadCommentInfo(pInfo, holder);
		
		
		
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
		
		
		holder.likeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO:check login status
				//TODO: check internet and show toast
				if (holder.likeButton.getText().equals(getResources().getString(R.id.like))) {
					Log.e("ZZZ", "onclick");
					pInfo.setLikes(pInfo.getLikes() + "@@@" + MFConfig.memberId + "|||" + MFConfig.memberName);
				} else {
					
				}
				loadLikeInfo(pInfo, holder);
			}
		});
		

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
    	LinearLayout likeLayout;
    	LinearLayout comment;
    	LinearLayout commentLayout;
    	Button likeButton;
    	Button commentButton;
    	Button infoButton;
    	Button deleteButton;
    }
    
    private void loadLikeInfo(final ParsedPSHolder pInfo, ViewHolder holder) {
    	Log.e("ZZZ", "loadlike info");
		setLikeButtonStatus(false, holder.likeButton);
		holder.likeLayout.setVisibility(View.VISIBLE);
		List<PSLike> likeList = extractLikeList(pInfo.getLikes());
		
		for (int i = likeList.size() - 1; i >= 0; i--) {
			//check if user liked already
			if (likeList.get(i).id.equals(MFConfig.memberId)) {
				setLikeButtonStatus(true, holder.likeButton);
				break;
			} 
		}
		
		if (likeList.size() > MAX_LIKE) {
			holder.like.setText(mContext.getResources().getString(R.string.peopleLikedThis, likeList.size()));
			holder.like.setTypeface(null, Typeface.BOLD);
			holder.like.setBackgroundResource(R.drawable.text_bg_selector_thinner);
			holder.like.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					
				}
			});
		} else if (likeList.size() > 0) {
			StringBuilder sb = new StringBuilder();
			int i;
			for (i = likeList.size() - 1; i >= 0; i--) {
				sb.append(likeList.get(i).name);
				if (i != 0) {
					sb.append(',');
				}
				sb.append(' ');
			}
			
			sb.append(mContext.getResources().getString(i > 1 ? R.string.alsoLikedThis : R.string.likedThis));
			SpannableString spannable = new SpannableString(sb.toString());
			spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.button_gray_text)), sb.length() - (i > 1 ? 3 : 4), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.like.setText(spannable);
			holder.like.setTypeface(null, Typeface.NORMAL);
			holder.like.setBackgroundDrawable(null);
			holder.like.setOnClickListener(null);
		} else {
			holder.likeLayout.setVisibility(View.GONE);
		}
    }
    
    private void loadCommentInfo(final ParsedPSHolder pInfo, ViewHolder holder) {
		holder.comment.removeAllViews();
		List<PSComment> commentList = extractCommentList(pInfo.getComments());
		if (commentList.size() > 0) {
			holder.commentLayout.setVisibility(View.VISIBLE);
			for (int i = 0; i < commentList.size(); i++) {
				PSComment psComment = commentList.get(i);
				String displayStr = psComment.name + " " + psComment.comment;
				SpannableString spannable = new SpannableString(displayStr);
	            spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green_text)), 0, psComment.name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            spannable.setSpan(new RelativeSizeSpan(13f/14), 0, psComment.name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				TextView tv = new TextView(mContext); 
				tv.setText(spannable);
				holder.comment.addView(tv);
			}
		} else {
			holder.commentLayout.setVisibility(View.GONE);
		}
    }
    
    private void setLikeButtonStatus(boolean like, Button likeButton) {
    	if (like) {
			likeButton.setText(R.string.liked);
		} else {
			likeButton.setText(R.string.like);
		}
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
