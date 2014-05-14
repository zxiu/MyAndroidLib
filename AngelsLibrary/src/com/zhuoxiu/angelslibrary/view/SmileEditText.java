package com.zhuoxiu.angelslibrary.view;

import java.util.List;

import util.ZipHelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.EditText;

public class SmileEditText extends EditText {
	List<Smile> smileList;

	public SmileEditText(Context context) {
		super(context);
	}

	public SmileEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setSmileZipFile(String path) {
		ZipHelper.getEntryList(path);
	}

	class Smile {
		Drawable drawable;
		String text;
		String name;
	}
}
