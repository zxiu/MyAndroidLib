package com.zhuoxiu.angelslibrary.util;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.Uri;
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
}
