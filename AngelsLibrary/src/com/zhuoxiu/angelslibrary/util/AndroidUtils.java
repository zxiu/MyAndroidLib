package com.zhuoxiu.angelslibrary.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.R.integer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class AndroidUtils {
	static final String tag = AndroidUtils.class.getSimpleName();

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isInternetConnected(Context context) {
		return isMobileConnected(context) || isWifiConnected(context);
	}

	public static boolean isMobileConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile 3G Data Network
		if (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) {
			return false;
		}

		State mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		return (mobile == State.CONNECTED || mobile == State.CONNECTING);
	}

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// Wlan Network
		State wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return (wifi == State.CONNECTED || wifi == State.CONNECTING);
	}

	public static boolean isURLConnectable(Context context, String url) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) new URL(url).openConnection();
				urlc.setConnectTimeout(10 * 1000); // 10 s.
				urlc.connect();
				if (urlc.getResponseCode() == 200) { // 200 = "OK" code (http
														// connection is fine).
					Log.wtf("Connection", "Success !");
					return true;
				} else {
					return false;
				}
			} catch (MalformedURLException e1) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		int width = drawable.getIntrinsicWidth();
		width = width > 0 ? width : 1;
		int height = drawable.getIntrinsicHeight();
		height = height > 0 ? height : 1;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static Intent getOpenFileIntent(File file) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		if (extension == null || mimeType == null) {
			return null;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), mimeType);
		return intent;
	}

	public static boolean hasSDCard() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		if (intent == null) {
			return false;
		}
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		return list.size() > 0;
	}

	public static File parseFileByIntentData(Context context, Intent data) {
		File file = null;
		if (data != null && data.getData() != null) {
			String[] proj = { MediaStore.Images.Media.DATA };
			CursorLoader cursorLoader = new CursorLoader(context, data.getData(), proj, null, null, null);
			Cursor cursor = null;
			try {
				cursor = cursorLoader.loadInBackground();
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				file = new File(cursor.getString(column_index));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
		return file;
	}

	static Bitmap generatorContactCountIcon(Context context, Bitmap icon) {
		int iconSize = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
		Bitmap contactIcon = Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);
		Canvas canvas = new Canvas(contactIcon);

		Paint iconPaint = new Paint();
		iconPaint.setDither(true);
		iconPaint.setFilterBitmap(true);
		Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
		Rect dst = new Rect(0, 0, iconSize, iconSize);
		canvas.drawBitmap(icon, src, dst, iconPaint);
		int contacyCount = 11;
		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.RED);
		countPaint.setTextSize(20f);
		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
		canvas.drawText(String.valueOf(contacyCount), iconSize - 18, 25, countPaint);
		return contactIcon;
	} 

	public static void updateShortCut(Context context, String title, int iconResId) {
		Log.i(tag,"updateShortCut");
		Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		if (title != null) {
			shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
		}
		shortcutIntent.putExtra("duplicate", false);
		Intent mainIntent = new Intent(Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		mainIntent.setComponent(new ComponentName(context.getPackageName(), context.getPackageName() + ".MainActivity"));
		mainIntent.setClass(context, context.getClass());

		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, mainIntent);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, iconResId));
		// shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
		// generatorContactCountIcon(context, ((BitmapDrawable)
		// (context.getResources().getDrawable(iconResId))).getBitmap()));
		context.sendBroadcast(shortcutIntent);
	}

}
