package com.zhuoxiu.angelslibrary.media;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

public class MediaManager {
	public static final int REQUEST_IMAGE_CAPTURE = 5000;
	public static final int REQUEST_IMAGE_VIEW = 5001;
	public static final String KEY_ACTION_TYPE = "action_type";

	Context mContext;
	OnFetchMediaListener mFetchMediaListener;

	public interface OnFetchMediaListener {
		void onFinish(boolean success, File file);
	};

	private MediaManager(Context context) {
		mContext = context;
	}

	public static MediaManager getInstance(Context context) {
		return new MediaManager(context);
	}

	public void captureImage(OnFetchMediaListener fetchMediaListener) {
		mFetchMediaListener = fetchMediaListener;
		mContext.startActivity(getImageCaptureIntent());
	}

	Intent getImageCaptureIntent() {
		Intent intent = new Intent(mContext, MediaActivity.class);
		intent.putExtra(KEY_ACTION_TYPE, REQUEST_IMAGE_CAPTURE);
		return intent;
	}

	public class MediaActivity extends Activity {

		int requestCode;
		File mMediaFile;

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			requestCode = getIntent().getIntExtra(KEY_ACTION_TYPE, requestCode);
			switch (requestCode) {
			case REQUEST_IMAGE_CAPTURE:
				imageCapture();
				break;
			default:
				finish();
				break;
			}
		}

		protected void imageCapture() {
			Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
				File photoFile = null;
				try {
					photoFile = createImageFile();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				if (photoFile != null) {
					takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
					startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
				} else {
					finish();
				}
			}
		}
 
		private File createImageFile() throws IOException {
			// Create an image file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
			String imageFileName = "JPEG_" + timeStamp + "_";
			File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			mMediaFile = File.createTempFile(imageFileName, ".jpg", storageDir);
			// Save a file: path for use with ACTION_VIEW intents
			return mMediaFile;
		}

		public void finish() {
			boolean success = true;
			if (mMediaFile == null || !mMediaFile.exists() || mMediaFile.isDirectory() || mMediaFile.length() == 0) {
				success = false;
				if (mMediaFile.exists()) {
					mMediaFile.delete();
					mMediaFile = null;
				}
			}
			mFetchMediaListener.onFinish(success, mMediaFile);
			super.finish();
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			finish();
		}
	}

}
