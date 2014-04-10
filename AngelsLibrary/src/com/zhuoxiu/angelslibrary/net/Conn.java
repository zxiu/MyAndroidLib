package com.zhuoxiu.angelslibrary.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class Conn {
	public static final String CHARSET = org.apache.http.protocol.HTTP.UTF_8;
	public static final String GET = HttpGet.METHOD_NAME;
	public static final String POST = HttpPost.METHOD_NAME;
	public static final String PUT = HttpPut.METHOD_NAME;
	public static final String DELETE = HttpDelete.METHOD_NAME;

	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	public static final String CONTENT_TYPE_JSON = "application/json";

	static final String AUTHORIZATION = "Authorization";
	static final String ACCESS_TOKEN = "access_token";
	static final String BEARER = "Bearer";
	static final String CONTENT_TYPE = "Content-Type";

	public static final String tag = Conn.class.getSimpleName();

	private List<BasicNameValuePair> headerList = new ArrayList<BasicNameValuePair>();
	private List<BasicNameValuePair> paramList = new ArrayList<BasicNameValuePair>();
	private List<NameValuePair> textBodyList = new ArrayList<NameValuePair>();
	private List<File> fileList = new ArrayList<File>();
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

	public void addTextBody(String name, String value) {
		textBodyList.add(new BasicNameValuePair(name, value));
	}

	public void addFile(File file) {
		if (file != null && file.isFile() && file.exists()) {
			fileList.add(file);
			totalSize += file.length();
		}
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

	public void setContentType(String type) {
		addHeader(CONTENT_TYPE, type);
	}

	public Conn setAuthorizationBearer(String auth_token) {
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

	class ProgressiveEntity implements HttpEntity {

		HttpEntity yourEntity;

		public ProgressiveEntity(HttpEntity yourEntity) {
			this.yourEntity = yourEntity;
		}

		@Override
		public void consumeContent() throws IOException {
			yourEntity.consumeContent();
		}

		@Override
		public InputStream getContent() throws IOException, IllegalStateException {
			return yourEntity.getContent();
		}

		@Override
		public Header getContentEncoding() {
			return yourEntity.getContentEncoding();
		}

		@Override
		public long getContentLength() {
			return yourEntity.getContentLength();
		}

		@Override
		public Header getContentType() {
			return yourEntity.getContentType();
		}

		@Override
		public boolean isChunked() {
			return yourEntity.isChunked();
		}

		@Override
		public boolean isRepeatable() {
			return yourEntity.isRepeatable();
		}

		@Override
		public boolean isStreaming() {
			return yourEntity.isStreaming();
		} // CONSIDER put a _real_ delegator into here!

		@Override
		public void writeTo(OutputStream outstream) throws IOException {

			class ProxyOutputStream extends FilterOutputStream {
				/**
				 * @author Stephen Colebourne
				 */

				public ProxyOutputStream(OutputStream proxy) {
					super(proxy);
				}

				public void write(int idx) throws IOException {
					out.write(idx);
				}

				public void write(byte[] bts) throws IOException {
					out.write(bts);
				}

				public void write(byte[] bts, int st, int end) throws IOException {
					out.write(bts, st, end);
				}

				public void flush() throws IOException {
					out.flush();
				}

				public void close() throws IOException {
					out.close();
				}
			} // CONSIDER import this class (and risk more Jar File Hell)

			class ProgressiveOutputStream extends ProxyOutputStream {
				public ProgressiveOutputStream(OutputStream proxy) {
					super(proxy);
				}

				public void write(byte[] bts, int st, int end) throws IOException {
					// Log.i(tag, "bts=" + bts.length + " st=" + st + " end=" +
					// end);
					// FIXME Put your progress bar stuff here!

					out.write(bts, st, end);
				}
			}

			yourEntity.writeTo(new ProgressiveOutputStream(outstream));
		}
	};

	public HttpResult execute() {
		HttpResult result = new HttpResult();
		try {
			for (NameValuePair header : headerList) {
				connection.setRequestProperty(header.getName(), header.getValue());
			}
			if (connection.getRequestMethod().equalsIgnoreCase(POST)) {
				connection.setDoOutput(true);
				if ((textBodyList.size() > 0 || fileList.size() > 0) && connection.getRequestMethod().equalsIgnoreCase(POST)) {
					connection.setRequestProperty("Content-Type", "Multipart/Form-Data; Boundary = " + boundary);
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					builder.setBoundary(boundary);
					for (int i = 0; i < textBodyList.size(); i++) {
						builder.addTextBody(textBodyList.get(i).getName(), textBodyList.get(i).getValue(), ContentType.TEXT_PLAIN);
					}
					for (int i = 0; i < fileList.size(); i++) {
						builder.addPart("[message][message_attachments_attributes][" + i + "][file]", new FileBody(fileList.get(i)));
					}
					final HttpEntity entity = builder.build();
					ProgressiveEntity progressiveEntity = new ProgressiveEntity(entity);
					// entity.writeTo(connection.getOutputStream());

					OutputStream os = new OutputStream() {
						private StringBuilder string = new StringBuilder();

						@Override
						public void write(int b) throws IOException {
							this.string.append((char) b);
						}

						// Netbeans IDE automatically overrides this toString()
						public String toString() {
							return this.string.toString();
						}
					};
					progressiveEntity.writeTo(os);

					connection.getOutputStream().close();
				} else if (!TextUtils.isEmpty(content)) {
					Log.i(tag, "content=" + content);
					OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
					osw.write(content);
					osw.close();
				}
			}
			connection.connect();
			result.setCode(connection.getResponseCode());

			InputStream is = null;
			if (result.isOK() || result.isCreated()) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String resultString = new String();
			String lines;
			while ((lines = reader.readLine()) != null) {
				System.out.println(lines);
				resultString += lines;
			}
			result.setEntityString(resultString);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
}
