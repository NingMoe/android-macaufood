package com.cycon.macaufood.utilities;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cycon.macaufood.R;

public class FeedBackDialogHelper {
	private static TextView name;
	private static TextView email;
	private static TextView content;
	private static String cafeId;
	private static final String TAG = FeedBackDialogHelper.class.getName();

	public static void showFeedBackDialog(final Context context,
			LayoutInflater inflater, String id) {
		cafeId = id;

		View view = inflater.inflate(R.layout.feedback, null);
		name = (TextView)view.findViewById(R.id.nameText);
		email = (TextView)view.findViewById(R.id.emailText);
		content = (TextView)view.findViewById(R.id.contentText);
		
		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle(R.string.giveFeedBack)
				.setCancelable(false)
				.setView(view)
				.setPositiveButton(context.getString(R.string.fbSubmit), null)
				.setNegativeButton(context.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).show();
		
		
		Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	validateAndSubmit(dialog, context);
            }
        });
	}
	
	private static void validateAndSubmit(DialogInterface dialog, Context context) {
		String emailStr = email.getText().toString();
		if (!validateEmail(emailStr)) {
			Toast.makeText(context, context.getString(R.string.emailWrongFormat), Toast.LENGTH_SHORT).show();
			return;
		} else if (content.getText().toString().length() < 5) {
			Toast.makeText(context, context.getString(R.string.contentWordsTooShort), Toast.LENGTH_SHORT).show();
			return;
		} else {
			AsyncTaskHelper.executeWithResultBoolean(new SubmitFeedBackTask(dialog, context));
		}
	}

	private static boolean validateEmail(String emailText) {
		String emailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";
		return emailText.matches(emailRegex);
	}

	private static class SubmitFeedBackTask extends
			AsyncTask<Void, Void, Boolean> {

		private ProgressDialog pDialog;
		private DialogInterface aDialog;
		private Context context;
		
		private SubmitFeedBackTask(DialogInterface dialog, Context context) {
			aDialog = dialog;
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(context, null,
					context.getString(R.string.sending), false, true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			String urlStr = MFURL.SUBMIT_FEEDBACK
					+ MFConfig.DEVICE_ID
					+ "&cafeid="
					+ cafeId
					+ "&name="
					+ name.getText().toString()
					+ "&content="
					+ content.getText().toString()
					+ "&email="
					+ email.getText().toString();

			try {
				HttpClient client = new DefaultHttpClient();
				HttpParams httpParams = client.getParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
				HttpGet request = new HttpGet(urlStr);
				client.execute(request);

			} catch (MalformedURLException e) {
				Log.e(TAG, "malformed url exception");
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				Log.e(TAG, "io exception");
				e.printStackTrace();
				return false;
			} catch (Exception e) {
				Log.e(TAG, "exception");
				e.printStackTrace();
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (pDialog != null) {
				pDialog.dismiss();
			}

			if (success) {
				Toast.makeText(context,
						context.getString(R.string.sendSucceed),
						Toast.LENGTH_SHORT).show();
				aDialog.dismiss();
			} else {
				Toast.makeText(context,
						context.getString(R.string.sendFailed),
						Toast.LENGTH_SHORT).show();
			}

		}
	}
}
