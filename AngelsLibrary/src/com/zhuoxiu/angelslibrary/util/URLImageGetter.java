package com.zhuoxiu.angelslibrary.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.view.Display;
import android.widget.TextView;
import angels.zhuoxiu.library.R;

public class URLImageGetter implements ImageGetter {
	TextView textView;
	Context context;
	static Map<String, URLDrawable> drawableMap = new HashMap<String, URLDrawable>();

	public URLImageGetter(Context contxt, TextView textView) {
		this.context = contxt;
		this.textView = textView;
	}

	@Override
	public Drawable getDrawable(String source) {
		URLDrawable urlDrawable = null;
		if (drawableMap.containsKey(source)) {
			urlDrawable = drawableMap.get(source);
		} else {
			urlDrawable = new URLDrawable(context);
			drawableMap.put(source, urlDrawable);
		}
		ImageGetterAsyncTask getterTask = new ImageGetterAsyncTask(urlDrawable);
		getterTask.execute(source);
		return urlDrawable;
	}

	public static void clear() {
		drawableMap.clear();
	}

	public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {
		URLDrawable urlDrawable;

		public ImageGetterAsyncTask(URLDrawable drawable) {
			this.urlDrawable = drawable;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null) {
				urlDrawable.drawable = result;

				URLImageGetter.this.textView.requestLayout();
			}
		}

		@Override
		protected Drawable doInBackground(String... params) {
			String source = params[0];
			return fetchDrawable(source);
		}

		public Drawable fetchDrawable(String url) {
			Drawable drawable = null;
			URL Url;
			try {
				Url = new URL(url);
				drawable = Drawable.createFromStream(Url.openStream(), "");
			} catch (Exception e) {
				return null;
			}

			// 按比例缩放图片
			Rect bounds = getDefaultImageBounds(context);
			int newwidth = bounds.width();
			int newheight = bounds.height();
			double factor = 1;

			double fx = (double) drawable.getIntrinsicWidth() / (double) newwidth;
			double fy = (double) drawable.getIntrinsicHeight() / (double) newheight;
			factor = fx > fy ? fx : fy;
			if (factor < 1)
				factor = 1;
			newwidth = (int) (drawable.getIntrinsicWidth() / factor);
			newheight = (int) (drawable.getIntrinsicHeight() / factor);

			drawable.setBounds(0, 0, newwidth, newheight);

			return drawable;
		}
	}

	// 预定图片宽高比例为 4:3
	@SuppressWarnings("deprecation")
	public Rect getDefaultImageBounds(Context context) {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = (int) (width * 3 / 4);

		Rect bounds = new Rect(0, 0, width, height);
		return bounds;
	}

}

class URLDrawable extends BitmapDrawable {
	protected Drawable drawable;

	@SuppressWarnings("deprecation")
	public URLDrawable(Context context) {
		this.setBounds(getDefaultImageBounds(context));
		drawable = context.getResources().getDrawable(R.drawable.ic_launcher);
		drawable.setBounds(getDefaultImageBounds(context));
		drawable = null;
	}

	@Override
	public void draw(Canvas canvas) {
		if (drawable != null) {
			drawable.draw(canvas);
		}
	}

	@SuppressWarnings("deprecation")
	public Rect getDefaultImageBounds(Context context) {
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = (int) (width * 3 / 4);

		Rect bounds = new Rect(0, 0, width, height);
		return bounds;
	}

}