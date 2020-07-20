package com.superguo.jiroplayer;

import android.util.Log;

public class PlayLayout implements Cloneable
{	
	private static final String TAG = "PlayLayout";
	public static final int SCREEN_WIDTH = 512;
	public static final int MTAIKO_WIDTH = 94;
	public static final int NOTE_SIZE = 48;
	public static final int SCROLL_BAND_WIDTH =
		PlayLayout.SCREEN_WIDTH - PlayLayout.MTAIKO_WIDTH - PlayLayout.NOTE_SIZE;
	public int MTaikoY = 90;
	public int scrollFieldHeight=56;
	public int scrollFieldY=125;
	public int seNotesY=165;
	public int normaGaugeWidth=256;
	public int normaGaugeX=348;
	public int normaGaugeY=33;
	public int scoreX=450;
	public int scoreY=76;
	public int addScoreX=450;
	public int addScoreY=55;
	public int rollBalloonX=197;
	public int rollBalloonY=35;
	public int rollNumberX=183;
	public int rollNumberY=27;
	public int burstBalloonX=200;
	public int burstBalloonY=77;
	public int burstNumberX=200;
	public int burstNumberY=77;
	public int comboBalloonX=220;
	public int comboBalloonY=77;
	public int comboNumberX=220;
	public int comboNumberY=77;
	public int courseSymbolX=185;
	public int courseSymbolY=45;
	public int playerCharacterX=100;
	public int playerCharacterY=45;
	public int playerCharacterBalloonX=133;
	public int playerCharacterBalloonY=130;
	public int dancerY=275;
	public int songTitleY=190;
	public int branchBalloonX=175;
	public int branchBalloonY=37;
	
	public PlayLayout clone() {
		try {
			return (PlayLayout) super.clone();
		} catch (CloneNotSupportedException e) {
			Log.wtf(TAG, e);
			return null;
		}
	}
}