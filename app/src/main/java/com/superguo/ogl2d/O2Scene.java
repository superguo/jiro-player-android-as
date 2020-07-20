package com.superguo.ogl2d;

import javax.microedition.khronos.opengles.GL10;

import android.view.*;

public abstract class O2Scene {
	public static final int MAX_EVENT = 128;

	protected O2Director mDirector;

//	private MotionEvent mMotionEventQ[];
//	private int mMotionEventHead;
//	private int mMotionEventTail;

	protected O2Scene(O2Director director) {
		mDirector = director;
//		mMotionEventQ = new MotionEvent[MAX_EVENT + 1];
//		mMotionEventHead = mMotionEventTail = 0;
	}

//	protected final MotionEvent getMotionEvent() {
//		// using a condition is faster than a virtual function
//		// using synchronized is faster than wait()+notify()
//		if (O2Director.isSingleProcessor) {
//			return getMotionEventUnsafe();
//		} else {
//			synchronized (mMotionEventQ) {
//				return getMotionEventUnsafe();
//			}
//		}
//	}
//
//	private final MotionEvent getMotionEventUnsafe() {
//		if (mMotionEventHead == mMotionEventTail) {
//			return null;
//		}
//		MotionEvent val = mMotionEventQ[mMotionEventHead];
//		if (mMotionEventHead == MAX_EVENT) {
//			mMotionEventHead = 0;
//		} else {
//			mMotionEventHead++;
//		}
//		return val;
//	}
//
//	final void addMotionEventUnsafe(MotionEvent e) {
//		if (mMotionEventHead == mMotionEventTail + 1
//				|| mMotionEventTail == MAX_EVENT && mMotionEventHead == 0)
//			// full!! we do nothing, though
//			return;
//
//		mMotionEventQ[mMotionEventTail] = MotionEvent.obtain(e);
//		if (mMotionEventTail == MAX_EVENT)
//			mMotionEventTail = 0;
//		else
//			mMotionEventTail++;
//	}

	public abstract void onEnteringScene();

	public abstract void onLeavingScene();

	public abstract void onPause();

	public abstract void onResume();

	public void onSizeChanged() {
	}

	public void preDraw(GL10 gl) {
	}

	public void postDraw(GL10 gl) {
	}

	public abstract void dispose();

	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
}
