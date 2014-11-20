package angel.zhuoxiu.library.util;

import java.io.File;

import android.os.Environment;
public class FileUtils {


	public static String getDirectory(String foldername) {
		// if (!foldername.startsWith(".")) {
		// foldername = "." + foldername;
		// }
		File directory = null;
		directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + foldername);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		return directory.getAbsolutePath();
	}

	public static String getFileExtension(String filename) {
		String extension = "";
		try {
			extension = filename.substring(filename.lastIndexOf(".") + 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return extension;
	}

}
