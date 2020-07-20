

package com.superguo.jiroplayer;

import java.io.*;

import javax.microedition.khronos.opengles.*;

import android.content.res.*;
//import android.graphics.*;
import android.media.*;
import android.util.Log;
import android.view.*;

import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private static final String TAG = "PlayScene";
	@SuppressWarnings("unused")
	private GameModel mGameModel;
	private PlayLayout mLayout;
	
	private SoundPool mSoundPool;
	private int 	 mSoundDong;
	private int 	 mSoundKa;

	private O2Sprite mBgSprite;
	private O2Sprite mMTaikoSprite;
	private O2TextureSlices mNotesSlices;
	private O2Sprite mTargetNoteSprite;
	private O2Sprite mTextSprite1;
	private O2Sprite mTextSprite2;

	private int mDrumY;
	private int mDrumX1;
	private int mDrumX2;
	
	/*
	private Paint paint;
	private Typeface typeface;
	private long paintId;
	*/

	public PlayScene(O2Director director, GameModel gameModel) {
		super(director);
		mGameModel = gameModel;
		mLayout = gameModel.layout;

		/*
		 * paint = new Paint(); typeface =
		 * Typeface.createFromAsset(director.getContext().getAssets(),
		 * "a-otf-kanteiryustd-ultra.otf"); paint.setTypeface(typeface); paintId
		 * = director.addPaint(paint);
		 */
	}

	@Override
	public void onEnteringScene() {
		mSoundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
		mSoundDong = mSoundPool.load(mDirector.getContext(), R.raw.dong, 1);
		mSoundKa = mSoundPool.load(mDirector.getContext(), R.raw.ka, 1);
		O2TextureManager tmgr = mDirector.getTextureManager();
		O2SpriteManager smgr = mDirector.getSpriteManager();
		mBgSprite = new O2Sprite(tmgr.createFromResource(R.drawable.bg,
				true));
		smgr.addSprite(mBgSprite);
		mMTaikoSprite = new O2Sprite(tmgr.createFromResource(R.drawable.mtaiko,
				true));
		mMTaikoSprite.valign = O2Texture.VALIGN_MIDDLE;
		mMTaikoSprite.x = 0;
		mMTaikoSprite.y = mLayout.scrollFieldY;
		smgr.addSprite(mMTaikoSprite);

		mNotesSlices = tmgr.createSpriteSheetWithRowsAndCols(
				tmgr.createFromResource(R.drawable.notes, true), 2, 13);
		mTargetNoteSprite = new O2Sprite(mNotesSlices, 0);
		mTargetNoteSprite.valign = O2Texture.VALIGN_MIDDLE;
		mTargetNoteSprite.x = PlayLayout.MTAIKO_WIDTH;
		mTargetNoteSprite.y = mLayout.scrollFieldY;
		smgr.addSprite(mTargetNoteSprite);

		try {
			InputStream is = mDirector.getContext().getAssets()
					.open("utf8test.txt", AssetManager.ACCESS_RANDOM);
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String text1 = ir.readLine();
			String text2 = ir.readLine();

			mTextSprite1 = new O2Sprite(tmgr.createFromString(text1, true));
			mTextSprite1.x = 100;
			smgr.addSprite(mTextSprite1);

			mTextSprite2 = new O2Sprite(tmgr.createFromString(text2, true));
			mTextSprite2.x = 200;
			smgr.addSprite(mTextSprite2);
		} catch (IOException e) {
			Log.e(TAG, "onEnteringScene", e);
		}
	}

	@Override
	public void onLeavingScene() {
		O2SpriteManager smgr = mDirector.getSpriteManager();

		smgr.removeSprite(mBgSprite);
		smgr.removeSprite(mMTaikoSprite);
		smgr.removeSprite(mTargetNoteSprite);
		smgr.removeSprite(mTextSprite1);
		smgr.removeSprite(mTextSprite2);

		mNotesSlices = null;

		mSoundPool.release();
		mSoundPool = null;
	}

	@Override
	public void onSizeChanged() {
		mDrumY = (int) mDirector.toYDevice(mLayout.seNotesY);
		mDrumX1 = (int) mDirector.toXDevice(178);
		mDrumX2 = (int) mDirector.toXDevice(334);
	}

	@Override
	public void onPause() {
		mSoundPool.autoPause();
	}

	@Override
	public void onResume() {
		mSoundPool.autoResume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e==null || O2Director.getInstance()==null) {
			return false;
		}
		long eventTime;
		int x, y;
		switch (e.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			eventTime = e.getEventTime();
			x = (int) e.getX();
			y = (int) e.getY();
			break;
		case MotionEvent.ACTION_POINTER_DOWN: {
			eventTime = e.getEventTime();
			int ptrIdx = e.getActionIndex();
			x = (int) e.getX(ptrIdx);
			y = (int) e.getY(ptrIdx);
            break;
		}
		default:
			return false;
		}
			
		long delay = android.os.SystemClock.uptimeMillis() - eventTime;
		Log.i(TAG, "touch time=" + eventTime + " delay=" + delay);
		if (y <= mDrumY) {
			return false;
		}
		if (mDrumX1 < x && x < mDrumX2)
			mSoundPool.play(mSoundDong, 1.0f, 1.0f, 10, 0, 1.0f);
		else
			mSoundPool.play(mSoundKa, 1.0f, 1.0f, 10, 0, 1.0f);

		return false;
	}

	@Override
	public void postDraw(GL10 gl) {
		FPSHolder.getInstance().showFPS();
	}

	@Override
	public void dispose() {
		onLeavingScene();
	}

}
