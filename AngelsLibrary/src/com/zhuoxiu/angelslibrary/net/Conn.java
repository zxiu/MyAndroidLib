package com.zhuoxiu.angelslibrary.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.webkit.URLUtil;

public class Conn implements Constant {
	public interface OnDownloadListener {
		public void onStart(long totalSize);

		public void onProgress(long finishedSize, long totalSize);

		public void onFinish(boolean success, File downloadedFile);
	}

	static final int BUFFER_SIZE = 4096;

	public static final String CHARSET = org.apache.http.protocol.HTTP.UTF_8;
	public static final String GET = HttpGet.METHOD_NAME;
	public static final String POST = HttpPost.METHOD_NAME;
	public static final String PUT = HttpPut.METHOD_NAME;
	public static final String DELETE = HttpDelete.METHOD_NAME;

	public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
	public static final String CONTENT_TYPE_JSON = "application/json";

	static final String ACCESS_TOKEN = "access_token";
	static final String BEARER = "Bearer";
	static final String CONTENT_TYPE = "Content-Type";
	static final String APPLICATION_JSON = "application/json; charset=utf8";

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

	HttpURLConnection conn;
	String content;

	public Conn(String url) throws IOException {
		this(url, GET);
	}

	public Conn(String url, String method) throws IOException {
		this.url = new URL(url);
		if (this.url.getProtocol().equalsIgnoreCase(HTTP)) {
			conn = (HttpURLConnection) this.url.openConnection();
		} else if (this.url.getProtocol().equalsIgnoreCase(HTTPS)) {
			conn = (HttpsURLConnection) this.url.openConnection();
		}
		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setReadTimeout(15000);
		conn.setConnectTimeout(15000);
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
		addHeader(CONTENT_TYPE, APPLICATION_JSON);
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
				conn.setRequestProperty(header.getName(), header.getValue());
			}
			if (conn.getRequestMethod().equalsIgnoreCase(POST)) {
				conn.setDoOutput(true);
				for (NameValuePair param : paramList) {
					conn.getOutputStream().write(param.getName().getBytes("UTF-8"));
					conn.getOutputStream().write("=".getBytes("UTF-8"));
					conn.getOutputStream().write(param.getValue().getBytes("UTF-8"));
					conn.getOutputStream().write("&".getBytes("UTF-8"));
				}
				if (content!=null){
					conn.getOutputStream().write(content.getBytes("UTF-8"));
				}
				conn.getOutputStream().flush();
				conn.getOutputStream().close();
			}
			conn.connect();
			result.setCode(conn.getResponseCode());
			InputStream is = null;
			if (result.isOK() || result.isCreated() || result.isNoContent()) {
				is = conn.getInputStream();
			} else {
				is = conn.getErrorStream();
			}
			if (is != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String resultString = new String();
				String lines;
				while ((lines = reader.readLine()) != null) {
					System.out.println(lines);
					resultString += lines;
				}
				result.setEntityString(resultString);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static File download(String fileURL, String saveDir, String key, OnDownloadListener listener) throws IOException {
		if (URLUtil.isValidUrl(fileURL)) {
			URL url = new URL(fileURL);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			int responseCode = httpConn.getResponseCode();
			// always check HTTP response code first
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				int contentLength = httpConn.getContentLength();
				if (listener != null) {
					listener.onStart(contentLength);
				}
				if (disposition != null) {
					// extracts file name from header field
					int index = disposition.indexOf("filename=");
					if (index > 0) {
						fileName = disposition.substring(index + 10, disposition.length() - 1);
					}
				} else {
					// extracts file name from URL
					fileName = (key != null ? key + "_" : new String()) + fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
					fileName = new Date().getTime() + ".3gp";
				}

				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("Content-Length = " + contentLength);
				System.out.println("fileName = " + fileName);

				// opens input stream from the HTTP connection
				InputStream inputStream = httpConn.getInputStream();
				String saveFilePath = saveDir + File.separator + fileName;
				File file = new File(saveFilePath);
				// opens an output stream to save into file
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream outputStream = new FileOutputStream(saveFilePath);
				int bytesRead = -1;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
					if (listener != null) {
						listener.onProgress(contentLength, contentLength);
					}
				}

				outputStream.close();
				inputStream.close();
				if (listener != null) {
					listener.onFinish(file.exists() && file.isFile() && file.length() > 0, file);
				}
				System.out.println("File downloaded");
				httpConn.disconnect();
				return file;
			} else {
				System.out.println("No file to download. Server replied HTTP code: " + responseCode);
				httpConn.disconnect();
				return null;
			}
		}
		return null;
	}

}
