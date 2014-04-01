package com.zhuoxiu.angelslibrary.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Conn {
	public static final String CHARSET = org.apache.http.protocol.HTTP.UTF_8;
	public static final String GET = HttpGet.METHOD_NAME;
	public static final String POST = HttpPost.METHOD_NAME;
	public static final String PUT = HttpPut.METHOD_NAME;
	public static final String DELETE = HttpDelete.METHOD_NAME;

	static final String AUTHORIZATION = "Authorization";
	static final String ACCESS_TOKEN = "access_token";
	static final String BEARER = "Bearer";

	public static final String tag = Conn.class.getSimpleName();

	private List<BasicNameValuePair> headerList = new ArrayList<BasicNameValuePair>();
	private List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
	public long finishedSize;
	public long totalSize;

	URL url;
	String json;
	String boundary = "SwA" + System.currentTimeMillis() + "SwA";

	static final String HTTP = "http";
	static final String HTTPS = "https";
	HttpURLConnection connection;
	String content;

	public Conn(String url, String method) throws IOException {
		this.url = new URL(url);
		Log.i(tag, "this.url.getProtocol()=" + this.url.getProtocol());
		if (this.url.getProtocol().equalsIgnoreCase(HTTP)) {
			connection = (HttpURLConnection) this.url.openConnection();
		} else if (this.url.getProtocol().equalsIgnoreCase(HTTPS)) {
			connection = (HttpsURLConnection) this.url.openConnection();
		}
		connection.setRequestMethod(method);
		if (method.equalsIgnoreCase(POST)) {
			connection.setDoOutput(true);
		}
		connection.setDoInput(true);
		connection.setReadTimeout(6000);
		connection.setConnectTimeout(5000);
	}

	public Conn(String url, JSONObject obj) throws IOException, JSONException {
		this(url, POST);
		addHeader("Content-Type", "application/json");
		content = obj.toString(2);
	}

	public Conn setContent(String content) {
		this.content = content;
		return this;
	}

	public Conn setAuthorization(String auth_token) {
		return setAuthorization(BEARER, auth_token);
	}

	public Conn setAuthorization(String type, String auth_token) {
		addHeader(AUTHORIZATION, type + " " + auth_token);
		return this;
	}

	public void addHeader(String name, String value) {
		headerList.add(new BasicNameValuePair(name, value));
	}

	public void addParam(String name, String value) {
		paramList.add(new BasicNameValuePair(name, value));
	}

	public HttpResult execute() {
		HttpResult result = new HttpResult();
		try {
			for (NameValuePair header : headerList) {
				Log.i(tag, header.getName() + " : " + header.getValue());
				connection.addRequestProperty(header.getName(), header.getValue());
			}
			if (content != null && connection.getRequestMethod().equalsIgnoreCase(POST)) {
				Log.i(tag, "content=" + content);
				connection.setDoOutput(true);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(content);
				out.close();
			}
			connection.connect();
			result.setCode(connection.getResponseCode());
			InputStream is = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String resultString = new String();
			String lines;
			while ((lines = reader.readLine()) != null) {
				System.out.println(lines);
				resultString += lines;
			}
			result.setEntityString(resultString);
			Log.i(tag, "result=" + result);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}
