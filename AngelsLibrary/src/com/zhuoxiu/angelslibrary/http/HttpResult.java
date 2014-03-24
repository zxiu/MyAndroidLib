package com.zhuoxiu.angelslibrary.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpResult implements org.apache.http.HttpStatus {
	String tag = this.getClass().getSimpleName();

	private int code;
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

	public void setCode(int code) {
		this.code = code;
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
	
}
