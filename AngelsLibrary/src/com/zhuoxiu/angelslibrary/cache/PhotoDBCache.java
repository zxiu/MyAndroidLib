package com.zhuoxiu.angelslibrary.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;

import com.zhuoxiu.angelslibrary.db.DBHelper;
import com.zhuoxiu.angelslibrary.net.Conn;
import com.zhuoxiu.angelslibrary.net.HttpResult;

public class PhotoDBCache {
	String tag = PhotoDBCache.class.getSimpleName();
	long id;
	String url;
	byte[] data;
	long last_use;
	Context context;

	public interface OnLoadFinishListener {
		public void onFinish(boolean success);
	}

	public PhotoDBCache(Context context, String url, boolean ifLoad, OnLoadFinishListener listener) {
		this.context = context;
		this.url = url;
		if (ifLoad) {
			load(listener);
		}
	}

	public void load(final OnLoadFinishListener listener) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (url == null) {
					return false;
				}
				SQLiteDatabase db = DBHelper.getDB(context, false);
				Cursor cursor = null;
				try {
					cursor = db.query(DBHelper.TABLE_PHOTO, null, DBHelper.COL_URL + " = ?", new String[] { url }, null, null, null);
					if (cursor.moveToFirst()) {
						id = cursor.getLong(cursor.getColumnIndex(DBHelper.COL_ID));
						data = cursor.getBlob(cursor.getColumnIndex(DBHelper.COL_DATA));
						updateLastUse();
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(tag, "photoCache data length=" + data);
					if (cursor != null) {
						cursor.close();
					}
				}
				if (!URLUtil.isValidUrl(url)) {
					return false;
				}
				try {
					Conn conn = new Conn(url, Conn.GET);
					HttpResult result = conn.execute();
					Log.d(tag, "photoCache result=" + result);
					if (result.isOK()) {
						data = result.getEntityBytes();
						db.insert(DBHelper.TABLE_PHOTO, null, getContentValues());
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}

			protected void onPostExecute(Boolean result) {
				listener.onFinish(result);
			};
		}.execute();
	}

	public ContentValues getContentValues() {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COL_DATA, data);
		values.put(DBHelper.COL_URL, url);
		values.put(DBHelper.COL_LAST_USE, new Date().getTime());
		return values;
	}

	public void updateLastUse() {
		last_use = new Date().getTime();
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				SQLiteDatabase db = DBHelper.getDB(context, false);
				ContentValues values = new ContentValues();
				values.put(DBHelper.COL_LAST_USE, last_use);
				db.update(DBHelper.TABLE_PHOTO, values, DBHelper.COL_ID + " = ?", new String[] { Long.toString(id) });
				return null;
			}

		}.execute();
	}

	public byte[] getData() {
		return data;
	}

	public Bitmap getBitmap() {
		try {
			YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, 20, 20, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(new Rect(0, 0, 20, 20), 80, baos);
			byte[] jdata = baos.toByteArray();

			Options options = new Options();
			options.inSampleSize = 8;
			return BitmapFactory.decodeByteArray(data, 0, data.length, options);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getId() {
		return id;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
