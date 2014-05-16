package com.zhuoxiu.angelslibrary.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {
	public static void showOrHide(Context context, View view, boolean show) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (show) {
			imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
		} else {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

}
