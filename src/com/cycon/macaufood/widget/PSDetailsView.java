package com.cycon.macaufood.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Process;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.R;
import com.cycon.macaufood.activities.Details;
import com.cycon.macaufood.bean.ParsedPSHolder;
import com.cycon.macaufood.utilities.ImageLoader;
import com.cycon.macaufood.utilities.LoginHelper;
import com.cycon.macaufood.utilities.LoginHelper.PendingAction;
import com.cycon.macaufood.utilities.LoginHelper.RegisterPSCallBack;
import com.cycon.macaufood.utilities.MFConfig;
import com.cycon.macaufood.utilities.MFService;
import com.cycon.macaufood.utilities.MFURL;
import com.cycon.macaufood.utilities.MFUtil;
import com.facebook.Session;

public class PSDetailsView extends LinearLayout {
	
	public interface DetailsViewCallback {
		void onDeletePhoto();
	}

	private final static int MAX_LIKE = 6;
	private Context mContext;
	private LoginHelper mLoginHelper;
	private DetailsViewCallback mCallback;
	
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
	
	public void setLoginHelper(LoginHelper helper) {
		mLoginHelper = helper;
	}
	
	public void setDetailsViewCallback(DetailsViewCallback callback) {
		mCallback = callback;
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
    	holder.pBar = (ProgressBar) findViewById(R.id.pBar);
		return holder;
	}
	
	public void loadInfo(final ParsedPSHolder pInfo, final ViewHolder holder, ImageLoader imageLoader, int pos) {
		final String cafeId = pInfo.getCafeid();

		int width = Integer.parseInt(pInfo.getImgwidth());
		int height = Integer.parseInt(pInfo.getImgheight());
		int padding = MFUtil.getPixelsFromDip(16, getResources());
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.photoImage.getLayoutParams();
		int imageHeight = (MFConfig.deviceWidth - padding) * height / width;
		params.height = imageHeight;
		holder.photoImage.setLayoutParams(params);
		RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) holder.pBar.getLayoutParams();
		params1.bottomMargin = 100;
		holder.pBar.setLayoutParams(params1);
		
		imageLoader.displayImage("image-" + pInfo.getPhotoid() + "-1.jpg", holder.photoImage, pos, holder.pBar);
		
		String captionText = pInfo.getCaption();
		if (captionText.length() > 0 && !captionText.equals("(null)")) {
			holder.caption.setText(pInfo.getCaption());
			holder.caption.setVisibility(View.VISIBLE);
		} else {
			holder.caption.setVisibility(View.GONE);
		}
		
		loadLikeInfo(pInfo, holder, false);
		
		loadCommentInfo(pInfo, holder);
		
		
		
