package com.superguo.ogl2d;

import android.graphics.*;

public class O2StringTexture extends O2Texture {

	protected String mText;
	protected long mPaintId;

	protected O2StringTexture(boolean managed, String text, long paintId) {
		super(managed);
		mText = managed ? new String(text) : text;
		mPaintId = paintId;
		if (O2Director.sInstance.mGl != null) {
			recreate();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void recreate() {
		Paint paint = O2Director.sInstance.getPaint(mPaintId);
		Rect rect = new Rect();
		paint.setTextAlign(Paint.Align.LEFT);
		paint.getTextBounds(mText, 0, mText.length(), rect);
		Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(),
				Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(mText, 0, -rect.top, paint);
		createTexFromBitmap(bitmap);
	}
}
