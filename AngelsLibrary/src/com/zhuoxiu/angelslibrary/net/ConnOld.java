package com.zhuoxiu.angelslibrary.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

import com.zhuoxiu.angelslibrary.exception.SessionExpiredException;

public class ConnOld {
	public static final String CHARSET = HTTP.UTF_8;
	public static final String GET = "GET";
	public static final String POST = "POST";
	public static final String PUT = "put";
	public static final String DELETE = "delete";

	public static final String tag = ConnOld.class.getSimpleName();

	private static HttpClient customerHttpClient;
	private List<HttpParams> paramList = new ArrayList<HttpParams>();

	private List<NameValuePair> textBodyList = new ArrayList<NameValuePair>();
	private List<File> fileList = new ArrayList<File>();

	private HttpClient client;
	private HttpUriRequest request;
	private HttpParams params = new BasicHttpParams();
	private List<Header> headerList = new ArrayList<Header>();
	private HttpResult result = new HttpResult();
	private HttpEntity entity;
	private HttpResponse response;

	public long finishedSize;
	public long totalSize;

	String method;
	String boundary = "SwA" + System.currentTimeMillis() + "SwA";
	String url;

	public ConnOld(String url, String method) {
		this.url = url;
		this.method = method;
		this.client = getHttpClient();
		if (method.equalsIgnoreCase(GET)) {
			request = new HttpGet(url);
		} else if (method.equalsIgnoreCase(POST)) {
			request = new HttpPost(url);
		} else if (method.equalsIgnoreCase(DELETE)) {
			request = new HttpDelete(url);
		}
		request.setParams(params);
		// if (Cube7App.getUser() != null) {
		// addHeader(AUTHORIZATION, Cube7App.getUser().getEncodeToken());
		// Log.i(tag, "url=" + url);
		// Log.i(tag, "token=" + Cube7App.getUser().getEncodeToken());
		// }
	}

	public ConnOld(String url, JSONObject jsonObject) {
		this(url, POST);
		addHeader("Content-Type", "application/json");
		addHeader("Content-Type", "application/x-www-form-urlencoded");
		try {
			entity = new StringEntity(jsonObject.toString(), CHARSET);
			((HttpPost) request).setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void setEntity(String entity) {
		try {
			this.entity = new StringEntity(entity);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public HttpResult execute() throws SessionExpiredException {
		for (Header header : headerList) {
			request.addHeader(header);
			Log.i(tag, header.getName() + " : " + header.getValue());
		}
		for (HttpParams params : paramList) {
			request.setParams(params);
		}
		if ((textBodyList.size() > 0 || fileList.size() > 0) && POST.equalsIgnoreCase(method)) {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			for (int i = 0; i < textBodyList.size(); i++) {
				builder.addTextBody(textBodyList.get(i).getName(), textBodyList.get(i).getValue(),
						ContentType.TEXT_PLAIN);
			}
			for (int i = 0; i < fileList.size(); i++) {
				builder.addPart("[message][message_attachments_attributes][" + i + "][file]",
						new FileBody(fileList.get(i)));
			}
			final HttpEntity entity = builder.build();
			ProgressiveEntity progressiveEntity = new ProgressiveEntity(entity);
			((HttpPost) request).setEntity(progressiveEntity);
		}
		try {
			Log.i(tag, "url = " + url);
			response = client.execute(request);
			result.setEntity(response.getEntity());
			result.setCode(response.getStatusLine().getStatusCode());
			Log.i(tag, "result=" + result);
			if (response.getStatusLine().getStatusCode() == 401 || response.getStatusLine().getStatusCode() == 500) {
				throw new SessionExpiredException();
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void setParam(String name, String value) {
		params.setParameter(name, value);
	}

	public void addHeader(String name, String value) {
		headerList.add(new BasicHeader(name, value));
	}

	public void addFile(File file) {
		if (file != null && file.isFile() && file.exists()) {
			fileList.add(file);
			totalSize += file.length();
		}
	}

	public void addTextBody(String name, String text) {
		textBodyList.add(new BasicNameValuePair(name, text));
	}

	/**
	 * Download file with Progress Change Listener.
	 * @param file The file to save.
	 * @param listener {@link OnDownloadListener} listener to handle download progress.
	 * @return If download is successful.
	 */
	public boolean downloadFileWithProgress(File file, OnDownloadListener listener) {
		try {
			HttpResponse response = client.execute(request);

			if (response != null && response.getStatusLine().getStatusCode() == 200) {
				OutputStream os = new FileOutputStream(file);
				totalSize = response.getEntity().getContentLength();
				Log.d(tag, "totalSize=" + totalSize);
				if (listener != null) {
					listener.onDownloadBegin(totalSize);
				}

				InputStream is = response.getEntity().getContent();
				byte[] buf = new byte[4096];
				int read;
				finishedSize = 0;
				while ((read = is.read(buf)) != -1) {
					os.write(buf, 0, read);
					finishedSize += read;
					if (listener != null) {
						listener.onProgressChange(finishedSize);
					}
				}
				os.close();
				if (listener != null) {
					listener.onDownloadFinish(true);
				}
				return true;
			}
		} catch (ClientProtocolException e) {
			listener.onDownloadFinish(false);
			Log.e(tag, "ClientProtocolException : " + e.getMessage());
		} catch (IOException e) {
			listener.onDownloadFinish(false);
			Log.e(tag, "IOException : " + e.getMessage());
		}
		return false;
	}

	public static SSLSocketFactory getSSLSocketFactory() throws KeyStoreException, KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);

		SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", sf, 443));

		ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

		return sf;
	}

	public static synchronized HttpClient getHttpClient() {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			// Basic parameter setting
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0(Linux;U;Android 4.2.1;en-us;Nexus One Build.FRG83) "
					+ "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			// set timeout

			/* Timeout of getting connection from connection pool */
			ConnManagerParams.setTimeout(params, 1000);

			/* Timeout of connection */
			HttpConnectionParams.setConnectionTimeout(params, 5000);

			/* Timeout of waiting for data. */
			HttpConnectionParams.setSoTimeout(params, 5000);

			// Set Client support http and htttps
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			try {
				schReg.register(new Scheme("https", getSSLSocketFactory(), 443));
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Use thread safe connection manager to create HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customerHttpClient;
	}

	public interface OnDownloadListener {
		public void onDownloadBegin(long totalSize);

		public void onProgressChange(long finishedSize);

		public void onDownloadFinish(boolean isSuccessful);
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
					Log.i(tag, "bts=" + bts.length + " st=" + st + " end=" + end);
					// FIXME Put your progress bar stuff here!

					out.write(bts, st, end);
				}
			}

			yourEntity.writeTo(new ProgressiveOutputStream(outstream));
		}

	};

	public static class SSLSocketFactoryEx extends SSLSocketFactory {

		SSLContext sslContext = SSLContext.getInstance("TLS");

		public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
