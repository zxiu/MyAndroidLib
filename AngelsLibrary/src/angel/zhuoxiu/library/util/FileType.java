package angel.zhuoxiu.library.util;

import java.util.Locale;

public enum FileType {
	UNKNOWN(0, new String[] {}), IMAGE(1, new String[] { "jpg", "jpeg", "bmp", "png" }), VIDEO(2, new String[] { "mp4",
			"3gp" }), AUDIO(4, new String[] { "3gp", "mp3", "amr" }), DOCUMENT(8, new String[] { "pdf", "txt" });

	private int value;
	private String[] extens;

	private FileType(int value, String[] extens) {
		this.value = value;
		this.extens = extens;
	}

	public static FileType getTypeByFileName(String fileName) {
		for (FileType type : FileType.values()) {
			if (type.isType(fileName)) {
				return type;
			}
		}
		return UNKNOWN;
	}

	public boolean isType(String fileName) {
		String fileExt = FileUtils.getFileExtension(fileName.toLowerCase(Locale.ENGLISH));
		for (String ext : extens) {
			if (ext.equalsIgnoreCase(fileExt)) {
				return true;
			}
		}
		return false;
	}

}
