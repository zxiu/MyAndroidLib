package angel.zhuoxiu.library.media;

/**
	
	<uses-feature android:name="android.hardware.camera" android:required="true" />
	<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"  android:maxSdkVersion="18"/>
	
 */

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import angel.zhuoxiu.library.media.MediaManager.OnFetchMediaListener;

public class MediaActivity extends Activity {
	static final String TAG = MediaActivity.class.getSimpleName();

	static final int REQUEST_IMAGE_CAPTURE = 5000;
	static final int REQUEST_IMAGE_VIEW = 5001;
	static final String KEY_ACTION_TYPE = "action_type";
	static final String KEY_ACTION_ID = "action_id";

	OnFetchMediaListener mFetchMediaListener;
	int requestCode;
	long actionId;
	File mMediaFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		requestCode = getIntent().getIntExtra(KEY_ACTION_TYPE, requestCode);
		actionId = getIntent().getLongExtra(KEY_ACTION_ID, 0);
		mFetchMediaListener = MediaManager.fetchMediaListenerArray.get(actionId);
		Log.i(TAG, "onCreate requestCode=" + requestCode);
		switch (requestCode) {
		case REQUEST_IMAGE_CAPTURE:
			doImageCapture();
			break;
		default:
			super.finish();
			break;
		}
	}

	protected void doImageCapture() {
		Log.i(TAG, "doImageCapture");
		Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (imageCaptureIntent.resolveActivity(getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			if (photoFile != null) {
				imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(imageCaptureIntent, REQUEST_IMAGE_CAPTURE);
			} else {
				finish();
			}
		}
	}

	/**  
	 * @param uri  
	 */
	public void cropPhoto(File file) {
		Intent cropIntent = new Intent("com.android.camera.action.CROP");
		cropIntent.setDataAndType(Uri.fromFile(file), "image/*");
		cropIntent.putExtra("crop", "true");
		cropIntent.putExtra("aspectX", 1);
		cropIntent.putExtra("aspectY", 1);
		cropIntent.putExtra("outputX", 150);
		cropIntent.putExtra("outputY", 150);
		cropIntent.putExtra("return-data", true);
		if (cropIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(cropIntent, 3);
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		Log.i(TAG, "createImageFile");
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		// mMediaFile = File.createTempFile(imageFileName, ".jpg", storageDir);
		mMediaFile = new File(storageDir, imageFileName + ".jpg");
		mMediaFile.createNewFile();
		// Save a file: path for use with ACTION_VIEW intents
		Log.i(TAG, "createImageFile mMediaFile=" + mMediaFile);
		return mMediaFile;
	}

	public void finish() {
		Log.i(TAG, "finish mMediaFile=" + mMediaFile);
		boolean success = true;
		super.finish();
		if (mMediaFile == null || !mMediaFile.exists() || mMediaFile.isDirectory() || mMediaFile.length() == 0) {
			success = false;
			if (mMediaFile != null && mMediaFile.exists()) {
				mMediaFile.delete();
				mMediaFile = null;
			}
		}
//		if (mMediaFile != null) {
//			cropPhoto(mMediaFile);
//		}
		if (mFetchMediaListener != null) {
			mFetchMediaListener.onFinish(success, mMediaFile);
			MediaManager.fetchMediaListenerArray.remove(actionId);
		}
		Log.i(TAG, "finish mMediaFile=" + mMediaFile);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult resultCode=" + resultCode);
		finish();
	}
}
