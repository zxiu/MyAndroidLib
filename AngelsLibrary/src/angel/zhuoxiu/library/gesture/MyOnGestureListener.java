package angel.zhuoxiu.library.gesture;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class MyOnGestureListener extends SimpleOnGestureListener {
	String tag = this.getClass().getSimpleName();

	public static final int NONE = 0;
	public static final int LEFT = 1;
	public static final int UP = 2;
	public static final int UP_LEFT = 21, UP_RIGHT = 23;
	public static final int RIGHT = 3;
	public static final int DOWN = 4;
	public static final int DOWN_LEFT = 41, DOWN_RIGHT = 43;
	int threshold = 500;
	double minTan = Math.tan(Math.toRadians(20)), maxTan = Math.tan(Math.toRadians(70));

	public interface GestureActionListener {
		public void onGesture(int gesture);
	}

	GestureActionListener listener;

	public MyOnGestureListener(GestureActionListener listener) {
		this.listener = listener;
	}

	float abs(float value) {
		return Math.abs(value);
	}

	double distance(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		int action = NONE;
		if (distance(vx, vy) > threshold) {
			if (minTan < abs(vy) / abs(vx) && abs(vy) / abs(vx) < maxTan) {
				action = vy < 0 ? (vx < 0 ? UP_LEFT : UP_RIGHT) : (vx < 0 ? DOWN_LEFT : DOWN_RIGHT);
			} else if (abs(vy) / abs(vx) < minTan) {
				action = vx < 0 ? LEFT : RIGHT;
			} else if (maxTan < abs(vy) / abs(vx)) {
				action = vy < 0 ? UP : DOWN;
			}
		}
		if (listener != null) {
			listener.onGesture(action);
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}
}
