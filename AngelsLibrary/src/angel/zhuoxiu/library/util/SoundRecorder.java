package angel.zhuoxiu.library.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Environment;
import android.util.Log;

public class SoundRecorder {
	static String tag = SoundRecorder.class.getSimpleName();

	public interface SoundRecordListener {
		public void onStart();

		public void onProgressChange(long ms);

		public void onFinish(boolean success, File audioTempFile);
	}

	MediaRecorder mRecorder;
	File audioTempFile;
	SoundRecordListener mListener;
	Thread recordTimeThread;
	long startTime;
	boolean isRecording = false;
	Context mContext;

	public SoundRecorder(Context context) {
		mContext = context;
	}

	public void startRecording(String fileName, SoundRecordListener listener) {
		audioTempFile = new File(mContext.getExternalCacheDir(), fileName + ".3gp");
		mListener = listener;
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(audioTempFile.getAbsolutePath());
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOnErrorListener(new OnErrorListener() {
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				stopRecording(); 
			}
		});
		recordTimeThread = new Thread() {
			public void run() {
				startTime = new Date().getTime();
				isRecording = true;
				while (isRecording) {
					mListener.onProgressChange(new Date().getTime() - startTime);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
						stopRecording();
					}
				}
			}
		};
		try {
			mRecorder.prepare();
			mRecorder.start();
			mListener.onStart();
			recordTimeThread.start();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void stopRecording() {
		isRecording = false;
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		if (mListener != null) {
			mListener.onFinish(audioTempFile != null && audioTempFile.exists() && audioTempFile.isFile() && audioTempFile.length() > 0, audioTempFile);
			mListener = null;
		}

	}
}
