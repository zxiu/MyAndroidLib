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
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.zhuoxiu.angelslibrary.net.ConnOld;
import com.zhuoxiu.angelslibrary.net.ConnOld.OnDownloadListener;

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
		String key = generateKey(url);
		return (diskCache.isCached(key) || memCache.isCached(key));
	}

	public File loadFile(String url, OnDownloadListener downloadListener) throws IOException {
		this.key = generateKey(url);
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
		String key = generateKey(url);
		Bitmap bitmap = memCache.getBitmapFromMem(key);
		if (bitmap == null) {
			bitmap = diskCache.getBitmapFromDisk(key);
		}
		if (bitmap == null) {
			bitmap = downloadBitmap(url);
			if (bitmap != null) {
				diskCache.addBitmapToCache(key, bitmap);
				memCache.addBitmapToCache(key, bitmap);
			}
		}
		return bitmap;
	}

	public void addBitmapToCache(String url, Bitmap bitmap) {
		String key = generateKey(url);
		if (bitmap != null) {
			diskCache.addBitmapToCache(key, bitmap);
			memCache.addBitmapToCache(key, bitmap);
		}
	}
 
	public String generateKey(String url) {
		return new String(Base64.encodeToString(url.getBytes(), Base64.URL_SAFE));
	}

	public Bitmap downloadBitmap(String url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn = (HttpURLConnection) new URL(url).openConnection();
		conn.connect();
		final Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
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
