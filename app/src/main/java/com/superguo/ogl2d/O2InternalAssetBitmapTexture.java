package com.superguo.ogl2d;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

class O2InternalAssetBitmapTexture extends O2Texture {
	
	String mAssetPath;
	
	O2InternalAssetBitmapTexture(boolean managed, String assetPath,
			AssetManager assetMan) {
		super(managed);
		mAssetPath = managed ? new String(assetPath) : assetPath;
		if (O2Director.sInstance.mGl != null) {
			recreate();
		}
	}

	void create(AssetManager assetMan) throws IOException
	{
	}
	
	@Override
	public void destroy() {
		mAssetPath = null;
		super.destroy();
	}

	@Override
	public void recreate() {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(O2Director.sInstance
					.getContext().getAssets().open(mAssetPath));
			createTexFromBitmap(bitmap);
			mAvailable = true;
		} catch (IOException e) {
			mAvailable = false;
		}
	}
}
