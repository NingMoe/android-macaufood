package com.cycon.macaufood.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;

public class MFUtil {

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public static String getDishesStringFromCafe(Cafe cafe) {
		StringBuilder sb = new StringBuilder();
		int dishes1 = Integer.parseInt(cafe.getType0() == null ? "0" : cafe
				.getType0());
		int dishes2 = Integer.parseInt(cafe.getType1());
		int dishes3 = Integer.parseInt(cafe.getType2());
		int index1 = Arrays.asList(MFConstants.dishesId).indexOf(dishes1);
		int index2 = Arrays.asList(MFConstants.dishesId).indexOf(dishes2);
		int index3 = Arrays.asList(MFConstants.dishesId).indexOf(dishes3);
		if (index1 != 0) {
			sb.append(MFConstants.dishesType[index1]);
		}
		if (index2 != 0) {
			sb.append(',');
			sb.append(MFConstants.dishesType[index2]);
		}
		if (index3 != 0) {
			sb.append(',');
			sb.append(MFConstants.dishesType[index3]);
		}

		return sb.toString();

	}

	public static String getPastTime(long time, Context context) {
		long diffTime = System.currentTimeMillis() / 1000 - time;
		if (diffTime < 60) { // less than 1 min
			if (diffTime <= 0) {
				diffTime = 1;
			}
			return diffTime + context.getResources().getString(R.string.sec);

		} else if (diffTime < 60 * 60) { // less than 1 hour
			int minutes = (int) Math.floor(diffTime / 60.0);
			return minutes + context.getResources().getString(R.string.min);

		} else if (diffTime < 60 * 60 * 24) { // less than 1 day
			int hour = (int) Math.floor(diffTime / (60.0 * 60));
			return hour + context.getResources().getString(R.string.hour);

		} else if (diffTime < 60 * 60 * 24 * 30 * 2) { // less than 2 month
			int day = (int) Math.floor(diffTime / (60.0 * 60 * 24));
			return day + context.getResources().getString(R.string.day);
		} else { // more than 1 month
			int month = (int) Math.floor(diffTime / (60.0 * 60 * 24 * 30));
			return month + context.getResources().getString(R.string.month);
		}
	}

	public static int getPixelsFromDip(float dips, Resources res) {
		return Math.round(dips * res.getDisplayMetrics().density);
	}

	public static float getDipFromPixels(int pixel, Resources res) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel,
				res.getDisplayMetrics());
	}

	public static Bitmap getBitmapFromCache(FileCache fileCache, String imageId) {
		if (fileCache == null)
			return null;
		File f = fileCache.getFile(imageId);
		try {
			return BitmapFactory.decodeStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String getStringFromCache(FileCache fileCache, String stringId) {
		String str = null;
		File f = fileCache.getFile(stringId);
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			return null;
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(fis));
		try {
			str = rd.readLine();
			if (str != null)
				str = str.trim();
			rd.close();
			return str;
		} catch (IOException e) {
			return null;
		}
	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a File for saving an image or video */
	public static File getOutputMediaFile(int type) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED))
			return null;

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MacauFood");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MacauFood", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	
  public static String getPath(Context context, Uri contentUri) {
	  Cursor cursor = null;
	  try { 
	    String[] proj = { MediaStore.Images.Media.DATA };
	    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	  } finally {
	    if (cursor != null) {
	      cursor.close();
	    }
	  }
	}

	public static int getOrientation(Context context, Uri photoUri, File file) {
		/* it's on the external media. */
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
				null, null, null);
		if (cursor != null && cursor.getCount() != 1) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		}

		int rotate = 0;
		ExifInterface exif = null;
		
		try {
			exif = new ExifInterface(file == null ? getPath(context, photoUri) : file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
				ExifInterface.ORIENTATION_NORMAL);
		

		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotate = 270;
			break;
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotate = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotate = 90;
			break;
		case ExifInterface.ORIENTATION_NORMAL:
			rotate = 0;
			break;
		}
		return rotate;
	}

	public static Bitmap getThumbnail(Uri uri, Context context)
			throws FileNotFoundException, IOException {
		InputStream input = context.getContentResolver().openInputStream(uri);

		BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
		onlyBoundsOptions.inJustDecodeBounds = true;
		onlyBoundsOptions.inDither = true;// optional
		onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
		input.close();
		if ((onlyBoundsOptions.outWidth == -1)
				|| (onlyBoundsOptions.outHeight == -1))
			return null;

		int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
				: onlyBoundsOptions.outWidth;
		int THUMBNAIL_SIZE = MFUtil.getPixelsFromDip(60f,
				context.getResources());
		double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE)
				: 1.0;

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
		bitmapOptions.inDither = true;// optional
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
		input = context.getContentResolver().openInputStream(uri);
		Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
		input.close();
		return bitmap;
	}
	
	public static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
		Matrix matrix = new Matrix();
		matrix.preRotate(rotation);
		Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return rotatedBitmap;
	}

	private static int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0)
			return 1;
		else
			return k;
	}

	public static FlushedInputStream flushedInputStream(InputStream is) {
		return new FlushedInputStream(is);
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int bytes = read();
					if (bytes < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

}
