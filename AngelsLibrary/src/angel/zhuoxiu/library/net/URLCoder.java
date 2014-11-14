package angel.zhuoxiu.library.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;

public class URLCoder {

	protected static final char[] hexadecimal = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	// Array containing the safe characters set.
	protected BitSet safeCharacters = new BitSet(256);

	static URLCoder coder = new URLCoder();

	private URLCoder() {
		for (char i = 'a'; i <= 'z'; i++) {
			safeCharacters.set(i);
		}
		for (char i = 'A'; i <= 'Z'; i++) {
			safeCharacters.set(i);
		}
		for (char i = '0'; i <= '9'; i++) {
			safeCharacters.set(i);
		}
	}

	public static String encode(String path) throws IOException {
		StringBuilder rewrittenPath = new StringBuilder(path.length());
		int maxBytesPerChar = 10;
		ByteArrayOutputStream byteBuff = new ByteArrayOutputStream(maxBytesPerChar);

		OutputStreamWriter writer = new OutputStreamWriter(byteBuff, "UTF-8");

		for (int i = 0; i < path.length(); i++) {
			int c = path.charAt(i);
			if (coder.safeCharacters.get(c)) {
				rewrittenPath.append((char) c);
			} else {
				writer.write((char) c);
				writer.flush();

				byte[] ba = byteBuff.toByteArray();
				for (int j = 0; j < ba.length; j++) {
					byte toEncode = ba[j];
					rewrittenPath.append('%');

					int low = toEncode & 0x0f;
					int high = (toEncode & 0xf0) >> 4;

					rewrittenPath.append(hexadecimal[high]);
					rewrittenPath.append(hexadecimal[low]);
				}
				byteBuff.reset();
			}
		}
		return rewrittenPath.toString();
	}
}