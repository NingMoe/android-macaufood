package com.cycon.dbaccess;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cycon.macaufood.R;
import com.cycon.macaufood.bean.Cafe;
import com.cycon.macaufood.utilities.Config;
import com.cycon.macaufood.utilities.ETLog;

public class LocalDaoManager {

	private static final String tag = LocalDaoManager.class.getName();

	private static final String dbName = "MacauFoodDatabase.db";
	private static LocalDaoManager localDaoManager;

	private Context context;
	private SQLiteDatabase writableDb;
	private SQLiteOpenHelper helper;

	public static synchronized LocalDaoManager getInstance(Context context) {
		if (localDaoManager == null) {
			localDaoManager = new LocalDaoManager(context);
		}
		return localDaoManager;
	}

	private LocalDaoManager(Context context) {
		this.context = context;
		helper = new EtmpSQLiteOpenHelper(context, dbName);
		initTable();
	}
	
	public synchronized void initTable() {
		beginWritableDb();
		writableDb.execSQL(context.getString(R.string.initCafeTableSql));
		endWritableDb();
	}
	
	public synchronized void clearTable() {
		String clearCafeTableSql = context
		.getString(R.string.clearCafeTableSql);
		beginWritableDb();
		writableDb.execSQL(clearCafeTableSql);
		endWritableDb();
	}
	
	public synchronized void beginWritableDb() {
		writableDb = helper.getWritableDatabase();
		writableDb.beginTransaction();
	}
	
	public synchronized void endWritableDb() {
		writableDb.setTransactionSuccessful();
		writableDb.endTransaction();
	}
	
	public synchronized void updateCafeField(String field, String value, String id) {
		ContentValues values = new ContentValues();     
		values.put(field, value);
		writableDb.update("CAFEENTRIES", values, "id=?", new String[] {id});
	}
	
	public synchronized void insertCafe(Cafe cafe) {

		String insertCafeSql = context
				.getString(R.string.insertCafeSql);
			writableDb.execSQL(insertCafeSql, new String[] {cafe.getId(), cafe.getName(), cafe.getPhone(), 
						cafe.getDistrict(), cafe.getAddress(), cafe.getWebsite(), 
						cafe.getCoordx(), cafe.getCoordy(), cafe.getOpenhours(), 
						cafe.getDescription(), cafe.getMessage(), cafe.getType0(), 
						cafe.getType1(), cafe.getType2(), cafe.getOption_phoneorder(), 
						cafe.getOption_booking(), cafe.getOption_night(), cafe.getOption_call(), 
						cafe.getOption_buffet(), cafe.getOption_banquet(), cafe.getMenuid(), 
						cafe.getIntroid(), cafe.getIntropage(), cafe.getRecommendid(), 
						cafe.getRecommendpage(), cafe.getMenupage(), 
						cafe.getPayment(), cafe.getStatus(), cafe.getOption_wifi(), 
						cafe.getOption_parking(), cafe.getBranch(), cafe.getPriority()});
	}
	
	public synchronized void insertCafeLists() {

		String insertCafeSql = context
				.getString(R.string.insertCafeSql);
		beginWritableDb();
		List<Cafe> list = Config.getInstance().getCafeLists();
		for (Cafe cafe : list) {
			writableDb.execSQL(insertCafeSql, new String[] {cafe.getId(), cafe.getName(), cafe.getPhone(), 
					cafe.getDistrict(), cafe.getAddress(), cafe.getWebsite(), 
					cafe.getCoordx(), cafe.getCoordy(), cafe.getOpenhours(), 
					cafe.getDescription(), cafe.getMessage(), cafe.getType0(), 
					cafe.getType1(), cafe.getType2(), cafe.getOption_phoneorder(), 
					cafe.getOption_booking(), cafe.getOption_night(), cafe.getOption_call(), 
					cafe.getOption_buffet(), cafe.getOption_banquet(), cafe.getMenuid(), 
					cafe.getIntroid(), cafe.getIntropage(), cafe.getRecommendid(), 
					cafe.getRecommendpage(), cafe.getMenupage(), 
					cafe.getPayment(), cafe.getStatus(), cafe.getOption_wifi(), 
					cafe.getOption_parking(), cafe.getBranch(), cafe.getPriority()});
		}
		endWritableDb();
	}

	public synchronized void getCafeListFromDB() {
		String getCafeEntriesSql = context
				.getString(R.string.getCafeEntriesSql);

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor c = db.rawQuery(getCafeEntriesSql, new String[0]);
		if (c != null && c.moveToFirst()) {
			while (!c.isAfterLast()) {
				Cafe cafe = new Cafe();
				cafe.setId(c.getString(0));
				cafe.setName(c.getString(1));
				cafe.setPhone(c.getString(2));
				cafe.setDistrict(c.getString(3));
				cafe.setAddress(c.getString(4));
				cafe.setWebsite(c.getString(5));
				cafe.setCoordx(c.getString(6));
				cafe.setCoordy(c.getString(7));
				cafe.setOpenhours(c.getString(8));
				cafe.setDescription(c.getString(9));
				cafe.setMessage(c.getString(10));
				cafe.setType0(c.getString(11));
				cafe.setType1(c.getString(12));
				cafe.setType2(c.getString(13));
				cafe.setOption_phoneorder(c.getString(14));
				cafe.setOption_booking(c.getString(15));
				cafe.setOption_night(c.getString(16));
				cafe.setOption_call(c.getString(17));
				cafe.setOption_buffet(c.getString(18));
				cafe.setOption_banquet(c.getString(19));
				cafe.setMenuid(c.getString(20));
				cafe.setIntroid(c.getString(21));
				cafe.setIntropage(c.getString(22));
				cafe.setRecommendid(c.getString(23));
				cafe.setRecommendpage(c.getString(24));
				cafe.setMenupage(c.getString(25));
				cafe.setPayment(c.getString(26));
				cafe.setStatus(c.getString(27));
				cafe.setOption_wifi(c.getString(28));
				cafe.setOption_parking(c.getString(29));
				cafe.setBranch(c.getString(30));
				cafe.setPriority(c.getString(31));
//				ETLog.e("localdao", cafe.getId());
				Config.getInstance().getCafeLists().add(cafe);
				c.moveToNext();
			}
		}
		if (c != null) {
			c.close();
		}
	}

	@Override
	protected synchronized void finalize() throws Throwable {
		helper.close();
		helper = null;
		super.finalize();

	}

}
