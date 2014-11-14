package angel.zhuoxiu.library.media;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

public class MediaManager {
	static final String TAG = MediaManager.class.getSimpleName();

	static final LongSparseArray<OnFetchMediaListener> fetchMediaListenerArray = new LongSparseArray<OnFetchMediaListener>();

	public interface OnFetchMediaListener {
		void onFinish(boolean success, File file);
	};

	static Intent getPreparedIntent(Context context) {
		Intent intent = new Intent(context, MediaActivity.class);
		long actionId = 0;
		do {
			actionId = System.currentTimeMillis();
		} while (fetchMediaListenerArray.get(actionId) != null);
		intent.putExtra(MediaActivity.KEY_ACTION_ID, actionId);
		return intent; 
	}

	public static Intent getImageCaptureIntent(Context context) {
		Intent intent = getPreparedIntent(context);
		intent.putExtra(MediaActivity.KEY_ACTION_TYPE, MediaActivity.REQUEST_IMAGE_CAPTURE);
		return intent;
	}

	public static void captureImage(Context context, OnFetchMediaListener onFetchMediaListener) {
		Log.d(TAG, "captureImage");
		Intent intent = getImageCaptureIntent(context);
		long actionId = intent.getLongExtra(MediaActivity.KEY_ACTION_ID, 0);
		if (actionId != 0 && fetchMediaListenerArray.get(actionId) == null) {
			fetchMediaListenerArray.put(actionId, onFetchMediaListener);
			Log.d(TAG, "startActivity " + intent);
			context.startActivity(intent);
		}
	}

}
