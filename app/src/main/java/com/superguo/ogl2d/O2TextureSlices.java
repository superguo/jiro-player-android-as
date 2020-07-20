package com.superguo.ogl2d;

import java.nio.*;
import java.util.*;

import android.graphics.*;
import android.opengl.GLES10;
import android.opengl.GLES11;

public class O2TextureSlices implements Comparable<O2TextureSlices>{
	protected O2Texture mTexture;
	protected boolean mCreated;
	protected static final int CREATE_FROM_ROWS_AND_COLS = 1;
	protected int mCreateMethod;
	protected int mCreateArg0;
	protected int mCreateArg1;
	
	protected int mVboFullTexCoods[];
	protected Point[] mSizes;
	private int mVertCoods[] = new int[8];
	private IntBuffer mVertBuf = 
		ByteBuffer.allocateDirect(32).order(null).asIntBuffer();

	protected O2TextureSlices(O2Texture texture, int createMethod, int createArg0, int createArg1, boolean doVerification)
	{
		mTexture = texture;
		mCreateMethod = createMethod;
		mCreateArg0 = createArg0;
		mCreateArg1 = createArg1;
		if (doVerification) verify();
	}

	public int compareTo(O2TextureSlices another) {
		//if (createMethod)
		return 0;
	}
	
	protected void verify()
	{
		switch (mCreateMethod)
		{
		case CREATE_FROM_ROWS_AND_COLS:
			if (mCreateArg0<0 || mCreateArg1<0) throw new IllegalArgumentException();
			return;
			
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	public final void draw(int index, int targetX, int targetY)
	{
		int vertCoods[] = mVertCoods;

		vertCoods[0] = vertCoods[4] = targetX << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = (targetX + mSizes[index].x) << 16;
		vertCoods[5] = vertCoods[7] = (targetY + mSizes[index].y) << 16;
		
		drawFull(index);
	}
	
	public final void draw(int index, int targetX, int targetY, int halign, int valign)
	{
		int vertCoods[] = mVertCoods;

		vertCoods[0] = vertCoods[4] = (targetX << 16) - (mSizes[index].x << 15) * halign;
		vertCoods[1] = vertCoods[3] = (targetY << 16) - (mSizes[index].y << 15) * valign;
		vertCoods[2] = vertCoods[6] = vertCoods[0] + (mSizes[index].x << 16);
		vertCoods[5] = vertCoods[7] = vertCoods[1] + (mSizes[index].y << 16);
		
		drawFull(index);
	}
	
	private final void drawFull(int index)
	{
		mVertBuf.position(0);
		mVertBuf.put(mVertCoods);
		mVertBuf.position(0);

		// Specify the vertex pointers
		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, mVertBuf);
		
		// Specify the texture coordinations
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, mTexture.mTex);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, mVboFullTexCoods[index]);
		GLES11.glTexCoordPointer(2, GLES10.GL_FIXED, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}

	protected void create() {
		switch (mCreateMethod)
		{
		case CREATE_FROM_ROWS_AND_COLS:
			createFromRowsAndCols(mCreateArg0, mCreateArg1);
			return;

		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private void createFromRowsAndCols(int rows, int cols)
	{
		int i, j, w, h;
		w = mTexture.mWidth/cols;
		h = mTexture.mHeight/rows;
		int count = rows*cols;
		if (count==0) return;
		
		// block sizes, using Point as size type
		mSizes = new Point[count];
		Point fixedSize = new Point(w, h);
		Arrays.fill(mSizes, fixedSize);
		
		// VBO
		mVboFullTexCoods = new int[count];
		int fullTexCoods[] = new int[8];
		IntBuffer fullTexBuf = ByteBuffer.allocateDirect(32).order(null).asIntBuffer();
		GLES11.glGenBuffers(count, mVboFullTexCoods, 0);
		int texPowOf2Width = mTexture.mTexPowOf2Width;
		int texPowOf2Height = mTexture.mTexPowOf2Height;
		for (i=0; i<rows; ++i)
			for (j=0; j<cols; ++j)
			{
				int left = j*w;
				int top = i*h;
				int right = left + w;
				int bottom = top + h;
				fullTexCoods[0] = fullTexCoods[4] = left 	<< (16-texPowOf2Width);
				fullTexCoods[1] = fullTexCoods[3] = top 	<< (16-texPowOf2Height);
				fullTexCoods[2] = fullTexCoods[6] = right  	<< (16-texPowOf2Width);
				fullTexCoods[5] = fullTexCoods[7] = bottom 	<< (16-texPowOf2Height);
				fullTexBuf.position(0);
				fullTexBuf.put(fullTexCoods);
				fullTexBuf.position(0);
				GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, mVboFullTexCoods[i*cols+j]);
				GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 32, fullTexBuf, GLES11.GL_STATIC_DRAW);
				GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
			}
		mCreated = true;
	}
}
