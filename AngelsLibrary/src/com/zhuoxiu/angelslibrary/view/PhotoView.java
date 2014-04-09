package com.zhuoxiu.angelslibrary.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import cn.trinea.android.common.service.impl.ImageCache;

import com.zhuoxiu.angelslibrary.R;
import com.zhuoxiu.angelslibrary.cache.PanoUtil;
import com.zhuoxiu.angelslibrary.cache.PhotoDBCache;
import com.zhuoxiu.angelslibrary.cache.PhotoDBCache.OnLoadFinishListener;

/**
 * View for photo on Conversation, FriendList. Circle Transparent Frame
 * 
 * @author Zhuo Xiu
 * 
 */
public class PhotoView extends ImageView {
	static ImageCache imageCache = new ImageCache();

	String tag = this.getClass().getSimpleName();
	public int scale = 10;
	public int ratio = (int) (Math.random() * (scale + 1));

	public float strokeRatio = 0.1f;

	private boolean circleEnable = false;
	private int circleColor = Color.RED;
	private float circleAlpha = 0.6f;

	public static Map<String, Bitmap> bitmaps = new HashMap<String, Bitmap>();
	private List<String> urlList = new ArrayList<String>();

	int messageCount;
	int indexOffset = 0;
	PanoUtil panoUtil;
	Bitmap defaultPhoto;

