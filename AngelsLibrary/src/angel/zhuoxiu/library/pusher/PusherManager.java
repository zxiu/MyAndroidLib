package angel.zhuoxiu.library.pusher;

import android.util.Log;

public class PusherManager {
	private static final String PUSHER_APP_KEY = "4cba300554a31ad19f60";
	private static final String PUSHER_APP_SECRET = "7d607f1c2b62bb302d29";
	@SuppressWarnings("unused")
	private static String tag = PusherManager.class.getSimpleName();
	private static String channel_base = "channel_account_";
	private static Pusher pusher;

	private PusherManager() {

	}

	public interface OnConnectListener {
		public void onConnect();
	}

	public static void initiate(final long account_id, final OnConnectListener listener) {
		Log.i("pusher", "initiate");
		destroy();
		new Thread() {
			public void run() {
				pusher = new Pusher(PUSHER_APP_KEY, PUSHER_APP_SECRET);
				pusher.subscribe(channel_base + account_id);
				pusher.connect();
				if (listener != null) {
					listener.onConnect();
				}
			};
		}.start(); 
	}

	public static void destroy() {
		if (pusher != null) {
			pusher.unbindAll();
			pusher.disconnect();
			pusher = null;
		}
	}

	public static void bind(String eventName, PusherCallback callback) {
		if (pusher != null) {
			pusher.bind(eventName, callback);
		}
	}

	public static void unbind(PusherCallback callback) {
		if (pusher != null) {
			pusher.unbind(callback);
		}
	}

}
