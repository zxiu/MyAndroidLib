package angel.zhuoxiu.library.cache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class PanoMemCache {

	// LinkedHashMap initial capacity
	private static final int INITIAL_CAPACITY = 16;
	// LinkedHashMap load factor
	private static final float LOAD_FACTOR = 0.75f;
	// LinkedHashMap access order
	private static final boolean ACCESS_ORDER = true;

	// Cache of soft reference
	private static LinkedHashMap<String, SoftReference<Bitmap>> mSoftCache;
	// Cache of hard reference
	private static LruCache<String, Bitmap> mLruCache;

	public PanoMemCache() {
		final int memClass = (int) Runtime.getRuntime().maxMemory();
		final int cacheSize = memClass / 4;
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				if (value != null) {
					return value.getRowBytes() * value.getHeight();
				} else {
					return 0;
				}
			}

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
				if (oldValue != null) {
					mSoftCache.put(key, new SoftReference<Bitmap>(oldValue));
				}
			}
		};

		mSoftCache = new LinkedHashMap<String, SoftReference<Bitmap>>(INITIAL_CAPACITY, LOAD_FACTOR, ACCESS_ORDER) {
			private static final long serialVersionUID = 7237325113220820312L;

			@Override
			protected boolean removeEldestEntry(Entry<String, SoftReference<Bitmap>> eldest) {
				if (size() > INITIAL_CAPACITY) {
					return true;
				}
				return false;
			}
		};
	}

	public boolean isCached(String key) {
		synchronized (mLruCache) {
			return mLruCache.get(key) != null;
		}
	}

	/** 
	 * @param key 
	 * @return bitmap 
	 */
	public Bitmap getBitmapFromMem(String key) {
		Bitmap bitmap = null;
		synchronized (mLruCache) {
			bitmap = mLruCache.get(key);
			if (bitmap != null) {
				mLruCache.remove(key);
				mLruCache.put(key, bitmap);
				return bitmap;
			}
		}

		synchronized (mSoftCache) {
			SoftReference<Bitmap> bitmapReference = mSoftCache.get(key);
			if (bitmapReference != null) {
				bitmap = bitmapReference.get();
				if (bitmap != null) {
					mLruCache.put(key, bitmap);
					mSoftCache.remove(key);
					return bitmap;
				} else {
					mSoftCache.remove(key);
				}
			}
		}
		return null;
	}

	/** 
	 * @param key 
	 * @param bitmap 
	 */
	public void addBitmapToCache(String key, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (mLruCache) {
				mLruCache.put(key, bitmap);
			}
		}
	}

	/** 
	 */
	public void clearCache() {
		mSoftCache.clear();
		mSoftCache = null;
	}
}
