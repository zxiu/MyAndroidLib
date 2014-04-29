package com.zhuoxiu.angelslibrary.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
import cn.trinea.android.common.util.ArrayUtils;

import com.zhuoxiu.angelslibrary.R;
import com.zhuoxiu.angelslibrary.cache.PanoUtil;

/**
 * View for photo on Conversation, FriendList. Circle Transparent Frame
 * 
 * @author Zhuo Xiu
 * 
 */
public class PhotoView extends ImageView {
	static ImageCache imageCache = new ImageCache();

	String TAG = this.getClass().getSimpleName();
	public int scale = 10;
	public int ratio = (int) (Math.random() * (scale + 1));

	public float strokeRatio = 0.1f;

	private boolean circleEnable = false;
	private int circleColor = Color.RED;
	private float circleAlpha = 0.6f;

	static Map<String, Bitmap> originalBitmaps = new HashMap<String, Bitmap>();
	static Map<String, Bitmap> roundBitmaps = new HashMap<String, Bitmap>();

	private List<String> urlList = new CopyOnWriteArrayList<String>();
	private List<String> invalidUrlList = new CopyOnWriteArrayList<String>();

	LoadImageTask loadImageTask;

	int messageCount;
	int indexOffset = 0;
	PanoUtil panoUtil;
	Bitmap defaultPhoto;
	Paint paint;
	Object tag;

	String[] fakeUrl = new String[] { "http://icons.iconarchive.com/icons/hopstarter/sleek-xp-software/256/Yahoo-Messenger-icon.png",
			"http://1.bp.blogspot.com/-ae-aXaKue70/UPbRltB8dTI/AAAAAAAAbgQ/pUroMZ5haUg/s1600/fotos-fake-morenas-06.jpg",
			"http://www.lesliensinvisibles.org/wp-content/uploads/2009/10/20/fake.jpg", "http://blog.cachinko.com/blog/wp-content/uploads/2012/07/fake.png",
			"http://willvideoforfood.com/wp-content/uploads/2011/09/FakeBoobs1.jpg" };

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
		paint = new Paint();
		paint.setAntiAlias(true);

		panoUtil = new PanoUtil(getContext());
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setDrawingCacheEnabled(true);
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				indexOffset += 1;
				invalidate();
			}
		});
		if (defaultPhoto == null) {
			defaultPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.ic_friend_gray);
		}
		setWillNotDraw(false);
	}

	public void setPhotoUrlList(List<String> urlList) {
		for (String url:urlList){
			Log.e(TAG,url+"");
		}
		this.urlList.clear();
		loadImageTask = new LoadImageTask();
		Log.e(TAG, urlList.toArray(new String[urlList.size()]).toString());
		loadImageTask.execute(urlList.toArray(new String[urlList.size()]));

	}


	public void setPhotoUrl(String url) {
	
		Log.d(TAG, "url="+url);
		this.tag = getTag();
		loadImageTask = new LoadImageTask();
		loadImageTask.execute(url);

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
		int length = getWidth();
		if (length == 0) {
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
			this.measure(w, h);
			int height = this.getMeasuredHeight();
			int width = this.getMeasuredWidth();
			length = width;
		}
		if (length != 0) {
			if (bitmap.getWidth() > bitmap.getHeight()) {
				bitmap = Bitmap.createBitmap(bitmap, (bitmap.getWidth() - bitmap.getHeight()) / 2, 0, bitmap.getHeight(), bitmap.getHeight());
			} else {
				bitmap = Bitmap.createBitmap(bitmap, 0, (bitmap.getHeight() - bitmap.getWidth()) / 2, bitmap.getWidth(), bitmap.getWidth());
			}
			Bitmap newBitmap;
			newBitmap = Bitmap.createScaledBitmap(bitmap, length, length, false);
			if (length != 0) {

			}
			return newBitmap;
		} else {
			return bitmap;
		}
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

	protected void prepareBitmapToPaint(List<String> urlList) {
		StringBuilder keyBuilder = new StringBuilder();
		for (int i = 0; i < Math.min(3, urlList.size()); i++) {
			keyBuilder.append(createKey(urlList.get((i + indexOffset) % urlList.size()))).append("_");
		}
		String key = keyBuilder.toString();
		Log.e(TAG,"urlList.size() "+urlList.size()); 
		Bitmap bitmapToPaint = roundBitmaps.get(key);
		if (bitmapToPaint == null) {
			Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
			Canvas newCanvas = new Canvas(bitmap);
			if (urlList.size() == 0){
				drawOnPosition(newCanvas, defaultPhoto, POS_FULL);
			}
			
			if (urlList.size() == 1) {
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get(0))), POS_FULL);
			}
			if (urlList.size() == 2) {
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT);
			}
			if (urlList.size() >= 3) {
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT_UP);
				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((2 + indexOffset) % urlList.size()))), POS_RIGHT_DOWN);
			}
			newCanvas.save(Canvas.ALL_SAVE_FLAG);
			newCanvas.restore();
			bitmapToPaint = bitmap.copy(Config.ARGB_8888, true);
			int width = bitmapToPaint.getWidth();
			int height = bitmapToPaint.getHeight();
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					if (Math.sqrt(Math.pow((x - width / 2), 2) + Math.pow((y - height / 2), 2)) > (width + height) / 4) {
						bitmapToPaint.setPixel(x, y, Color.TRANSPARENT);
					}
				}
			}
			roundBitmaps.put(key, bitmapToPaint);
		}
		setImageBitmap(bitmapToPaint);
	}

