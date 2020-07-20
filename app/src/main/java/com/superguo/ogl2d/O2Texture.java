package com.superguo.ogl2d;

import java.nio.*;
//import javax.microedition.khronos.opengles.*;
import android.graphics.*;
import android.opengl.*;

public abstract class O2Texture {
	public static final int MAX_SIZE = 1024;
	public static final int HALIGN_LEFT = 0;
	public static final int HALIGN_MIDDLE = 1;
	public static final int HALIGN_RIGHT = 2;
	public static final int VALIGN_TOP = 0;
	public static final int VALIGN_MIDDLE = 1;
	public static final int VALIGN_BOTTOM = 2;

	protected boolean mAvailable;
	protected boolean mManaged;
	protected int mTex;
	protected int mWidth;
	protected int mHeight;
	protected int mTexPowOf2Width;
	protected int mTexPowOf2Height;
	protected int mVboFullTexCood;

	private int mVertCoods[] = new int[8];
	private int mTexCoods[] = new int[8];
	private IntBuffer mVertBuf = ByteBuffer.allocateDirect(32).order(null)
			.asIntBuffer();
	private IntBuffer mTexBuf = ByteBuffer.allocateDirect(32).order(null)
			.asIntBuffer();

	protected O2Texture(boolean managed) {
		if (O2Director.sInstance == null) {
			throw new O2Exception("O2Director instance not created");
		}

		if (!managed && O2Director.sInstance.mGl == null) {
			throw new O2Exception(
					"Cannot create unmanaged sprite when GL surface is lost");
		}

		mManaged = managed;
		mAvailable = false;
	}

	public abstract void recreate();

	public void destroy() {
		if (O2Director.sInstance != null && O2Director.sInstance.mGl != null) {
			int texArr[] = { mTex };
			GLES10.glDeleteTextures(1, texArr, 0);
			int vboArray[] = { mVboFullTexCood };
			GLES11.glDeleteBuffers(0, vboArray, 0);
		}
		mAvailable = false;
	}

	protected void createTexFromBitmap(Bitmap bmp) {
		mWidth = bmp.getWidth();
		mHeight = bmp.getHeight();

		if (mWidth > MAX_SIZE || mHeight > MAX_SIZE)
			throw new IllegalArgumentException(
					"bitmap width/height larger than 1024");

		if (mWidth <= 0 || mHeight <= 0)
			throw new IllegalArgumentException("bitmap width/height <= 0");

		boolean hasEnlarged = false;

		// pad the size of bitmap to the power of 2
		mTexPowOf2Width = 31 - Integer.numberOfLeadingZeros(mWidth);
		if (Integer.lowestOneBit(mWidth) != Integer.highestOneBit(mWidth)) {
			++mTexPowOf2Width;
			hasEnlarged = true;
		}

		mTexPowOf2Height = 31 - Integer.numberOfLeadingZeros(mHeight);
		if (Integer.lowestOneBit(mHeight) != Integer.highestOneBit(mHeight)) {
			++mTexPowOf2Height;
			hasEnlarged = true;
		}

		if (hasEnlarged) {
			Bitmap standardBmp = Bitmap.createBitmap(1 << mTexPowOf2Width,
					1 << mTexPowOf2Height, Bitmap.Config.ARGB_4444);
			standardBmp.setDensity(bmp.getDensity());
			Canvas canvas = new Canvas(standardBmp);
			canvas.setDensity(bmp.getDensity());
			canvas.drawBitmap(bmp, 0.0f, 0.0f, new Paint());
			createTexFromStandardBitmap(standardBmp);
			canvas = null;
			standardBmp.recycle();
			standardBmp = null;
		} else
			createTexFromStandardBitmap(bmp);
	}

