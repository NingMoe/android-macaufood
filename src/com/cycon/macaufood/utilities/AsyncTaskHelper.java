package com.cycon.macaufood.utilities;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;

public class AsyncTaskHelper {

	@SuppressLint("NewApi")
	public static void execute(AsyncTask<Void, Void, Void> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	@SuppressLint("NewApi")
	public static void executeWithResultBitmap(AsyncTask<Void, Void, Bitmap> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	@SuppressLint("NewApi")
	public static void executeWithResultBoolean(AsyncTask<Void, Void, Boolean> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	@SuppressLint("NewApi")
	public static void executeWithResultString(AsyncTask<Void, Void, String> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	@SuppressLint("NewApi")
	public static  void execute(AsyncTask<Boolean, Void, ?> task, Boolean params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}
}
