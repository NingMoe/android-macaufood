package com.cycon.macaufood.utilities;

import android.util.Log;

public class MFLog {

	public static final boolean DEBUG = true;
	
	public static void e(String tag, String log) {
		if (DEBUG) Log.e(tag, log);
	}
	
}
