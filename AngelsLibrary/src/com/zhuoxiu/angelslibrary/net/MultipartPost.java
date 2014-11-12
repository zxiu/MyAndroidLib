package com.zhuoxiu.angelslibrary.net;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;

import android.util.Log;
import android.util.Pair;

/**
 * This utility class provides an abstraction layer for sending multipart HTTP
 * POST requests to a web server.
 * @author www.codejava.net
 *
 */
public class MultipartPost implements Constant {
	private final String boundary;
	private static final String LINE_FEED = "\r\n";
	private HttpURLConnection httpConn;
	private String charset;
	private OutputStream outputStream;
	private PrintWriter writer;
	List<Pair<String, String>> headerPairList = new ArrayList<Pair<String, String>>();
	List<Pair<String, String>> formFieldList = new ArrayList<Pair<String, String>>();
	List<Pair<String, File>> filePartList = new ArrayList<Pair<String, File>>();

	/**
	 * This constructor initializes a new HTTP POST request with content type
	 * is set to multipart/form-data
	 * @param requestURL
	 * @param charset
	 * @throws IOException
	 */
	public MultipartPost(String requestURL, String charset) throws IOException {
		this.charset = charset;

		// creates a unique boundary based on time stamp
		boundary = "===" + System.currentTimeMillis() + "===";

		URL url = new URL(requestURL);
		if (url.getProtocol().equalsIgnoreCase(HTTP)) {
			httpConn = (HttpURLConnection) url.openConnection();
		} else if (url.getProtocol().equalsIgnoreCase(HTTPS)) {
			httpConn = (HttpsURLConnection) url.openConnection();
		}
		httpConn.setUseCaches(false);
		httpConn.setDoOutput(true); // indicates POST method
		httpConn.setDoInput(true);
		httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
		httpConn.setRequestProperty("User-Agent", "Zhuo Xiu's Android Library - Angelslibrary");
	}

	public void addHeader(String name, String value) {
		headerPairList.add(Pair.create(name, value));
	}

	public void setAuthBearerToken(String token) {
		addHeader(AUTHORIZATION, BEARER + " " + token);
	}

	/**
	 * Adds a form field to the request
	 * @param name field name
	 * @param value field value
	 */
	public void addFormField(String name, String value) {
		formFieldList.add(Pair.create(name, value));
	}

	/**
	 * Adds a upload file section to the request
	 * @param fieldName name attribute in <input type="file" name="..." />
	 * @param uploadFile a File to be uploaded
	 * @throws IOException
	 */
	public void addFilePart(String fieldName, File uploadFile) throws IOException {
		String fileName = uploadFile.getName();
		filePartList.add(Pair.create(fieldName, uploadFile));

	}

	/**
	 * Adds a header field to the request.
	 * @param name - name of the header field
	 * @param value - value of the header field
	 */
	public void addHeaderField(String name, String value) {
		writer.append(name + ": " + value).append(LINE_FEED);
		writer.flush();
	}

	/**
	 * Completes the request and receive the response from server.
	 * @return A Object of {@link HttpResult} as response
	 * @throws IOException 
	 */
	public HttpResult execute() throws IOException {
		for (Pair<String, String> header : headerPairList) {
			httpConn.setRequestProperty(header.first, header.second);
		}
		outputStream = httpConn.getOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);
		for (Pair<String, String> formField : formFieldList) {
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"" + formField.first + "\"").append(LINE_FEED);
			writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
			writer.append(LINE_FEED);
			writer.append(formField.second).append(LINE_FEED);
			writer.flush();
		}
		for (Pair<String, File> filePart : filePartList) {
			writer.append("--" + boundary).append(LINE_FEED);
			writer.append("Content-Disposition: form-data; name=\"" + filePart.first + "\"; filename=\"" + filePart.second.getName() + "\"").append(LINE_FEED);
			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(filePart.second.getName())).append(LINE_FEED);
			writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
			writer.append(LINE_FEED);
			writer.flush();

			FileInputStream inputStream = new FileInputStream(filePart.second);
			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();
			inputStream.close();
			writer.append(LINE_FEED);
			writer.flush();
		}

		writer.append(LINE_FEED).flush();
		writer.append("--" + boundary + "--").append(LINE_FEED);
		writer.close();
		// httpConn.connect();
		HttpResult result = new HttpResult().setCode(httpConn.getResponseCode());
		InputStream is = null;
		if (result.isOK() || result.isCreated() || result.isNoContent()) {
			is = httpConn.getInputStream();
		} else {
			is = httpConn.getErrorStream();
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
		return result;
	}
}