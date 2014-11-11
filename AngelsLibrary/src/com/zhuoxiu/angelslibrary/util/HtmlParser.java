package com.zhuoxiu.angelslibrary.util;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.util.Log;

public class HtmlParser {
	static final String TAG = HtmlParser.class.getSimpleName();

	public static final ImageGetter imageGetter = new ImageGetter() {
		@Override
		public Drawable getDrawable(String source) {
			Log.i(TAG,"source="+source);

			return null;
		}
	};

	public static Spanned parse(String html) {
		return Html.fromHtml(html, imageGetter, null);
	}
}
