package com.zhuoxiu.angelslibrary.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.zhuoxiu.angelslibrary.bean.Smile;

public class SmileEditText extends EditText {
	List<Smile> smileList = new ArrayList<Smile>();

	public SmileEditText(Context context) {
		super(context);
	}

	public SmileEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setSmileList(List<Smile> smileList) {
		this.smileList = smileList;
	}
	
	 

}
