package com.zhuoxiu.angelslibrary.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zhuoxiu.angelslibrary.db.DBTableBuilder;

public class DBHelper extends SQLiteOpenHelper implements DBConstant {
	private static String tag = DBHelper.class.getSimpleName();
	private static String dbName = "com.zhuoxiu.angleslibrary";
	private static final int dbVersion = 1;
	public static DBHelper helper;

	public static String TABLE_PHOTO="photo";
	public static String COL_DATA = "data";
	public static String COL_LAST_USE = "last_use";

	public DBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	public static SQLiteDatabase getDB(Context context, boolean writeable) {
		if (helper == null) {
			helper = new DBHelper(context, dbName, null, dbVersion);
		}
		return writeable ? helper.getWritableDatabase() : helper.getReadableDatabase();
	}

	public void onCreate(SQLiteDatabase db) {
		Log.i(tag, "onCreate");
		createDB(db);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(tag, "onUpgrade");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
		createDB(db);
	}

	private void createDB(SQLiteDatabase db) {
		DBTableBuilder.getInstance(db, TABLE_PHOTO).addColumn(COL_ID, TYPE_INTEGER, true).addColumn(COL_URL, TYPE_TEXT).addColumn(COL_DATA, TYPE_BLOB)
				.addColumn(COL_LAST_USE, TYPE_INTEGER).build();
	}

	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
	}

	@SuppressLint("NewApi")
	public static void print(String tag, Cursor cursor) {
		cursor.moveToFirst();
		int columnCount = cursor.getColumnCount();
		String output1 = new String();
		for (int i = 0; i < columnCount; i++) {
			output1 += cursor.getColumnName(i) + " ";
		}
		Log.i(tag, output1);
		while (!cursor.isAfterLast()) {
			String output2 = new String();
			for (int i = 0; i < columnCount; i++) {
				String columnName = cursor.getColumnName(i);
				int columnIndex = cursor.getColumnIndex(columnName);
				String columnValue = new String();
				if (cursor.getType(columnIndex) == Cursor.FIELD_TYPE_INTEGER) {
					columnValue = Integer.toString(cursor.getInt(columnIndex));
				}
				if (cursor.getType(columnIndex) == Cursor.FIELD_TYPE_STRING) {
					columnValue = cursor.getString(columnIndex);
				}
				output2 += columnValue + " ";
			}
			Log.i(tag, output2);
			cursor.moveToNext();
		}
	}

}
