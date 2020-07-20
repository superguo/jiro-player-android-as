package com.superguo.jiroplayer;

import com.superguo.ogl2d.O2Texture;
import com.superguo.ogl2d.O2TextureManager;

public class FPSHolder {
	private static FPSHolder sInstance;
	private int 	 		mFPSCount;
	private int 	 		mFPSDisplay;
	private long	 		mLastFPSRecTime;
	private O2Texture 		mFPSTex;
	private O2Texture 		mNumerTex[];

	private FPSHolder() {
		O2TextureManager mgr = O2TextureManager.getInstance();
		mFPSTex = mgr.createFromString("FPS: ", true);
		mNumerTex = new O2Texture[10];
		for (int i = 0; i < 10; ++i) {
			mNumerTex[i] = mgr.createFromString(Integer.toString(i), true);
		}
	}

	public static final FPSHolder getInstance() {
		if (sInstance == null)
			sInstance = new FPSHolder();
		return sInstance;
	}

	public void destroy() {
		if (mFPSTex != null) {
			mFPSTex.destroy();
			mFPSTex = null;
		}

		if (mNumerTex != null) {
			for (O2Texture tex : mNumerTex)
				tex.destroy();
			mNumerTex = null;
		}

		sInstance = null;
	}

	public void showFPS() {
		mFPSCount++;

		long FPSRecTime = android.os.SystemClock.uptimeMillis();
		if (FPSRecTime - mLastFPSRecTime > 1000) {
			mFPSDisplay = mFPSCount;
			mFPSCount = 0;
			mLastFPSRecTime = FPSRecTime;
		}

		if (mFPSDisplay > 0) {
			int x = 10;
			int y = 350;
			boolean drawn100 = false;
			mFPSTex.draw(x, y);
			x += mFPSTex.getWidth();

			int index = mFPSDisplay / 100;
			if (index > 9)
				index = 9;
			if (index > 0) {
				mNumerTex[index].draw(x, y);
				drawn100 = true;
				x += mNumerTex[index].getWidth();
			}

			index = (mFPSDisplay % 100) / 10;
			if (drawn100 || index > 0) {
				mNumerTex[index].draw(x, y);
				x += mNumerTex[index].getWidth();
			}

			index = mFPSDisplay % 10;
			mNumerTex[index].draw(x, y);
		}
	}
}
