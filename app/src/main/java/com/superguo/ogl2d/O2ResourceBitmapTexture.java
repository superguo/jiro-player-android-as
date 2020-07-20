package com.superguo.ogl2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class O2ResourceBitmapTexture extends O2Texture {
	int mResId;

	O2ResourceBitmapTexture(boolean managed, int resId, Resources res) {
		super(managed);
		mResId = resId;
		if (O2Director.sInstance.mGl != null)
			recreate();
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void recreate() {
		if (O2Director.sInstance.mGl == null)
			return;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(
				O2Director.sInstance.getResources(), mResId, opts);
		createTexFromBitmap(bitmap);
		mAvailable = true;
	}
}
