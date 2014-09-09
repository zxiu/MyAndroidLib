package com.zhuoxiu.angelslibrary.util;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
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

	public static boolean hasNet(Context context) {
		return hasMobileNet(context) || hasWifiNet(context);
	}

	public static boolean hasMobileNet(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// mobile 3G Data Network
		if (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) {
			return false;
		}

		State mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		return (mobile == State.CONNECTED || mobile == State.CONNECTING);
	}

	public static boolean hasWifiNet(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		// Wlan Network
		State wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		return (wifi == State.CONNECTED || wifi == State.CONNECTING);
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
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
}
