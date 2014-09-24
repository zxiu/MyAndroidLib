package com.zhuoxiu.angelslibrary.db;

import android.provider.SyncStateContract.Columns;

public interface DBConstant {

	// column
	public static final String COL_ID = Columns._ID;
	public static final String COL_KEY = "key";
	public static final String COL_JSON = "json";
	public static final String COL_URL = "url";
	public static final String COL_TIMESTAMP = "timestamp";
	public static final String COL_FLAG = "flag";
	public static final String COL_DATA = "data";
	public static final String COL_MESSAGE = "message";
	public static final String COL_LAST_USE = "last_use";
	public static final String COL_ALERT = "alert";
	public static final String COL_CATEGORY = "category";

	// type
	public static final String TYPE_NULL = "NULL";
	public static final String TYPE_INTEGER = "INTEGER";
	public static final String TYPE_TEXT = "TEXT";
	public static final String TYPE_REAL = "REAL";
	public static final String TYPE_BLOB = "BLOB";

	// flag
	public static final Integer FLAG_NONE = 1;
	public static final Integer FLAG_NEW = 2;
	public static final Integer FLAG_SENT = 4;
	public static final Integer FLAG_PAID = 8;
	public static final Integer FLAG_DELETED = 16;
	public static final Integer FLAG_READ = 32;
	public static final Integer FLAG_SELECTED = 64;
}
