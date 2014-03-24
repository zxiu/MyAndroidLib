package com.xiuzhuo.dbhelper;

public interface DBConstant {

	// type
	public static final String TYPE_NULL = "NULL";
	public static final String TYPE_INTEGER = "INTEGER";
	public static final String TYPE_TEXT = "TEXT";
	public static final String TYPE_REAL = "REAL";
	public static final String TYPE_BLOB = "BLOB";

	// flag
	public static final int FLAG_UNSENT = 0;
	public static final int FLAG_SENT = 1;
	public static final int FLAG_PAID = 2;
	public static final int FLAG_NORMAL = 3;
	public static final int FLAG_DELETED = 4;
}
