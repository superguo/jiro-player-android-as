package com.superguo.ogl2d;

import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

public final class O2SpriteManager {

	/**
	 * Currently not thread safe
	 */
	private Vector<O2Sprite> mSpriteVec;
	
	/**
	 * For performance enhancement
	 */
	private O2Sprite[] mSpriteArray;
	
	private int mMaxId;

	O2SpriteManager() {
		mSpriteVec = new Vector<O2Sprite>(30);
		mSpriteArray = new O2Sprite[30];
	}

	public static O2SpriteManager getInstance() {
		return O2Director.sInstance.mSpriteManager;
	}

	public void drawAllSprites(GL10 gl) {
		for (O2Sprite sprite : mSpriteArray) {
			if (sprite==null)
				continue;
			sprite.draw(gl);
		}
	}

	public void addSprite(O2Sprite sprite) {
		sprite.mId = ++mMaxId;
		mSpriteVec.add(sprite);
		mSpriteArray = mSpriteVec.toArray(mSpriteArray);
	}

	public void removeSprite(O2Sprite sprite) {
		if (sprite==null) {
			return;
		}
		mSpriteVec.remove(sprite);
		mSpriteArray = mSpriteVec.toArray(mSpriteArray);
	}
}
