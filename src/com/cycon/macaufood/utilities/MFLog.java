package com.cycon.macaufood.utilities;

import android.util.Log;

public class MFLog {

	public static final boolean DEBUG = false;
	
	public static void e(String tag, String log) {
		if (DEBUG) Log.e(tag, log);
	}
	
}
