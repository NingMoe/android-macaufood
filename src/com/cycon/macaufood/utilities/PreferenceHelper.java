package com.cycon.macaufood.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceHelper {
	
	private static final String PREF_NAME = "macaufood.preferences";
	
	public static void savePreferencesLong(Context context, String key, long value) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		Editor prefsPrivateEditor = prefs.edit();
		prefsPrivateEditor.putLong(key, value);
		prefsPrivateEditor.commit();
	}
	
	public static long getPreferenceValueLong(Context context, String key, long defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		return prefs.getLong(key, defaultValue);
	}

	public static void savePreferencesStr(Context context, String key, String value) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		Editor prefsPrivateEditor = prefs.edit();
		prefsPrivateEditor.putString(key, value);
		prefsPrivateEditor.commit();
	}
	
	public static String getPreferenceValueStr(Context context, String key, String defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		return prefs.getString(key, defaultValue);
	}

	public static void savePreferencesBoolean(Context context, String key, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		Editor prefsPrivateEditor = prefs.edit();
		prefsPrivateEditor.putBoolean(key, value);
		prefsPrivateEditor.commit();
	}
	
	public static boolean getPreferenceValueBoolean(Context context, String key, boolean defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(
				PREF_NAME, 0);
		return prefs.getBoolean(key, defaultValue);
	}
}
