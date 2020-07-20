package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;
import com.superguo.jiroplayer.TJANotation.Note;
import com.superguo.jiroplayer.TJANotation.NoteBar;

/** PlayModel's publicly visible data are all in this class
 * 
 * @author superguo
 *
 */
public final class PlayerMessage {
//	/** Contain moving notes only 
//	 * So notes with type of
//	 * NOTE_START_ROLLING_BALOON or
//	 * NOTE_START_ROLLING_POTATO
//	 * are contained only when it is not in playing(rolling)
//	 */
	
	public int courseIndex;
	public int score;
	public int gauge;

	/** The added score
	 * Drawer must set to 0 after the frame is drawn
	 */
	public int addedScore;
	public int numCombos;
	public int numMaxCombos;
	
	/** The number of NORMAL notes */
	public int numBeatedNormalNotes;
	
	/** The number of GOOD notes */
	public int numBeatedGoodNotes;

	/** The number of BAD+MISSED notes */
	public int numMissedOrBadNotes;

	/** The number of len-da hits */
	public int numTotalLenda;

	/** The value is one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * Drawer must set to HIT_NONE after the frame is drawn
	 */
	//public int iHit;

	/** The action branch */
	public Bar[] actionBranch;
	
	/**
	 * The next branch. It may be null if #BRANCHSTART is coming but cannot
	 * determine the next branch
	 */
	public Bar[] nextBranch;
	
	/** The action note bar is the one that appears in the screen first. */
	public int actionNoteBarIndex;

	/** The index of the upcoming note that is not hit or missed or broken yet. */
	public int actionNoteIndex;

	/** The note beated */
	public int beatedNote;
	
	/** The note in rolling state */
	public Note actionRollingNote;
	
	/** Indicate the current note is played good,
	 * 	play normal, missed, passed or not judged yet
	 * Drawer must set to NOTE_JUDGED_NONE after the frame is drawn
	 */
	public int noteJudged;
	
	/** Current branch */
	public int branchIndex;
	
	public boolean isGGT;
	
	/**
	 * One if following values
	 * <ul>
	 * <li>{@link PlayModel#ROLLING_NONE}</li>
	 * <li>{@link PlayModel#ROLLING_LENDA_BAR}</li>
	 * <li>{@link PlayModel#ROLLING_BIG_LENDA_BAR}</li>
	 * <li>{@link PlayModel#ROLLING_BALLOON}</li>
	 * <li>{@link PlayModel#ROLLING_POTATO}</li>
	 * </ul>
	 */
	public int rollingState;		
	
	/** Can be positive or negative or zero 
	 * Non-negative value means the rolling counter:
	 * down-counter for balloon/potato
	 * up counter for len-da bar
	 * 
	 * Negative value means the rolling ended:
	 * see SPECIAL_ROLLING_COUNT_
	 * 
	 * In the case of negative value, the drawer must set to 0 
	 * after the "rolling end" animation just starts to draw
	 * 
	 */
	public int rollingCount;
	
	public void reset(TJACourse course, Bar[] branch) {
		// Reset current course
		courseIndex = course.courseIndex;

		// Reset current score
		score = 0;
		addedScore = 0;
		
		gauge = 0;

		actionBranch = nextBranch = branch;
		actionNoteBarIndex = 0;
		actionNoteIndex = 0;
		
		// Reset hit
		//iHit = PlayModel.HIT_NONE;
		
		// Reset judge
		noteJudged = PlayModel.JUDGED_NONE;
		
		// Reset branch state
		if (course.hasBranches)
			branchIndex = PlayModel.BRANCH_INDEX_NORMAL;
		else
			branchIndex = PlayModel.BRANCH_INDEX_NONE;
		
		// Reset GO-GO-TIME state
		isGGT = false;
		
		// Reset rolling states
		actionRollingNote = null;
		rollingState = PlayModel.ROLLING_NONE;
		rollingCount = 0;

		// Reset some counters
		numMaxCombos = numBeatedNormalNotes = numBeatedGoodNotes = numMissedOrBadNotes = numTotalLenda = 0;
	}
	
	public void handleBadOrMissedHit(int[] gaugePerNote) {
		gauge -= gaugePerNote[PlayModel.GS_INDEX_TWICE];
		if (gauge<0) {
			gauge=0;
		}
		numMissedOrBadNotes++;
		numCombos = 0;
	}
	
	public void handleExpectedHit(boolean isGood, boolean isBig, int[] gaugePerNote, int[][] scorePerNote) {
		if (++numCombos > numMaxCombos) {
			numMaxCombos = numCombos;
		}
		int gsIndex = 0;
		if (isGood) {
			gsIndex++;
			numBeatedGoodNotes++;
		} else {
			numBeatedNormalNotes++;
		}
		if (isBig) gsIndex++;
		gauge += gaugePerNote[gsIndex];
		if (gauge >= PlayModel.MAX_GAUGE) {
			gauge = PlayModel.MAX_GAUGE;
		}
		int ggtIndex = isGGT ? PlayModel.GGT_INDEX_ON : PlayModel.GGT_INDEX_OFF;
		int baseScore = scorePerNote[ggtIndex][gsIndex];
		if (numCombos<100) {
			addedScore = baseScore * (numCombos / 10 + 1);  
		} else if (numCombos==100){
			addedScore = baseScore * 11;
		}
		score += addedScore;
	}
	
	public void handleRollingHit() {
		int ggtIndex = isGGT ? PlayModel.GGT_INDEX_ON : PlayModel.GGT_INDEX_OFF;
		switch (rollingState) {
		case PlayModel.ROLLING_LENDA_BAR:
			addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_NORMAL];
			rollingCount++;
			break;

		case PlayModel.ROLLING_BIG_LENDA_BAR:
			addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_BIG];
			rollingCount++;
			break;

		case PlayModel.ROLLING_BALLOON:
			if (rollingCount==1) {
				rollingCount = PlayModel.SPECIAL_ROLLING_COUNT_BALLOON_FINISHED;
				addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_BALLON_POPPED];
			} else {
				rollingCount--;
				addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_NORMAL];
			}
			break;

		case PlayModel.ROLLING_POTATO:
			if (rollingCount==1) {
				rollingCount = PlayModel.SPECIAL_ROLLING_COUNT_POTATO_FINISHED;
				addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_BALLON_POPPED];
			} else {
				rollingCount--;
				addedScore = PlayModel.SCORE_PER_LENDA_NOTE[ggtIndex][PlayModel.LENDA_SCORE_INDEX_NORMAL];
			}
			break;
		}
	}
}