//	@SuppressLint("DrawAllocation")
//	protected void onDraw1(Canvas canvas) {
//		Log.d(TAG, "onDraw");
//		StringBuilder keyBuilder = new StringBuilder();
//		for (int i = 0; i < Math.min(3, urlList.size()); i++) {
//			keyBuilder.append(createKey(urlList.get((i + indexOffset) % urlList.size()))).append("_");
//		}
//		String key = keyBuilder.toString();
//		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//		Canvas newCanvas = new Canvas(bitmap);
//		if (urlList.size() == 0) {
//			canvas.drawBitmap(defaultPhoto, new Rect(0, 0, defaultPhoto.getWidth(), defaultPhoto.getHeight()),
//					new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
//			return;
//		}
//
//		Bitmap bitmapToPaint = roundBitmaps.get(key);
//		// bitmapTopaint=defaultPhoto;
//		Log.d(TAG, "bitmapTopaint = " + bitmapToPaint);
//		if (bitmapToPaint == null) {
//			if (urlList.size() == 1) {
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get(0))), POS_FULL);
//			}
//			if (urlList.size() == 2) {
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT);
//			}
//			if (urlList.size() >= 3) {
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((0 + indexOffset) % urlList.size()))), POS_LEFT);
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((1 + indexOffset) % urlList.size()))), POS_RIGHT_UP);
//				drawOnPosition(newCanvas, originalBitmaps.get(createKey(urlList.get((2 + indexOffset) % urlList.size()))), POS_RIGHT_DOWN);
//			}
//			newCanvas.save(Canvas.ALL_SAVE_FLAG);
//			newCanvas.restore();
//			bitmapToPaint = bitmap.copy(Config.ARGB_8888, true);
//			int width = bitmapToPaint.getWidth();
//			int height = bitmapToPaint.getHeight();
//			for (int x = 0; x < width; x++) {
//				for (int y = 0; y < height; y++) {
//					if (Math.sqrt(Math.pow((x - width / 2), 2) + Math.pow((y - height / 2), 2)) > (width + height) / 4) {
//						bitmapToPaint.setPixel(x, y, Color.TRANSPARENT);
//					}
//				}
//			}
//			roundBitmaps.put(key, bitmapToPaint);
//		}
//
//		if (bitmapToPaint != null) {
//			if (key.equals("https%3A%2F%2Fc7dev%2Es3%2Eamazonaws%2Ecom%2Fuploads%2Faccount%5Fgallery%5Fimage%2Fpicture%2F143644%2Fthumb%5Falex%5F2012%5Fsmall%2Epng%3Fv%3D1395996613_100_100_")) {
//				Log.e(TAG, "bingo! " + bitmapToPaint.getWidth() + " " + bitmapToPaint.getHeight());
//				// bitmapTopaint=defaultPhoto;
//			}
//
//			canvas.drawBitmap(bitmapToPaint, new Rect(0, 0, bitmapToPaint.getWidth(), bitmapToPaint.getHeight()),
//					new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), paint);
//			if (messageCount > 0) {
//				paint.setColor(Color.RED);
//				canvas.drawRoundRect(new RectF((int) (canvas.getWidth() * 11.0 / 16), 0, canvas.getWidth(), canvas.getWidth() / 8 * 3), 10, 10, paint);
//				paint.setColor(Color.WHITE);
//				paint.setStyle(Style.FILL);
//				if (messageCount <= 9) {
//					paint.setTextSize(canvas.getWidth() / 4);
//					String number = Integer.toString(messageCount);
//					canvas.drawText(number, canvas.getWidth() / 4 * 3 + 2, canvas.getWidth() / 4 + 2, paint);
//				} else {
//					String number = "9+";
//					paint.setTextSize(canvas.getWidth() / 5);
//					canvas.drawText(number, canvas.getWidth() / 4 * 3, canvas.getWidth() / 4 + 2, paint);
//				}
//			}
//		}
//	}

	private void drawOnPosition(Canvas canvas, Bitmap bitmap, int position) {
		if (bitmap == null) {
			return;
		}

		int cWidth = canvas.getWidth();
		int cHeight = canvas.getHeight();

		int bWidth = bitmap.getWidth();
		int bHeight = bitmap.getHeight();
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

	String createKey(String url) {
		String key = panoUtil.createKey(url);
		if (getWidth() == 0) {
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.AT_MOST);
			this.measure(w, h);
			int height = this.getMeasuredHeight();
			int width = this.getMeasuredWidth();
			key += "_" + width + "_" + height;
		} else {
			key += "_" + getWidth() + "_" + getHeight();
		}
		return key;
	}

	class LoadImageTask extends AsyncTask<String, Void, List<String>> {
		Object tag;
		List<String> urlNewList = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
			tag = getTag();
		}

		protected List<String> doInBackground(String... urls) {
			for (String url : urls) {
				if (URLUtil.isValidUrl(url)) {
					Bitmap bitmap = null;
					try {
						bitmap = originalBitmaps.get(createKey(url));
						Log.e(TAG, "bitmap =" + bitmap);
						if (bitmap == null && !invalidUrlList.contains(url)) {
							if (tag != getTag()) {
								return null;
							}
							bitmap = panoUtil.getBitmap(url);
							if (bitmap == null) {
								invalidUrlList.add(url);
							} else {
								originalBitmaps.put(createKey(url), initBitmap(bitmap));
							}
						}
						if (bitmap != null) {
							urlNewList.add(url);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			urlList.clear();
			urlList.addAll(urlNewList);
			//postInvalidate();
			return urlList;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			super.onPostExecute(result);
			if (result!=null){
				prepareBitmapToPaint(result);
			}
		}
	}
}
