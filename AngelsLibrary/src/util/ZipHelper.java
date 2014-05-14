package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.res.AssetManager;
import android.util.Log;

public class ZipHelper {
	static String tag = ZipHelper.class.getSimpleName();

	public ZipHelper(AssetManager am, String path) throws IOException {
		am.open(path);
	}

	public static List<String> getEntryList(String path) {
		List<String> entryNameList = new ArrayList<String>();
		try {
			File file = new File(path);
			ZipFile zipFile = new ZipFile(file);
			Enumeration e = zipFile.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				entryNameList.add(entry.getName());
				Log.i(tag, entry.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return entryNameList;
	}
}
