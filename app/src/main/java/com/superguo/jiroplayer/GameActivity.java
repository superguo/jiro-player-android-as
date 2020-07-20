package com.superguo.jiroplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.superguo.ogl2d.O2Director;

public class GameActivity extends Activity {
	// GLSurfaceView gameView;
	private O2Director mDirector;
	private GameModel mGameModel;
	private PlayScene mPlayScene;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mDirector = O2Director.createInstance(this,
				new O2Director.Config(512, 384));
		mGameModel = new GameModel();
		mPlayScene = new PlayScene(mDirector, mGameModel);
		mDirector.setCurrentScene(mPlayScene);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onPause() {
		mDirector.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mDirector.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mDirector != null) {
			mDirector.onDestroy();
			mDirector = null;

			FPSHolder.getInstance().destroy();
			mPlayScene = null;
			mGameModel = null;
		}
		super.onDestroy();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		mDirector.fastTouchEvent(e);
		getWindow().superDispatchTouchEvent(e);
		return true;
	}

}
