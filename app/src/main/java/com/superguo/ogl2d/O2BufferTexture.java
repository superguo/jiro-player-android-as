package com.superguo.ogl2d;

import android.opengl.*;

public final class O2BufferTexture extends O2Texture {
	int fbo;
	int depthBuf;
	int width;
	int height;

	O2BufferTexture(int width, int height) {
		super(true);
		this.width = width;
		this.height = height;
	}

	@Override
	public void recreate() {
		// generate texture, frame buffer & render buffer
		int intArr[] = new int[3];
		GLES10.glGenTextures(1, intArr, 0);
		GLES11Ext.glGenFramebuffersOES(1, intArr, 1);
		GLES11Ext.glGenRenderbuffersOES(1, intArr, 2);
		mTex = intArr[0];
		fbo = intArr[1];
		depthBuf = intArr[2];
	}

	public final void begin()
	{

		// use frame buffer
		GLES11Ext.glBindFramebufferOES(GLES11Ext.GL_FRAMEBUFFER_OES, fbo);

		// attach a texture
		GLES10.glBindTexture(mTex, GLES10.GL_TEXTURE_2D);
		GLES10.glTexImage2D(GLES10.GL_TEXTURE_2D, 0, GLES10.GL_RGBA,  width, height, 0, GLES10.GL_RGBA, GLES10.GL_UNSIGNED_SHORT_4_4_4_4, null);
		GLES11Ext.glFramebufferTexture2DOES(GLES11Ext.GL_FRAMEBUFFER_OES, GLES11Ext.GL_COLOR_ATTACHMENT0_OES, GLES10.GL_TEXTURE_2D, mTex, 0);

		// attach a depth buffer
		GLES11Ext.glBindRenderbufferOES(GLES11Ext.GL_RENDERBUFFER_OES, depthBuf);
		GLES11Ext.glRenderbufferStorageOES(GLES11Ext.GL_RENDERBUFFER_OES, GLES11Ext.GL_DEPTH_COMPONENT16_OES, width, height);
		GLES11Ext.glFramebufferRenderbufferOES(GLES11Ext.GL_FRAMEBUFFER_OES, GLES11Ext.GL_DEPTH_ATTACHMENT_OES, GLES11Ext.GL_RENDERBUFFER_OES, depthBuf);
		
		// setup view port and matrices
        GLES10.glShadeModel(GLES10.GL_FLAT);
        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);
		GLES10.glViewport(0, 0, width, height);
        
		GLES10.glMatrixMode(GLES10.GL_PROJECTION);
        //GLES10.glPushMatrix();
        GLES10.glLoadIdentity();
        GLES10.glOrthof(0.0f, width,0.0f,  height, 0.0f, 1.0f);
        
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        //GLES10.glPushMatrix();
        GLES10.glLoadIdentity();
        
        // Magic offsets to promote consistent rasterization.
        GLES10.glTranslatef(0.375f, height + 0.375f, 0.0f);
        GLES10.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        
	}

	public final void end()
	{
		GLES11Ext.glBindFramebufferOES(GLES11Ext.GL_FRAMEBUFFER_OES, 0);
	}
	
	@Override
	public void destroy()
	{
		int intArr[] = {fbo, depthBuf};
		GLES11Ext.glDeleteFramebuffersOES(1, intArr, 0);
		GLES11Ext.glDeleteRenderbuffersOES(1, intArr, 1);
		super.destroy();
	}
}
