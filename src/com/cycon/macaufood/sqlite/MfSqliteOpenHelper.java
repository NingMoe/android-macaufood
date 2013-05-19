package com.cycon.macaufood.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MfSqliteOpenHelper extends SQLiteOpenHelper {
	private static final String tag = MfSqliteOpenHelper.class.getName();

	public MfSqliteOpenHelper(Context context, String dbName) {
		this(context, dbName, null, 1);
	}

	public MfSqliteOpenHelper(Context context, String name,
			SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public void onCreate(SQLiteDatabase db) {

	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
