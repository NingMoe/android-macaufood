package com.cycon.macaufood.utilities;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class AsyncTaskHelper {

	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	public static void execute(AsyncTask<?, ?, ?> task) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}
	
	@SuppressWarnings("unchecked")
	@SuppressLint("NewApi")
	public static <Params> void execute(AsyncTask<Params, ?, ?> task, Params params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}
}
