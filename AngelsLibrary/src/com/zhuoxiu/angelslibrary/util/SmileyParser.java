package com.zhuoxiu.angelslibrary.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParserException;

import com.zhuoxiu.angelslibrary.bean.Smile;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

public class SmileyParser {
	private Context mContext;
	private Pattern mPattern;
	private HashMap<String, Smile> mSmileyToRes;

	static List<Smile> smileList;

	public SmileyParser(Context context) throws XmlPullParserException, IOException {
		if (smileList == null) {
			smileList = Smile.getSmileList(context);
		}
		mContext = context;
		mSmileyToRes = new HashMap<String, Smile>();
		mPattern = buildPattern();

		for (Smile smile : smileList) {
			mSmileyToRes.put(smile.getText(), smile);
		}
	}

	private Pattern buildPattern() {
		StringBuilder patternString = new StringBuilder(smileList.size() * 3);
		patternString.append('(');
		for (Smile smile : smileList) {
			String s = smile.getText();
			patternString.append(Pattern.quote(s));
			patternString.append('|');
		}
		patternString.replace(patternString.length() - 1, patternString.length(), ")");

		return Pattern.compile(patternString.toString());
	}

	public CharSequence replace(CharSequence text) {
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		Matcher matcher = mPattern.matcher(text);
		while (matcher.find()) {
			builder.setSpan(new ImageSpan(mContext, mSmileyToRes.get(matcher.group()).getBitmap()), matcher.start(), matcher.end(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
}
