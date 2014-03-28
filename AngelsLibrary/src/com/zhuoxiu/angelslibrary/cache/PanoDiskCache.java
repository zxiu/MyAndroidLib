package com.zhuoxiu.angelslibrary.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class PanoDiskCache {

	private static final String tag = PanoDiskCache.class.getSimpleName();

	// Folder of cache
	private static final String CACHE_DIR = "panoCache";
	private static final String CACHE_FILE_SUFFIX = ".cache";

	private static final int MB = 1024 * 1024;
	private static final int CACHE_SIZE = 500; // 500M
	private static final int SDCARD_CACHE_THRESHOLD = 10;

	public PanoDiskCache() {
		removeCache(getDiskCacheDir());
	}

	public boolean isCached(String key) {
		return getFileFromDisk(key).exists();
	}

	/**
	 * Get Bitmap from Disk
	 * 
	 * @param key
	 * @return Bitmap, or null if doesn't exist
	 */
	public Bitmap getBitmapFromDisk(String key) {
		File file = getFileFromDisk(key);
		if (file != null && file.exists()) {
			Log.i(tag,"getBitmapFromDisk");
			Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
			if (bitmap == null) {
				file.delete();
			} else {
				updateLastModified(file.getPath());
				return bitmap;
			}
		}
		return null;
	}

	public static File getFileFromDisk(String key) {
		if (key==null){
			return null;
		}
		String dir = getDiskCacheDir(); 
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File file = new File(dir, genCacheFileName(key));
		return file;
	}

	/**
	 * 将Bitmap写入文件缓存
	 * 
	 * @param bitmap
	 * @param key
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		// 判断当前SDCard上的剩余空间是否足够用于文件缓存
		if (SDCARD_CACHE_THRESHOLD > calculateFreeSpaceOnSd()) {
			return;
		}

		File file = getFileFromDisk(key);
		Log.i(tag, file.getAbsolutePath());
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			Log.e(tag, "FileNotFoundException");
		} catch (IOException e) {
			Log.e(tag, "IOException" + e.getMessage());
		}
	}

	/**
	 * 清理文件缓存
	 * 当缓存文件总容量超过CACHE_SIZE或SDCard的剩
	 * 余空间小于SDCARD_CACHE_THRESHOLD时，将删除40%
	 * 最近没有被使用的文件
	 * 
	 * @param dirPath
	 * @return
	 */
	private boolean removeCache(String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if (files == null || files.length == 0) {
			return true;
		}
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return false;
		}

		int dirSize = 0;
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().contains(CACHE_FILE_SUFFIX)) {
				dirSize += files[i].length();
			}
		}
		if (dirSize > CACHE_SIZE * MB
				|| SDCARD_CACHE_THRESHOLD > calculateFreeSpaceOnSd()) {
			int removeFactor = (int) (0.4 * files.length + 1);
			Arrays.sort(files, new FileLastModifiedSort());
			for (int i = 0; i < removeFactor; i++) {
				if (files[i].getName().contains(CACHE_FILE_SUFFIX)) {
					files[i].delete();
				}
			}
		}

		if (calculateFreeSpaceOnSd() <= SDCARD_CACHE_THRESHOLD) {
			return false;
		}
		return true;
	}

	/**
	 * set last modified time
	 * 
	 * @param path
	 */
	private void updateLastModified(String path) {
		File file = new File(path);
		long time = System.currentTimeMillis();
		file.setLastModified(time);
	}

	/**
	 * Calculate free space on SD card	 
	 *  
	 * @return space in MB
	 */
	private int calculateFreeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		@SuppressWarnings("deprecation")
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;
		return (int) sdFreeMB;
	}

	/**
	 * 
	 * @param key
	 * @return cache file name
	 */
	public static String genCacheFileName(String key) {
		// String[] strs = key.split(File.separator);
		// return strs[strs.length - 1] + CACHE_FILE_SUFFIX;

		String fileName = key + CACHE_FILE_SUFFIX;
		return fileName;
	}

	/**
	 * 获取磁盘缓存目录
	 * 
	 * @return
	 */
	private static String getDiskCacheDir() {
		return getSDPath() + File.separator + CACHE_DIR;
	}

	/**
	 * 获取SDCard目录
	 * 
	 * @return
	 */
	private static String getSDPath() {
//		return Cube7App.context.getCacheDir().getAbsolutePath();
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	/**
	 * 根据文件最后修改时间进行排序
	 */
	private class FileLastModifiedSort implements Comparator<File> {
		@Override
		public int compare(File lhs, File rhs) {
			if (lhs.lastModified() > rhs.lastModified()) {
				return 1;
			} else if (lhs.lastModified() == rhs.lastModified()) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}