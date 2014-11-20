package angel.zhuoxiu.library.util;

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

	public static void show(Context context, View view) {
		showOrHide(context, view, true);
	}

	public static void hide(Context context, View view) {
		showOrHide(context, view, false);
	}
}
