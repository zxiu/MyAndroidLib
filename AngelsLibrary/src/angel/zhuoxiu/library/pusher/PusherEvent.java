package angel.zhuoxiu.library.pusher;

import org.json.JSONObject;

public class PusherEvent {
	public static final String EVENT_NEW_MESSAGE_NOTIFICATION = "new_message_notification";
	public static final String EVENT_MESSAGE_INCOMMING = "message_incomming";
	public static final String EVENT_UPDATED_UNREAD_MESSAGE_COUNT = "updated_unread_message_count";
	public static final String EVENT_CONNECTION_ESTABLISHED = "connection_established";

	private String eventName;
	private JSONObject eventData;
	private String channelName;

	public PusherEvent(String eventName, JSONObject eventData, String channelName) {
		this.eventName = eventName;
		this.eventData = eventData;
		this.channelName = channelName;
	}

	@Override
	public String toString() {
		return "PusherEvent [eventName=" + eventName + ", eventData=" + eventData + ", channelName=" + channelName + "]";
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public JSONObject getEventData() {
		return eventData;
	}

	public String getEventDataJson() {
		return eventData.toString();
	}

	public void setEventData(JSONObject eventData) {
		this.eventData = eventData;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

}
