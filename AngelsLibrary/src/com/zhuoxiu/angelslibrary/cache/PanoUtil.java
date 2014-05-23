package com.zhuoxiu.angelslibrary.cache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.zhuoxiu.angelslibrary.net.ConnOld;
import com.zhuoxiu.angelslibrary.net.ConnOld.OnDownloadListener;
import com.zhuoxiu.angelslibrary.net.URLCoder;

public class PanoUtil {
	static String tag = PanoUtil.class.getSimpleName();
	static PanoDiskCache diskCache = new PanoDiskCache();
	static PanoMemCache memCache = new PanoMemCache();
	int width;
	int height;
	String key;

	OnDownloadListener downloadListener;

	public PanoUtil(Context context) {
		WindowManager windowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		windowMgr.getDefaultDisplay().getMetrics(outMetrics);
		width = outMetrics.widthPixels;
		height = outMetrics.heightPixels;
	}

	public PanoUtil(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public boolean hasCache(String url) {
		String key = createKey(url);
		return (diskCache.isCached(key) || memCache.isCached(key));
	}

	public File loadFile(String url, OnDownloadListener downloadListener) throws IOException {
		this.key = createKey(url);
		File file = PanoDiskCache.getFileFromDisk(key);
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			ConnOld conn = new ConnOld(url, ConnOld.GET);
			conn.downloadFileWithProgress(file, downloadListener);
		}
		return file;
	}

	public Bitmap getBitmap(String url) throws IOException {
		String key = createKey(url);
		Bitmap bitmap = memCache.getBitmapFromMem(key);
		if (bitmap == null) {
			Log.d(tag, "cache bitmap not in memory url=" + url);
			bitmap = diskCache.getBitmapFromDisk(key);
		} else {
			Log.e(tag, "cache bitmap in memory url=" + url);
		}
		if (bitmap == null) {
			Log.d(tag, "cache bitmap not in disk url=" + url);
			bitmap = downloadBitmap(url);
			Log.d(tag, "size= downloadbitmap = " + bitmap);
			if (bitmap != null) {
				memCache.addBitmapToCache(key, bitmap);
				diskCache.addBitmapToCache(key, bitmap);
			}
		} else {
			Log.e(tag, "cache bitmap in disk url=" + url);
		}
		return bitmap;
	}

	public void addBitmapToCache(String url, Bitmap bitmap) {
		String key = createKey(url);
		if (bitmap != null) {
			diskCache.addBitmapToCache(key, bitmap);
			memCache.addBitmapToCache(key, bitmap);
		}
	}

	public String createKey(String url) {
		try {
			return URLCoder.encode(url);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Bitmap downloadBitmap(String url) {
		Bitmap bitmap = null;
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.connect();
			bitmap = BitmapFactory.decodeStream(conn.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	public byte[] readInputStream(InputStream in) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
		in.close();
		return out.toByteArray();
	}

	/**
	 * Scale Bitmap
	 * 
	 * @param context
	 * @param in
	 * @return
	 */
	private Bitmap scaleBitmap(byte[] data) {
		if (data == null) {
			return null;
		}
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int imgWidth = options.outWidth;
		int imgHeight = options.outHeight;
		if (imgWidth > width || imgHeight > height) {
			options.inSampleSize = calculateInSampleSize(options, width, height);
		}
		Log.i(tag, "options.inSampleSize=" + options.inSampleSize);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
		return bitmap;
	}

	/** 
	 * 计算Bitmap抽样倍数
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
			if (inSampleSize < 1) {
				inSampleSize = 1;
			}
		}

		return inSampleSize;
	}
}
