package com.zhuoxiu.angelslibrary.pusher;
/**
	Messages
	When accounts create new message
	
	    Message 1
	        channel: ["channel_account_#{account_id_1}", "channel_account_#{account_id_2}", "channel_account_#{account_id_3}"] # it depends on receivers.
	        event: 'message_incomming'
	        data:
	
	 {
	    conversation_id:  conversation id,
	    msg_id:           message id,
	    body:             converted_body,
	    emoticons:        emoticons,
	    raw_body:         raw body,
	    from_id:          sender account_id,
	    from_name:        sender account fullname,
	    from_icon:        sender account profile icon,
	    created_at:       created at,
	    distance_date:    distance_of_time_in_words_to_now(created_at) # see distance_of_time_in_words_to_now rails helper
	  }
	
	    Message 2
	        channel: ["channel_account_#{account_id_1}", "channel_account_#{account_id_2}", "channel_account_#{account_id_3}"] # it depends on receivers.
	        event: 'new_message_notification'
	        data:
	
	 {
	    id:  message id,
	    conversation_id: conversation id,
	  }

 * @author Zhuo Xiu
 *
 */
public interface PusherConstant {
	public static final String EVENT_NEW_MESSAGE_NOTIFICATION = "new_message_notification";
	public static final String EVENT_MESSAGE_INCOMMING = "message_incomming";
	public static final String EVENT_UPDATED_UNREAD_MESSAGE_COUNT = "updated_unread_message_count";
	public static final String EVENT_CONNECTION_ESTABLISHED = "connection_established";

	public static final String CHANNEL="channel";
	public static final String DATA = "data";
	
	
	public static final String ID="id";
	public static final String CONVERSATION_ID="conversation_id";
	public static final String MES_ID = "msg_id";
	public static final String BODY = "body";
	public static final String EMOTIONS = "emoticons";
	public static final String RAW_BODY = "raw_body";
	public static final String FROM_ID = "from_id";
	public static final String FROM_NAME = "from_name";
	public static final String FROM_ICON = "from_icon";
	public static final String CREATED_AT = "created_at";
	public static final String DISTANCE_DATE="distance_date";
	
}
