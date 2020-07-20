package com.superguo.ogl2d;

import android.graphics.*;

class O2InternalBitmapTexture extends O2Texture {

	private Bitmap mBitmap;

	O2InternalBitmapTexture(boolean managed, Bitmap bitmap) {
		super(managed);
		mBitmap = managed ? bitmap.copy(bitmap.getConfig(), false) : bitmap;
		if (O2Director.sInstance.mGl != null)
			recreate();
	}

	@Override
	public void destroy() {
		if (mBitmap != null && mManaged)
			mBitmap.recycle();
		super.destroy();
	}

	@Override
	public void recreate() {
		createTexFromBitmap(mBitmap);
		mAvailable = true;
	}
}