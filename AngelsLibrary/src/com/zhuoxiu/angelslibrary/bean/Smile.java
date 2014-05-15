package com.zhuoxiu.angelslibrary.bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Smile {
	static final String TAG = Smile.class.getSimpleName();
	static final String FOLDER_NAME = "smile";
	static final String XML_FILE_NAME = "smile.xml";
	static final String TAG_SMILE = "smile";
	static final String ATTR_NAME = "name";
	static final String ATTR_TEXT = "text";
	static final String ATTR_FILE = "file";


	private String name;
	private String text;
	private String fileName;
	private Bitmap bitmap;

	@Override
	public String toString() {
		return "SmileBean [name=" + name + ", text=" + text + ", fileName=" + fileName + ", bitmap=" + bitmap + "]";
	}

	public static List<Smile> getSmileList(Context context) throws XmlPullParserException, IOException {
		List<Smile> smileList = new ArrayList<Smile>();
		AssetManager am = context.getAssets();
		String path = FOLDER_NAME + "/" + XML_FILE_NAME;
		XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
		xpp.setInput(am.open(path), "utf-8");
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				// System.out.println("Start document");
			} else if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equalsIgnoreCase(TAG_SMILE)) {
					Smile smile = new Smile();
					smile.setName(xpp.getAttributeValue(null, ATTR_NAME));
					smile.setText(xpp.getAttributeValue(null, ATTR_TEXT));
					smile.setFileName(xpp.getAttributeValue(null, ATTR_FILE));
					smile.setBitmap(BitmapFactory.decodeStream(am.open(FOLDER_NAME + "/" + smile.getFileName())));
					Log.i(TAG, smile.toString());
					smileList.add(smile);
				}
				// System.out.println("Start tag " + xpp.getName());
			} else if (eventType == XmlPullParser.END_TAG) {
				// System.out.println("End tag " + xpp.getName());
			} else if (eventType == XmlPullParser.TEXT) {
				// System.out.println("Text " + xpp.getText());
			}
			eventType = xpp.next();
		}
		return smileList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
