package angel.zhuoxiu.library.db;

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
	List<Column> colList = new ArrayList<Column>();
	List<String> columnList = new ArrayList<String>();
	List<String> typeList = new ArrayList<String>();
	List<String> primaryKeyList = new ArrayList<String>();
	private boolean hasFlag;
	private int defaultFlag;

	public static DBTableBuilder getInstance(SQLiteDatabase db, String tableName) {
		return new DBTableBuilder(db, tableName);
	}

	private DBTableBuilder(SQLiteDatabase db, String tableName) {
		this.db = db;
		this.tableName = tableName;
	}

	public DBTableBuilder addColumn(String columnName, String type, String defaultValue, boolean primaryKey, boolean autoIncrement) {
		if (columnName != null && type != null) {
			colList.add(new Column(columnName, type, defaultValue, primaryKey, autoIncrement));

			columnName = columnName.toLowerCase(Locale.ENGLISH);
			columnList.add(columnName);
			typeList.add(type);
			if (primaryKey) {
				primaryKeyList.add(columnName);
			}
		}
		return this;
	}

	public DBTableBuilder addColumn(String columnName, boolean primaryKey, boolean autoIncrement) {
		this.addColumn(columnName, TYPE_INTEGER, null, primaryKey, autoIncrement);
		return this;
	}

	public DBTableBuilder addColumn(String columnName, String type, boolean primaryKey) {
		this.addColumn(columnName, type, null, primaryKey, false);
		return this;
	}

	public DBTableBuilder addColumn(String columnName, String type, String defaultValue) {
		return addColumn(columnName, type, defaultValue, false, false);
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
		return addFlagColumn(FLAG_NONE);
	}

	public void build() {
		sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(";
		for (int i = 0; i < colList.size(); i++) {
			Column column = colList.get(i);
			sql += (i > 0) ? ", " : "";
			sql += column.name + " " + column.type;
			sql += column.defaultValue != null ? " DEFAULT " + column.defaultValue : "";
			sql += column.isPrimaryKey ? " PRIMARY KEY" : "";
			sql += column.autoIncrement ? " AUTOINCREMENT" : "";
		}
		sql += ")";
		Log.i(tag, "sql = " + sql);
		db.execSQL(sql);
	}

	class Column {
		public String name;
		public String type;
		public String defaultValue;
		public boolean isPrimaryKey;
		public boolean autoIncrement;

		public Column(String name, String type, boolean isPrimary, boolean autoIncrement) {
			this(name, type, null, isPrimary, autoIncrement);
		}

		public Column(String name, String type, String defaultValue, boolean isPrimary, boolean autoIncrement) {
			this.name = name;
			this.type = type;
			this.defaultValue = defaultValue;
			this.isPrimaryKey = isPrimary;
			this.autoIncrement = autoIncrement;
		}

	}

}