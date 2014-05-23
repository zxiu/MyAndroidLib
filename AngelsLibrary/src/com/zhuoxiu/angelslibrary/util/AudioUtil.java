package com.zhuoxiu.angelslibrary.util;

import java.io.IOException;

import android.media.MediaRecorder;
import android.util.Log;

public class AudioUtil {
	static String tag = AudioUtil.class.getSimpleName();
	static MediaRecorder mRecorder;

	public static void startRecording(String fileName) {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(fileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
		} catch (IOException e) {  
			Log.e(tag, "prepare() failed");
		}
		mRecorder.start();
	}
	public static void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
}
