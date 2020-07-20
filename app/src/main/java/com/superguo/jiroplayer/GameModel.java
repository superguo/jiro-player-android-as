package com.superguo.jiroplayer;

public final class GameModel {
	private PlayLayout mDefaultLayout = new PlayLayout();
	public PlayLayout layout;
	public PlayModel playingModel = new PlayModel();

	public GameModel() {
		layout = (PlayLayout) mDefaultLayout.clone();
	}
}
