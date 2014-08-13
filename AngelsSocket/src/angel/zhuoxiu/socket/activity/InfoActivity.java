package angel.zhuoxiu.socket.activity;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends Activity {
	private WifiManager my_wifiManager;
	private WifiInfo wifiInfo;
	private DhcpInfo dhcpInfo;

	TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		setContentView(tv);

		my_wifiManager = ((WifiManager) getSystemService("wifi"));
		dhcpInfo = my_wifiManager.getDhcpInfo();
		wifiInfo = my_wifiManager.getConnectionInfo();
	}

	@Override
	protected void onResume() {
		super.onResume();
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				StringBuilder sb = new StringBuilder();
				sb.append("Network Info：");
				sb.append("\nipAddress：" + intToIp(dhcpInfo.ipAddress));
				sb.append("\nnetmask：" + intToIp(dhcpInfo.netmask));
				sb.append("\ngateway：" + intToIp(dhcpInfo.gateway));
				sb.append("\nserverAddress：" + intToIp(dhcpInfo.serverAddress));
				sb.append("\ndns1：" + intToIp(dhcpInfo.dns1));
				sb.append("\ndns2：" + intToIp(dhcpInfo.dns2));
				sb.append("\n\n");
				System.out.println(intToIp(dhcpInfo.ipAddress));
				System.out.println(intToIp(dhcpInfo.netmask));
				System.out.println(intToIp(dhcpInfo.gateway));
				System.out.println(intToIp(dhcpInfo.serverAddress));
				System.out.println(intToIp(dhcpInfo.dns1));
				System.out.println(intToIp(dhcpInfo.dns2));
				System.out.println(dhcpInfo.leaseDuration);

				sb.append("Wifi Info：");
				sb.append("\nIpAddress：" + intToIp(wifiInfo.getIpAddress()));
				sb.append("\nMacAddress：" + wifiInfo.getMacAddress());
				return sb.toString();
			}

			protected void onPostExecute(String result) {
				tv.setText(result);
			};
		}.execute();
	}

	private String intToIp(int paramInt) {
		return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
	}


}