		//buttons------------------------------------------------
		if (Integer.parseInt(cafeId) > 0 && MFConfig.getInstance().getCafeLists().size() >= Integer.parseInt(cafeId)) {
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
				checkLogin(PendingAction.LIKE, pInfo, holder, null);
			}
		});
		

		

		if (pInfo.getMemberid().equals(MFConfig.memberId)) {
			holder.deleteButton.setVisibility(View.VISIBLE);
			holder.deleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					checkLogin(PendingAction.DELETE, pInfo, holder, null);
				}
			});
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
    	ProgressBar pBar;
    }
    
    private void loadLikeInfo(final ParsedPSHolder pInfo, ViewHolder holder, boolean cancelMyLike) {
		setLikeButtonStatus(false, holder.likeButton);
		holder.likeLayout.setVisibility(View.VISIBLE);
//		List<PSLike> likeList = extractLikeList(pInfo.getLikes());
		
		StringBuilder sb = new StringBuilder();
    	final List<PSLike> likeList = new ArrayList<PSLike>();
    	String[] tokens = pInfo.getLikes().split("@@@");
    	for (int i = tokens.length - 1; i >=0; i--) {
    		String tokenStr = tokens[i];
			String[] strArr = tokenStr.split("\\|\\|\\|");
			if (strArr.length > 1) {
				if (strArr[0].equals(MFConfig.memberId)) {
					if (cancelMyLike) {
						continue;
					} else {
						setLikeButtonStatus(true, holder.likeButton);
					}
				}
				PSLike like = new PSLike();
				like.id = strArr[0];
				like.name = strArr[1];
				likeList.add(like);
				sb.append(like.name);
				if (i != 0) {
					sb.append(',');
				}
				sb.append(' ');
			}
		}

    	if (cancelMyLike) {
    		StringBuilder tempSb = new StringBuilder(); //for rewriting like str when cancel like
			for (int i = likeList.size() - 1; i >=0; i--) {
				tempSb.append(likeList.get(i).id);
				tempSb.append("|||");
				tempSb.append(likeList.get(i).name);
				if (i != 0) {
					tempSb.append("@@@");
				}
			}
			pInfo.setLikes(tempSb.toString());
		}
		
//		for (int i = likeList.size() - 1; i >= 0; i--) {
//			//check if user liked already
//			if (likeList.get(i).id.equals(MFConfig.memberId)) {
//				setLikeButtonStatus(true, holder.likeButton);
//				break;
//			} 
//		}
		
		if (likeList.size() > MAX_LIKE) {
			holder.like.setText(mContext.getResources().getString(R.string.peopleLikedThis, likeList.size()));
			holder.like.setTypeface(null, Typeface.BOLD);
			holder.like.setBackgroundResource(R.drawable.text_bg_selector_thinner);
			holder.like.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
			    	final LikeListDialogView view = new LikeListDialogView(mContext, likeList);
			    	
					AlertDialog dialog = new AlertDialog.Builder(mContext)
					.setView(view)
					.setPositiveButton(mContext.getResources().getString(R.string.confirmed),
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
					
				}
			});
		} else if (likeList.size() > 0) {
//			StringBuilder sb = new StringBuilder();
//			int i;
//			for (i = likeList.size() - 1; i >= 0; i--) {
//				sb.append(likeList.get(i).name);
//				if (i != 0) {
//					sb.append(',');
//				}
//				sb.append(' ');
//			}
			
			sb.append(mContext.getResources().getString(likeList.size() > 1 ? R.string.alsoLikedThis : R.string.likedThis));
			SpannableString spannable = new SpannableString(sb.toString());
			spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.button_gray_text)), sb.length() - (likeList.size() > 1 ? 3 : 4), sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			holder.like.setText(spannable);
			holder.like.setTypeface(null, Typeface.NORMAL);
			holder.like.setBackgroundDrawable(null);
			holder.like.setOnClickListener(null);
		} else {
			holder.likeLayout.setVisibility(View.GONE);
		}
    }
    
    private void loadCommentInfo(final ParsedPSHolder pInfo, final ViewHolder holder) {
		holder.comment.removeAllViews();
		final List<PSComment> commentList = extractCommentList(pInfo.getComments());
		if (commentList.size() > 0) {
			holder.commentLayout.setVisibility(View.VISIBLE);
			for (int i = 0; i < commentList.size(); i++) {
				PSComment psComment = commentList.get(i);
				String displayStr = psComment.name + "  " + psComment.comment;
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
		
		holder.commentButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkLogin(PendingAction.COMMENT, pInfo, holder, commentList);
			}
		});
    }
    
    private void doDeleteButtonClickAction(final ParsedPSHolder pInfo) {
		if (!MFConfig.isOnline(mContext)) {
			Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
		} else {
			new AlertDialog.Builder(mContext)
			.setMessage(mContext.getString(R.string.deleteConfirmPrompt))
			.setPositiveButton(mContext.getString(R.string.confirmed),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							MFService.sendRequest(String.format(Locale.US, MFURL.PHOTOSHARE_DELETE, MFConfig.memberId, pInfo.getPhotoid()), mContext.getApplicationContext());
							String photoId = pInfo.getPhotoid();
							MFConfig.getInstance().getPsInfoMap().remove(photoId);
							MFConfig.getInstance().getPsHotList().remove(photoId);
							MFConfig.getInstance().getFriendsActivityList().remove(photoId);
							mCallback.onDeletePhoto();
						}
					})
			.setNegativeButton(mContext.getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.dismiss();
						}
					}).show();
		}
    }
    
    private void doLikeButtonClickAction(final ParsedPSHolder pInfo, final ViewHolder holder) {
		if (!MFConfig.isOnline(mContext)) {
			Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
		} else {
			if (holder.likeButton.getText().equals(getResources().getString(R.string.like))) {
				pInfo.setLikes(pInfo.getLikes() + (pInfo.getLikes().equals("") ? "" : "@@@") + MFConfig.memberId + "|||" + MFConfig.memberName);
				loadLikeInfo(pInfo, holder, false);
				MFService.sendRequest(String.format(Locale.US, MFURL.PHOTOSHARE_LIKE, MFConfig.memberId, pInfo.getPhotoid()), mContext.getApplicationContext());
			} else {
				loadLikeInfo(pInfo, holder, true);
				MFService.sendRequest(String.format(Locale.US, MFURL.PHOTOSHARE_UNLIKE, MFConfig.memberId, pInfo.getPhotoid()), mContext.getApplicationContext());
			}
		}
    }
    
    private void doCommentButtonClickAction(final ParsedPSHolder pInfo, final ViewHolder holder, List commentList) {
		if (!MFConfig.isOnline(mContext)) {
			Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
			return;
		}
    	final CommentDialogView view = new CommentDialogView(mContext, commentList);
		if (Build.VERSION.SDK_INT < 11) {
			view.setBackgroundColor(Color.WHITE);
		}
    	
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
		.setView(view)
		.show();
		
		final EditText editTv = (EditText) view.findViewById(R.id.editTv);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		editTv.requestFocus();
		
		Button sendButton = (Button) view.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String commentStr = editTv.getText().toString().trim();
				if (commentStr.length() > 0) {
					if (!MFConfig.isOnline(mContext)) {
						Toast.makeText(mContext, R.string.errorMsg, Toast.LENGTH_SHORT).show();
					} else {
						List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		    			pairs.add(new BasicNameValuePair("memberid", MFConfig.memberId));
		    			pairs.add(new BasicNameValuePair("photoid", pInfo.getPhotoid()));
		    			pairs.add(new BasicNameValuePair("comment", commentStr));
		    			MFService.sendRequestWithParams(MFURL.PHOTOSHARE_COMMENT, mContext.getApplicationContext(), pairs);
		    			pInfo.setComments(commentStr + "|||" + MFConfig.memberId + "|||" + MFConfig.memberName + "|||" + (System.currentTimeMillis() / 1000) + (pInfo.getComments().equals("") ? "" : "@@@") + pInfo.getComments() );
		    			dialog.dismiss();
		    			loadCommentInfo(pInfo, holder);
					}
				}

			}
		});
    }
    
	private void checkLogin(PendingAction pa, final ParsedPSHolder pInfo, final ViewHolder holder, final List list) {
		
		if (mLoginHelper.isLogin()) {
			handlePendingAction(pa, pInfo, holder, list);
		} else {
    		mLoginHelper.showLoginDialog(null, pa, new RegisterPSCallBack() {
				
				@Override
				public void onErrorRegistered() {
				}
				
				@Override
				public void onCompleteRegistered(PendingAction pa) {
					handlePendingAction(pa, pInfo, holder, list);
				}
			});
		}
	}
    
    private void handlePendingAction(PendingAction pa, final ParsedPSHolder pInfo, final ViewHolder holder, List list) {
    	switch (pa) {
		case LIKE:
			doLikeButtonClickAction(pInfo, holder);
			break;
		case COMMENT:
			doCommentButtonClickAction(pInfo, holder, list);
			break;
		case DELETE:
			doDeleteButtonClickAction(pInfo);
			break;
		default:
			break;
		}
    }
    
    private void setLikeButtonStatus(boolean like, Button likeButton) {
    	if (like) {
			likeButton.setText(R.string.liked);
			likeButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_heart_red), null, null, null);
		} else {
			likeButton.setText(R.string.like);
			likeButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_heart_grey), null, null, null);
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
    	for (int i = tokens.length - 1; i >= 0; i--) {
    		String tokenStr = tokens[i];
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
    
    public static class PSLike {
    	public String id;
    	public String name;
    }
    
    public static class PSComment {
    	public String comment;
    	public String id;
    	public String name;
    	public String timeStamp;
    }
	
}