	private void createTexFromStandardBitmap(Bitmap bmp) {
		// width, height, texPowOf2Width, texPowOf2Height
		// are done in createTexFromBitmap(Bitmap)
		int texArr[] = new int[1];
		GLES10.glGenTextures(1, texArr, 0);
		mTex = texArr[0];
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTex);
		GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D,
				GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST);
		GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D,
				GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR);

		GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S,
				GLES10.GL_CLAMP_TO_EDGE);
		GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T,
				GLES10.GL_CLAMP_TO_EDGE);

		GLES10.glTexEnvf(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE,
				GLES10.GL_REPLACE);
		GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bmp, 0);

		int[] vboArr = new int[1];
		int texCoodFixedW = mWidth << (16 - mTexPowOf2Width);
		int texCoodFixedH = mHeight << (16 - mTexPowOf2Height);
		int fullTexCoods[] = { 0, 0, texCoodFixedW, 0, 0, texCoodFixedH,
				texCoodFixedW, texCoodFixedH };
		IntBuffer fullTexBuf = ByteBuffer.allocateDirect(32).order(null)
				.asIntBuffer();
		fullTexBuf.position(0);
		fullTexBuf.put(fullTexCoods);
		fullTexBuf.position(0);

		GLES11.glGenBuffers(1, vboArr, 0);
		mVboFullTexCood = vboArr[0];
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, mVboFullTexCood);
		GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 32, fullTexBuf,
				GLES11.GL_STATIC_DRAW);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
	}

	public final void draw(int targetX, int targetY) {
		// vertex coordinations
		int vertCoods[] = mVertCoods;

		vertCoods[0] = vertCoods[4] = targetX << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = vertCoods[0] + (mWidth << 16);
		vertCoods[5] = vertCoods[7] = vertCoods[1] + (mHeight << 16);

		drawFull();
	}

	public final void draw(int targetX, int targetY, int halign, int valign) {
		// vertex coordinations
		int vertCoods[] = mVertCoods;

		vertCoods[0] = vertCoods[4] = (targetX << 16) - (mWidth << 15) * halign;
		vertCoods[1] = vertCoods[3] = (targetY << 16) - (mHeight << 15)
				* valign;
		vertCoods[2] = vertCoods[6] = vertCoods[0] + (mWidth << 16);
		vertCoods[5] = vertCoods[7] = vertCoods[1] + (mHeight << 16);

		drawFull();
	}

	private final void drawFull() {
		mVertBuf.position(0);
		mVertBuf.put(mVertCoods);
		mVertBuf.position(0);

		// Specify the vertex pointers
		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, mVertBuf);

		// Specify the texture coordinations
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTex);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, mVboFullTexCood);
		GLES11.glTexCoordPointer(2, GLES10.GL_FIXED, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}

	public final void draw(int srcX, int srcY, int srcWidth, int srcHeight,
			int tagetX, int targetY) {
		int vertCoods[] = mVertCoods;

		vertCoods[0] = vertCoods[4] = tagetX << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = (tagetX + srcWidth) << 16;
		vertCoods[5] = vertCoods[7] = (targetY + srcHeight) << 16;

		mVertBuf.position(0);
		mVertBuf.put(vertCoods);
		mVertBuf.position(0);

		int texCoods[] = mTexCoods;

		texCoods[0] = texCoods[4] = srcX << (16 - mTexPowOf2Width);
		texCoods[1] = texCoods[3] = srcY << (16 - mTexPowOf2Height);
		texCoods[2] = texCoods[6] = (srcX + srcWidth) << (16 - mTexPowOf2Width);
		texCoods[5] = texCoods[7] = (srcY + srcHeight) << (16 - mTexPowOf2Height);

		mTexBuf.position(0);
		mTexBuf.put(texCoods);
		mTexBuf.position(0);

		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, mVertBuf);
		GLES10.glTexCoordPointer(2, GLES10.GL_FIXED, 0, mTexBuf);

		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}

	public final int getWidth() {
		return mWidth;
	}

	public final int getHeight() {
		return mHeight;
	}

	public final boolean isManaged() {
		return mManaged;
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}
}
