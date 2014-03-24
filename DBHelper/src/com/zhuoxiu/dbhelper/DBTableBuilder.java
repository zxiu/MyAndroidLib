package com.zhuoxiu.dbhelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBTableBuilder implements DBConstant {

	String tag = this.getClass().getSimpleName();

	String sql = new String();
	int columnSize = 0;
	SQLiteDatabase db; 
	String tableName;
	List<String> columnList = new ArrayList<String>();
	List<String> typeList = new ArrayList<String>();
	List<String> primaryKeyList = new ArrayList<String>();
	private boolean hasFlag;
	private int defaultFlag = FLAG_UNSENT;

	public static DBTableBuilder getInstance(SQLiteDatabase db, String tableName) {
		return new DBTableBuilder(db, tableName);
	}

	private DBTableBuilder(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	public DBTableBuilder addColumn(String columnName, String type, boolean primaryKey) {
		if (columnName != null && type != null) {
			columnName = columnName.toLowerCase(Locale.ENGLISH);
			columnList.add(columnName);
			typeList.add(type);
			if (primaryKey) {
				primaryKeyList.add(columnName);
			}
		}
		return this;
	}

	public DBTableBuilder addColumn(String columnName, String type) {
		return addColumn(columnName, type, false);
	}

	public DBTableBuilder addFlagColumn(int flag) {
		hasFlag = true;
		defaultFlag = flag;
		return this;
	}

	public DBTableBuilder addFlagColumn() {
		return addFlagColumn(FLAG_UNSENT);
	}

	public void build() {
		sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
		for (int i = 0; i < columnList.size(); i++) {
			if (i > 0) {
				sql += ", ";
			}
			sql += columnList.get(i) + " " + typeList.get(i);
		}
		if (hasFlag) {
			if (columnList.size() > 0) {
				sql += ", ";
			}
			sql += "flag INTEGER DEFAULT " + defaultFlag;
		}

		if (primaryKeyList.size() > 0) {
			sql += ", PRIMARY KEY (";
			for (int i = 0; i < primaryKeyList.size(); i++) {
				if (i > 0) {
					sql += ", ";
				}
				sql += primaryKeyList.get(i);
			}
			sql += ")";
		}

		sql += ")";
		Log.i(tag, "sql = " + sql);
		db.execSQL(sql);
	}

}