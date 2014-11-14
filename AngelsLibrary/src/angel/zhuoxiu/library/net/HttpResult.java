package angel.zhuoxiu.library.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

public class HttpResult implements org.apache.http.HttpStatus {
	String tag = this.getClass().getSimpleName();
	public static final int CODE_EXCEPTION = -1;
	private int code = 0;
	private byte[] bytes;
	public static final int NOT_CONNECTED = 0;
	public static final String CHARSET = "utf-8";

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "HttpResult [code=" + code + ", getEntityString()=" + getEntityString() + "]";
	}

	public HttpResult setCode(int code) {
		this.code = code;
		return this;
	}

	public void setEntity(HttpEntity entity) {
		if (entity != null) {
			try {
				bytes = EntityUtils.toByteArray(entity);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setEntityString(String entity) {
		bytes = entity.getBytes();
	}

	public String getEntityString() {
		if (bytes != null) {
			try {
				return new String(bytes, CHARSET);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public byte[] getEntityBytes() {
		return bytes;
	}

	public boolean isOK() {
		return (code == SC_OK);
	}

	public boolean isCreated() {
		return (code == SC_CREATED);
	}

	public boolean isNoContent() {
		return (code == SC_NO_CONTENT);
	}

	public boolean isBadGateway() {
		return (code == SC_BAD_GATEWAY);
	}

	public boolean isBadRequest() {
		return (code == SC_BAD_REQUEST);
	}

	public boolean isForbidden() {
		return (code == SC_FORBIDDEN);
	}

	public boolean isException() {
		return (code == CODE_EXCEPTION);
	}
}
