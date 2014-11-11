package com.zhuoxiu.angelslibrary.util;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

public class HtmlParser {
	static final String TAG = HtmlParser.class.getSimpleName();

	public static Spanned parse(Context context, TextView tv, String html) {
		return Html.fromHtml(html, new URLImageGetter(context, tv), null);
	}
}
