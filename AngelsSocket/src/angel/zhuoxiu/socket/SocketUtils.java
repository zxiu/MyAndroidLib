package angel.zhuoxiu.socket;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class SocketUtils {
	public static InetAddress getInetAddress(Context context) {
		int ipAdd = 0;
		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// 检查Wifi状态
		if (!wm.isWifiEnabled()) {
			Toast.makeText(context, "please open wifi", Toast.LENGTH_SHORT).show();
		} else {
			// wm.setWifiEnabled(true);
			WifiInfo wi = wm.getConnectionInfo();
			// 获取32位整型IP地址
			ipAdd = wi.getIpAddress();
			// 把整型地址转换成“*.*.*.*”地址
		}
		return intToInetAddress(ipAdd);
	}

	public static InetAddress intToInetAddress(int hostAddress) {
		byte[] addressBytes = { (byte) (0xff & hostAddress), (byte) (0xff & (hostAddress >> 8)), (byte) (0xff & (hostAddress >> 16)),
				(byte) (0xff & (hostAddress >> 24)) };

		try {
			return InetAddress.getByAddress(addressBytes);
		} catch (UnknownHostException e) {
			throw new AssertionError();
		}
	}
}