	String fakeUrl = "http://icons.iconarchive.com/icons/hopstarter/sleek-xp-software/256/Yahoo-Messenger-icon.png";

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PhotoView, 0, 0);
		circleColor = typedArray.getColor(R.styleable.PhotoView_circleColor, circleColor);
		circleAlpha = typedArray.getFloat(R.styleable.PhotoView_circleAlpha, circleAlpha);
		circleEnable = typedArray.getBoolean(R.styleable.PhotoView_circleEnable, circleEnable);
		initiate();
	}

	public PhotoView(Context context) {
		super(context);
		initiate();
	}

	public PhotoView(Context context, Bitmap defaultPhoto) {
		this(context);
		this.defaultPhoto = defaultPhoto;
	}

	void initiate() {
		panoUtil = new PanoUtil(getContext());
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setDrawingCacheEnabled(true);
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(tag, "onClick");
				indexOffset += 1;
				invalidate();
			}
		});
		if (defaultPhoto == null) {
			defaultPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_friend_gray);
		}
	}

	public void setPhotoUrlList(List<String> photoUrlList) {
		this.urlList.clear();
		for (String photoUrl : photoUrlList) {
			addPhotoUrl(photoUrl);
		}
	}

	public void addPhotoUrl(String photoUrl) {
		if (!URLUtil.isValidUrl(photoUrl)) {
			return;
		}
		//new LoadImageTask(photoUrl).execute();
		final PhotoDBCache photoCache = new PhotoDBCache(getContext(), fakeUrl, false, null);
		photoCache.load(new OnLoadFinishListener() {
			@Override
			public void onFinish(boolean success) {
				Log.i(tag, "photoCache success="+success+" getData="+photoCache.getData().length+" url=" + fakeUrl+" bitmap="+photoCache.getBitmap());
			}
		});

	}

	public void setPhotoUrl(String photoUrl) {
		this.urlList.clear();
		if (!URLUtil.isValidUrl(photoUrl)) {
			return;
		}
		Log.i(tag, "setPhotoUrl");
		addPhotoUrl(photoUrl);
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
		invalidate();
	}

	@SuppressLint("NewApi")
	private Bitmap initBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int length = this.getWidth();
		if (bitmap.getWidth() > bitmap.getHeight()) {
			bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - bitmap.getHeight()) / 2, 0, bitmap.getHeight(), bitmap.getHeight());
		} else {
			bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - bitmap.getWidth()) / 2, bitmap.getWidth(), bitmap.getWidth());
		}
		Bitmap newBitmap;
		newBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
		if (length != 0) {

		}
		return newBitmap;
	}

	private static final int POS_FULL = 0;
	private static final int POS_LEFT = 1;
	private static final int POS_RIGHT = 2;
	private static final int POS_LEFT_UP = 3;
	private static final int POS_LEFT_DOWN = 4;
	private static final int POS_RIGHT_UP = 5;
	private static final int POS_RIGHT_DOWN = 6;

	public void setDefaultPhoto(Bitmap defaultPhoto) {
		this.defaultPhoto = defaultPhoto;
	}

	@SuppressLint("DrawAllocation")
	protected void onDraw(Canvas canvas) {
		Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas newCanvas = new Canvas(bitmap);

		if (urlList.size() == 0) {
			drawOnPosition(newCanvas, defaultPhoto, POS_FULL);
		}
		if (urlList.size() == 1) {
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get(0))), POS_FULL);
		}
		if (urlList.size() == 2) {
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT);
		}
		if (urlList.size() >= 3) {
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT_UP);
			drawOnPosition(newCanvas, bitmaps.get(getKey(urlList.get((2 + indexOffset) % urlList.size()))), POS_RIGHT_DOWN);
		}
		newCanvas.save(Canvas.ALL_SAVE_FLAG);
		newCanvas.restore();
		bitmap = bitmap.copy(Config.ARGB_8888, true);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (Math.sqrt(Math.pow((x - width / 2), 2) + Math.pow((y - height / 2), 2)) > (width + height) / 4) {
					bitmap.setPixel(x, y, Color.TRANSPARENT);
				}
			}
		}

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(bitmap, 0, 0, paint);

		if (messageCount > 0) {
			paint.setColor(Color.RED);
			canvas.drawRoundRect(new RectF((int) (canvas.getWidth() * 11.0 / 16), 0, canvas.getWidth(), canvas.getWidth() / 8 * 3), 10, 10, paint);
			paint.setColor(Color.WHITE);
			paint.setStyle(Style.FILL);
			if (messageCount <= 9) {
				paint.setTextSize(canvas.getWidth() / 4);
				String number = Integer.toString(messageCount);
				canvas.drawText(number, canvas.getWidth() / 4 * 3 + 2, canvas.getWidth() / 4 + 2, paint);
			} else {
				String number = "9+";
				paint.setTextSize(canvas.getWidth() / 5);
				canvas.drawText(number, canvas.getWidth() / 4 * 3, canvas.getWidth() / 4 + 2, paint);
			}
		}

	}

	private void drawOnPosition(Canvas canvas, Bitmap bitmap, int position) {
		if (bitmap == null) {
			return;
		}

		int cWidth = canvas.getWidth();
		int cHeight = canvas.getHeight();

		int bWidth = bitmap.getWidth();
		int bHeight = bitmap.getHeight();

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		switch (position) {
		case POS_FULL:
			canvas.drawBitmap(bitmap, new Rect(0, 0, bWidth, bHeight), new Rect(0, 0, cWidth, cHeight), paint);
			break;
		case POS_LEFT:
			canvas.drawBitmap(bitmap, new Rect(bWidth / 4, 0, bWidth * 3 / 4, bHeight), new RectF(0, 0, cWidth / 2 - 1, cHeight), paint);
			break;
		case POS_RIGHT:
			canvas.drawBitmap(bitmap, new Rect(bWidth / 4, 0, bWidth * 3 / 4, bHeight), new RectF(cWidth / 2, 0, cWidth, cHeight), paint);
			break;
		case POS_LEFT_UP:
			canvas.drawBitmap(bitmap, new Rect(0, 0, bWidth, bHeight), new Rect(0, 0, cWidth / 2 - 1, cHeight / 2 - 1), paint);
			break;
		case POS_LEFT_DOWN:
			canvas.drawBitmap(bitmap, new Rect(0, 0, bWidth, bHeight), new Rect(0, cHeight / 2, cWidth / 2 - 1, cHeight), paint);
			break;

		case POS_RIGHT_UP:
			canvas.drawBitmap(bitmap, new Rect(0, 0, bWidth, bHeight), new Rect(cWidth / 2, 0, cWidth, cHeight / 2 - 1), paint);
			break;
		case POS_RIGHT_DOWN:
			canvas.drawBitmap(bitmap, new Rect(0, 0, bWidth, bHeight), new Rect(cWidth / 2, cHeight / 2, cWidth, cHeight), paint);
			break;
		}
	}

	private String getKey(String url) {
		return panoUtil.generateKey(url);
	}

	class LoadImageTask extends AsyncTask<Void, Integer, Bitmap> {
		String url;
		Bitmap bitmap;

		public LoadImageTask(String url) {
			Log.i(tag, "LoadImageTask " + url);
			this.url = url;
		}

		protected Bitmap doInBackground(Void... params) {
			try {
				bitmap = panoUtil.getBitmap(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null && !urlList.contains(url)) {
				urlList.add(url);
			}
			System.out.println("size=" + PhotoView.this.getWidth() + " " + PhotoView.this.getHeight() + " url=" + url +  " result=" + result);
			if (!bitmaps.containsKey(panoUtil.generateKey(url))) {
				bitmaps.put(panoUtil.generateKey(url), initBitmap(bitmap));
			}
			invalidate();
		}
	}
}
