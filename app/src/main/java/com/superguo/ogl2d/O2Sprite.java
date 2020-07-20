package com.superguo.ogl2d;

import javax.microedition.khronos.opengles.*;

public class O2Sprite {

	// id to distinct itself from other O2Sprite
	int mId;

	// create from texture
	protected O2Texture mTexture;

	// create from texture slices
	protected O2TextureSlices mTexSli;
	protected int mIndexOfSli;

	// public properties
	public int halign;
	public int valign;
	public int x;
	public int y;
	public int zorder;

	public O2Sprite(O2Texture aTexture) {
		mTexture = aTexture;
	}

	public O2Sprite(O2TextureSlices aTexSli, int anIndex) {
		mTexSli = aTexSli;
		mIndexOfSli = anIndex;
	}

	public void draw(GL10 gl) {
		if (null != mTexture) {
			mTexture.draw(x, y, halign, valign);
		} else {
			mTexSli.draw(mIndexOfSli, x, y, halign, valign);
		}
	}
}
