/** PlayModel the playing model for TJAFormat
 * @author superguo
 */
package com.superguo.jiroplayer;

import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;
import com.superguo.jiroplayer.TJANotation.Note;
import com.superguo.jiroplayer.TJANotation.NoteBar;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand.BranchJudge;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand.BranchJudgePrecision;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand.BranchJudgeRoll;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand.BranchJudgeScore;
import static com.superguo.jiroplayer.TJANotation.NOTE_EMPTY;
import static com.superguo.jiroplayer.TJANotation.NOTE_FACE;
import static com.superguo.jiroplayer.TJANotation.NOTE_SIDE;
import static com.superguo.jiroplayer.TJANotation.NOTE_BIG_FACE;
import static com.superguo.jiroplayer.TJANotation.NOTE_BIG_SIDE;
import static com.superguo.jiroplayer.TJANotation.NOTE_LENDA;
import static com.superguo.jiroplayer.TJANotation.NOTE_BIG_LENDA;
import static com.superguo.jiroplayer.TJANotation.NOTE_BALLOON;
import static com.superguo.jiroplayer.TJANotation.NOTE_POTATO;

public final class PlayModel {
//	public static final int NOTE_BARLINE		= -1;

	public static final int BEAT_NOTE_NONE     = 0;
	public static final int BEAT_NOTE_FACE     = 1;
	public static final int BEAT_NOTE_BIG_FACE = 2;
	public static final int BEAT_NOTE_SIDE 	   = 3;
	public static final int BEAT_NOTE_BIG_SIDE = 4;

	public static final int BRANCH_INDEX_NONE	= 0;
	public static final int BRANCH_INDEX_NORMAL	= 1;
	public static final int BRANCH_INDEX_EASY	= 2;
	public static final int BRANCH_INDEX_MASTER	= 3;
	
//	public static final int ROLLING_NONE_LENDA		= -1;	// Just in convenience to display 
//	public static final int ROLLING_NONE_BIG_LENDA	= -2;	// Just in convenience to display
	public static final int ROLLING_NONE			= 0;
	public static final int ROLLING_LENDA_BAR		= 1;
	public static final int ROLLING_BIG_LENDA_BAR	= 2;
	public static final int ROLLING_BALLOON			= 3;
	public static final int ROLLING_POTATO			= 4;

	public static final int SPECIAL_ROLLING_COUNT_BALLOON_FINISHED 	= -1;
	public static final int SPECIAL_ROLLING_COUNT_BALLOON_FAILED 	= -2;
	public static final int SPECIAL_ROLLING_COUNT_POTATO_FINISHED 	= -3;
	public static final int SPECIAL_ROLLING_COUNT_POTATO_FAILED 	= -4;

	/** The time to wait before we start */
	public static final long START_WAIT_TIME = 2000;
	
	/** The time to wait after the whole notation ends */
	public static final int END_WAIT_TIME = 2000; 
	public static final int BEAT_DIST = 64;	// pixel distance between two beats
	
	public static final int HIT_NONE = 0;
	public static final int HIT_FACE = 1;
	public static final int HIT_SIDE = 2;
	
//	public static final int MAX_PREPROCESSED_BAR = 3;
	
	public static final int TIME_JUDGE_GOOD 	= 50;
	public static final int TIME_JUDGE_NORMAL 	= 150;
	public static final int TIME_JUDGE_BAD	 	= 217;

	static final int GGT_INDEX_OFF	= 0;
	static final int GGT_INDEX_ON	= 1;

	/** Half/full/twice gauge/scoring indice. */
	static final int GS_INDEX_HALF 	= 0;
	static final int GS_INDEX_FULL 	= 1;
	static final int GS_INDEX_TWICE = 2;
	
	public static final int LENDA_SCORE_INDEX_NORMAL = 0;
	
	public static final int LENDA_SCORE_INDEX_BIG = 1;
	
	public static final int LENDA_SCORE_INDEX_BALLON_POPPED = 2;

	public static final int[][] SCORE_PER_LENDA_NOTE = {
		{ 300, 360, 3000 },	// not in GGT
		{ 360, 430, 5000 }	// in GGT
	};

	/** The note is not judged yet	 */
	public static final int JUDGED_NONE 	= 0;
	
	/** The note is hit correctly within TIME_JUDGE_GOOD */
	public static final int JUDGED_GOOD 	= 1;
	
	/** The note is hit correctly between TIME_JUDGE_GOOD and TIME_JUDGE_NORMAL */
	public static final int JUDGED_NORMAL 	= 2;
	
	/** The note is hit correctly between TIME_JUDGE_NORMAL and TIME_JUDGE_MISSED 
	 * or the note is hit incorrectly within TIME_JUDGE_NORMAL */
	public static final int JUDGED_BAD 		= 3;
	
	/** The note is not hit within TIME_JUDGE_MISSED */
	public static final int JUDGED_MISSED	= 4;

	/** The maximum level of 4 course */
	public static final int MAX_LEVEL_OF[] = { 5, 7, 8, 10 };
	public static final int MAX_COURSE = 4;
	
	public static final int FULL_SCORES[][] =	{
		{	// easy
			0,
			150000,		// 1
			200000,		// 2
			250000,		// 3
			300000,		// 4
			350000,		// 5
		},
		{	// normal
			0,
			350000,		// 1
			400000,		// 2
			450000,		// 3
			500000,		// 4
			550000,		// 5
			600000,		// 6
			650000,		// 7
		},
		{	// hard
			0,
			500000,		// 1
			600000,		// 2
			650000,		// 3
			700000,		// 4
			750000,		// 5
			800000,		// 6
			850000,		// 7
			900000,		// 8
		},
		{	// oni
			0,
			6500000,	// 1
			7000000,	// 2
			8000000,	// 3
			8500000,	// 4
			9000000,	// 5
			9500000,	// 6
			1000000,	// 7
			1050000,	// 8
			1100000,	// 9
			1200000		// 10
		}
	};
	
	/** The max soul gauge grid */
	public static final int MAX_GAUGE_GRID = 50;
	/** The minimum gauge grids for pass of 4 courses */
	public static final int PASSED_GAUGE_GRID_OF[] = {25, 30, 35, 40};
	/** The gauge value per grid */
	public static final int GAUGE_PER_GRID = 100;
	/** The max soul gauge value */
	public static final int MAX_GAUGE = MAX_GAUGE_GRID * GAUGE_PER_GRID;
	/** The minimum GOOD notes played rate to reach max gauge */
	public static final double MAX_GAUGE_RATES[][] =	{
		{	// easy
			0,
			0.4,		// 1
			0.4,		// 2
			0.4,		// 3
			0.4,		// 4
			0.4,		// 5
		},
		{	// normal
			0,
			0.5,		// 1
			0.5,		// 2
			0.5,		// 3
			0.5,		// 4
			0.5,		// 5
			0.5,		// 6
			0.5,		// 7
		},
		{	// hard
			0,
			0.67,		// 1
			0.67,		// 2
			0.67,		// 3
			0.67,		// 4
			0.67,		// 5
			0.67,		// 6
			0.67,		// 7
			0.67,		// 8
		},
		{	// oni
			0,
			0.69,	// 1
			0.69,	// 2
			0.69,	// 3
			0.69,	// 4
			0.69,	// 5
			0.69,	// 6
			0.69,	// 7
			0.69,	// 8
			0.77,	// 9
			0.76	// 10
		}
	};
	
	// SECTION statistics
	final class SectionStat {
		int numRolled;
		int score;
		int precisionPlayed;
		int precisionTotal;

		void reset() {
			numRolled = score = precisionPlayed = precisionTotal = 0;
		}
	}

	/** Result for {@link #handleNormalNoteHit(boolean, boolean, boolean, int))}.
	 * Tells onEvent() to return with nothing to do	 */
	private static final int NORMAL_NOTE_RESULT_DO_NOTHING = 0;
	
	/** Result for {@link #handleNormalNoteHit(boolean, boolean, boolean, int))}.
	 * Tells onEvent() that it should not process next note
	 */
	private static final int NORMAL_NOTE_RESULT_REMOVE_NOTE = 1;
	
	/** Result for {@link #handleNormalNoteHit(boolean, boolean, boolean, int))}.
	 * Tells onEvent() that it should process next note	 */
	private static final int NORMAL_NOTE_RESULT_REMOVE_MISSED_NOTE = 2;
	private static final String TAG = "PlayModel";

	private TJAFormat mTJA;
	private TJACourse mCourse;
	private TJANotation mNotation;

	private PlayerMessage mPlayerMessage = new PlayerMessage();
//	private Bar[] mBars = new Bar[PlayModel.MAX_PREPROCESSED_BAR];
//	private IntegerRef mPreprocessedCommandIndexRef = new IntegerRef();
//	private IntegerRef mPreprocessedBarIndexRef = new IntegerRef();
	
	private int mRollingBaloonIndex;
	private int mScoreInit;
	private int mScoreDiff;
	private int[] mGaugePerNote = new int[3];
	private int[][] mScorePerNote = new int[2][3];
	private SectionStat mSectionStat;
	/** Indicate a new #SECTION is arranged before next notation	 */
	private boolean mIsSectionArranging;

	/** The adjusted offset time before first bar begins.
	 * In milliseconds.
	 * It can be positive or negative.
	 */
	private long mStartTimeMillis;
	
	/** The end time (when last bar is just passed) in milliseconds
	 */
	private long mEndTimeMillis;
	
	/** The last adjusted event time in milliseconds
	 * The event time is relative to start() is called
	 * It is always 0 after start() is called	 
	 */
//	private long mLastEventTimeMillis;
//	private int mLastPlayingBarIndex;
//	private int mLastPlayingNoteIndex;

	private String mPlayTimeError = "";
	private long mLastOffset;
	private StartBranchCommand mUpcomingStartBranchCommand;

	public final PlayerMessage prepare(TJAFormat tja, int courseIndex, int notationIndex) {
		mTJA = tja;
		mCourse = mTJA.courses[courseIndex];
		PlayerMessage playerMessage = mPlayerMessage;
		playerMessage.reset(mCourse, mNotation.normalBranch);
		
		// Compile and reset internal values
		TJANotationCompiler compiler = new TJANotationCompiler();
		mNotation = compiler.compile(tja,
				courseIndex,
				notationIndex,
				START_WAIT_TIME,
				END_WAIT_TIME,
				PlayLayout.NOTE_SIZE,
				PlayLayout.MTAIKO_WIDTH	+ PlayLayout.NOTE_SIZE,
				PlayLayout.SCREEN_WIDTH,
				PlayLayout.MTAIKO_WIDTH + PlayLayout.NOTE_SIZE / 2);
//		mPreprocessedCommandIndexRef.value = -1;
//		mPreprocessedBarIndexRef.value = -1;
//		mBranchExitIndexRef.value = 0;
//		mLastPlayingBarIndex = 0;
//		mLastPlayingNoteIndex = 0;
		mRollingBaloonIndex = -1;
		float totalScoringNotes;
		AtomicInteger totalGaugeNotesRef = new AtomicInteger();
		totalScoringNotes = computeScoringNotesAndGaugeNotes(mNotation, totalGaugeNotesRef);
		resetGauge(totalGaugeNotesRef.get()); // Reset iGaugePerNote
		resetScores(totalScoringNotes); // Reset the score info
		mScorePerNote[GGT_INDEX_OFF][GS_INDEX_FULL] = mScoreInit;
		mScorePerNote[GGT_INDEX_ON][GS_INDEX_FULL] = (int) (mScoreInit * 1.2f) / 10 * 10;
		mScorePerNote[GGT_INDEX_OFF][GS_INDEX_TWICE] = mScoreInit << 1;
		mScorePerNote[GGT_INDEX_ON][GS_INDEX_TWICE] = (int) (mScoreInit * 2.4f) / 10 * 10;
		mScorePerNote[GGT_INDEX_OFF][GS_INDEX_HALF] = ((mScoreInit / 10) >> 1) * 10;
		mScorePerNote[GGT_INDEX_ON][GS_INDEX_HALF] = ((mScoreInit / 10) >> 1) * 10;

		mSectionStat.reset(); // Reset the SECTION statistics

		mIsSectionArranging = mCourse.hasBranches ? true : false;
		
		mUpcomingStartBranchCommand = null;
		
		// Fill actionNoteBar and nextActionNoteBar
		for (int index=0; index<mCurrentBranch.length; ++index) {
			
			if (mCurrentBranch[index].isNoteBar) {
				if (mPlayerMessage.actionNoteBar==null) {
					mPlayerMessage.actionNoteBar = mCurrentBranch[index].noteBar;
					mPlayerMessage.actionNoteIndex = 0;
					mActionNoteBarIndex = index;
					mNextActionNoteBarIndex = nextNoteBarIndex(index, null, false);
					if (mNextActionNoteBarIndex != -1) {
						mPlayerMessage.nextActionNoteBar = mCurrentBranch[mNextActionNoteBarIndex].noteBar;
					}
					break;
				}
				
			}
		}

		return mPlayerMessage;
	}

	public final void start(long startTimeMillis) {
		mStartTimeMillis = startTimeMillis;
		mEndTimeMillis = 0;
		mLastOffset = 0;
		
//		mPlayerMessage.notePosCount = 0;

//		mPlayerMessage.notePosCount = translateNotePos(
//				mPlayerMessage.notePosArray, -mStartTimeMillis * 1000,
//				mBars, mLastPlayingBarIndex);
	}

	/**
	 * 
	 * @param eventTimeMillis
	 *            The time in milliseconds when hit event occurs
	 * @param hit
	 *            one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * @return true if and only if the playing is not finished.
	 */
	public final boolean onEvent(long eventTimeMillis, int hit) {
		long currentOffset = eventTimeMillis - mStartTimeMillis;
		if (currentOffset<mLastOffset) {
			throw new IllegalStateException("Time rewinded");
		}
		
		TJANotation notaion = mNotation;
		PlayerMessage playerMessage = mPlayerMessage;
		NoteBar actionNoteBar = playerMessage.actionNoteBar;
		NoteBar nextActionNoteBar = playerMessage.nextActionNoteBar;
		
		// Return false if we reach the end of notation plus the ending wait time
		if (actionNoteBar == null) {
			return notaion.endMillis < currentOffset;
		}
		
		Note[] actionNotes = actionNoteBar.notes;
		
		// TODO fetch nextActionNoteBar if null

		for (;;) {
			// If the current note bar has no notes at all!
			if (actionNotes.length == 0) {
				// handle special case - no next action note bar!
				if (nextActionNoteBar == null) {
					if (mActionNoteBarIndex==mCurrentBranch.length-1) {
						// reach the end of notation
						playerMessage.actionNoteBar = null;
						break;
					} else {
						// exception!
						throw new IllegalStateException("No notes in current bar and no next action bar");
					}
				}
				if (currentOffset >= nextActionNoteBar.beatMillis) {
					walkToNextActionNoteBar();
				} else {
					break;
				}
			} else {
				int index = playerMessage.actionNoteIndex;
				if (index < actionNotes.length) {
					int diff = (int) (actionNotes[index].beatMillis - currentOffset);
					int noteValue = actionNotes[index].noteValue;
					switch (noteValue) {
					case NOTE_FACE:
					case NOTE_BIG_FACE:
					case NOTE_SIDE:
					case NOTE_BIG_SIDE: {
						boolean hasHit = hit!=HIT_NONE;
						boolean isExpectedHit = false;
						boolean isBig = false;
						if (hasHit) {
							isExpectedHit = 
									((noteValue==NOTE_FACE || noteValue==NOTE_BIG_FACE) && hit==HIT_FACE) ||
									((noteValue==NOTE_SIDE || noteValue==NOTE_BIG_SIDE) && hit==HIT_SIDE);
							if (isExpectedHit) {
								isBig = noteValue==NOTE_BIG_FACE || noteValue==NOTE_BIG_SIDE;
							}
						}
						int handleResult = handleNormalNoteHit(hasHit, isExpectedHit, isBig, diff);
						switch (handleResult) {
						case NORMAL_NOTE_RESULT_DO_NOTHING:
							return true;
						case NORMAL_NOTE_RESULT_REMOVE_NOTE:
						case NORMAL_NOTE_RESULT_REMOVE_MISSED_NOTE:
							if (index < actionNotes.length - 1) {
								playerMessage.actionNoteIndex++;
							} else {
								walkToNextActionNoteBar();
							}
							if (handleResult==NORMAL_NOTE_RESULT_REMOVE_MISSED_NOTE) {
								continue;
							} else {
								return true;
							}
						}
						break;
					}
					
					case NOTE_LENDA:
//						assert playerMessage.rollingState == ROLLING_NONE; 
						playerMessage.rollingState = ROLLING_LENDA_BAR;
						playerMessage.rollingCount = 0;
						break;
						
					case NOTE_BIG_LENDA:
//						assert playerMessage.rollingState == ROLLING_NONE;
						playerMessage.rollingState = ROLLING_BIG_LENDA_BAR;
						playerMessage.rollingCount = 0;
						break;
						
					case NOTE_BALLOON:
//						assert playerMessage.rollingState == ROLLING_NONE;
						playerMessage.rollingState = ROLLING_BALLOON;
						playerMessage.rollingCount = mCourse.balloons[++mRollingBaloonIndex];
						break;
						
					case NOTE_POTATO:
//						assert playerMessage.rollingState == ROLLING_NONE;
						playerMessage.rollingState = ROLLING_POTATO;
						playerMessage.rollingCount = mCourse.balloons[++mRollingBaloonIndex];
						break;
						
					case NOTE_ROLLING_END:
//						assert playerMessage.rollingState != ROLLING_NONE;
						if (diff>=0) {
							int rollingState = playerMessage.rollingState;
							int rollintCount = playerMessage.rollingCount;
							if ( rollingState==ROLLING_BALLOON || rollingState==ROLLING_POTATO) {
								if (rollintCount > 0) {
									playerMessage.rollingCount = rollingState==ROLLING_BALLOON ?
											SPECIAL_ROLLING_COUNT_BALLOON_FAILED : SPECIAL_ROLLING_COUNT_POTATO_FAILED;
								}
							} else {
								playerMessage.rollingState = ROLLING_NONE;
							}
						} else if (hit!=HIT_NONE){
							int rollingState = playerMessage.rollingState;
							if (rollingState==ROLLING_LENDA_BAR ||
								rollingState==ROLLING_BIG_LENDA_BAR ||
								rollingState==ROLLING_BALLOON && hit==HIT_FACE ||
								rollingState==ROLLING_POTATO && hit==HIT_FACE) {
								playerMessage.handleRollingHit();
							}
						}
						break;
					}
				} else {
					// TODO
					// walkToNextActionNoteBar();
				}
				
			}
		}

		// TODO handles if mIsSectionArranging is true
		// TODO handles all commands until we reach a noteBar or the end  
		
		mLastOffset = currentOffset;
		// TODO tell whether we reach the end
		return true;
	}
	
	private void walkToNextActionNoteBar() {
		// TODO Walks from actionBarIndex to nextActionNoteBarIndex
	}
	
	
	private void handleEnteringNoteBar() {
		
		Bar[] currentBranch = mPlayerMessage.actionBranch;

		if (mIsSectionArranging) {
			mSectionStat.reset();
			mIsSectionArranging = false;
		}
		
		// Set mUpcomingStartBranchCommand if hasBranches
		mUpcomingStartBranchCommand = null;
		if (mCourse.hasBranches) {
			for (int index = mPlayerMessage.actionNoteBarIndex + 1;
					index < currentBranch.length && ! currentBranch[index].isNoteBar;
					index ++) {
				if (currentBranch[index].command.commandValue == TJANotation.COMMAND_STARTBRANCH) {
					mUpcomingStartBranchCommand = (StartBranchCommand) currentBranch[index].command;
					break;
				}
			}
		}
		
		// set next action bar
		mPlayerMessage.nextBranch = nextBranch(false);
	}
	
	/**
	 * Handles the hit on NOTE_FACE/NOTE_SIDE and their big case. This method
	 * only marks GOOD/NORMAL/BAD, adds score, adds/reduces gauge, and does
	 * stat. Called by {@link #onEvent(long, int)}.
	 * 
	 * @param hasHit
	 * @param isExptectedHit
	 * @param isBig
	 * @param diff
	 * @return {@link #NORMAL_NOTE_RESULT_DO_NOTHING},
	 *         {@link #NORMAL_NOTE_RESULT_REMOVE_NOTE},
	 *         {@link #NORMAL_NOTE_RESULT_REMOVE_MISSED_NOTE}
	 */
	private int handleNormalNoteHit(boolean hasHit, boolean isExptectedHit, boolean isBig, int diff) {
		PlayerMessage playerMessage = mPlayerMessage;
		int absDiff = Math.abs(diff);
		mSectionStat.precisionTotal += 2;
		
		// Dealing special case - not hit 
		if (!hasHit) {
			if (absDiff<=TIME_JUDGE_BAD) {
				playerMessage.noteJudged = JUDGED_NONE;
				return NORMAL_NOTE_RESULT_DO_NOTHING;
			}
			// mark MISSED, minus gauge, and stat
			playerMessage.noteJudged = JUDGED_MISSED;
			playerMessage.handleBadOrMissedHit(mGaugePerNote);
			return NORMAL_NOTE_RESULT_REMOVE_MISSED_NOTE;
		}
		
		// We are here and hasHit is true
		if (diff>TIME_JUDGE_BAD) {
			// Not reach yet
			playerMessage.noteJudged = JUDGED_NONE;
			return NORMAL_NOTE_RESULT_DO_NOTHING;
		} else if (absDiff<=TIME_JUDGE_GOOD){
			if (isExptectedHit) {
				// mark GOOD, add score, gauge, and stat
				playerMessage.noteJudged = JUDGED_GOOD;
				playerMessage.handleExpectedHit(true, isBig, mGaugePerNote, mScorePerNote);
				mSectionStat.score = playerMessage.score;
				mSectionStat.precisionPlayed += 2;
			} else {
				// mark BAD, minus gauge, and stat
				playerMessage.noteJudged = JUDGED_BAD;
				playerMessage.handleBadOrMissedHit(mGaugePerNote);
			}
		} else if (absDiff<=TIME_JUDGE_NORMAL){
			if (isExptectedHit) {
				// mark NORMAL, add score, gauge, and stat
				playerMessage.noteJudged = JUDGED_NORMAL;
				playerMessage.handleExpectedHit(false, isBig, mGaugePerNote, mScorePerNote);
				mSectionStat.score = playerMessage.score;
				mSectionStat.precisionPlayed ++;
			} else {
				// mark BAD, minus gauge, and stat
				playerMessage.noteJudged = JUDGED_BAD;
				playerMessage.handleBadOrMissedHit(mGaugePerNote);
			}
		} else if (absDiff<=TIME_JUDGE_BAD){
			// mark BAD, minus gauge, and stat
			playerMessage.noteJudged = JUDGED_BAD;
			playerMessage.handleBadOrMissedHit(mGaugePerNote);
		} else {
			// Too lagged if we reach here
			// mark MISSED, minus gauge, and stat.
			Log.w(TAG, "Too lagged");
			playerMessage.noteJudged = JUDGED_MISSED;
			playerMessage.handleBadOrMissedHit(mGaugePerNote);
		}
		return NORMAL_NOTE_RESULT_REMOVE_NOTE;
	}

	/**
	 * Computes the next note branch of specified note bar index
	 * 
	 * @param areBarNotesPassed
	 *            Indicates whether the notes of the current bar are all passed.
	 * @return
	 */
	private Bar[] nextBranch(boolean areBarNotesPassed) {
		// If the course has no branch
		if ( ! mCourse.hasBranches ) {
			return mNotation.normalBranch;
		}

		// If #BRANCHSTART is not encountered
		if ( mUpcomingStartBranchCommand == null ) {
			return mPlayerMessage.actionBranch;
		}

		// If the course has branches
		int branchIndex = selectBranch(mUpcomingStartBranchCommand, mSectionStat, areBarNotesPassed);
		switch (branchIndex) {
		case BRANCH_INDEX_NONE:
			return null;
		case BRANCH_INDEX_NORMAL:
			return mNotation.normalBranch;
		case BRANCH_INDEX_EASY:
			return mNotation.easyBranch;
		case BRANCH_INDEX_MASTER:
			return mNotation.masterBranch;
		default:
			return null;
		}
	}
	
	private static final int selectBranch(StartBranchCommand startBranchCommand,
			SectionStat sectionStat, boolean areBarNotesPassed) {
		// N < E < M
		// Normal < Easy < Master !

		int limitE, limitM, played;
		BranchJudge branchJudge = startBranchCommand.branchJudge;
		int judgeType = startBranchCommand.branchJudge.judgeType;
		switch (judgeType) {
		case TJANotation.BRANCH_JUDGE_ROLL:
			limitE = ((BranchJudgeRoll)branchJudge).easyRollingCount;
			limitM = ((BranchJudgeRoll)branchJudge).masterRollingCount;
			played = sectionStat.numRolled;
			break;

		case TJANotation.BRANCH_JUDGE_SCORE:
			limitE = ((BranchJudgeScore)branchJudge).easyScore;
			limitM = ((BranchJudgeScore)branchJudge).masterScore;
			played = sectionStat.score;
			break;

		case TJANotation.BRANCH_JUDGE_PRECISION:
			limitE = (int) Math.ceil(sectionStat.precisionTotal
					* ((BranchJudgePrecision) branchJudge).easyBeatPrecision);
			limitM = (int) Math.ceil(sectionStat.precisionTotal
					* ((BranchJudgePrecision) branchJudge).masterBeatPrecision);
			played = sectionStat.precisionPlayed;
			break;

		default:
			return BRANCH_INDEX_NONE;
		}

		if (areBarNotesPassed) {
			// Normal, Easy, Master!
			if (played < limitE) {
				return BRANCH_INDEX_NORMAL;
			} else if (played < limitM) {
				return BRANCH_INDEX_EASY;
			} else {
				return BRANCH_INDEX_MASTER;
			}
		} else if (judgeType==TJANotation.BRANCH_JUDGE_ROLL || judgeType==TJANotation.BRANCH_JUDGE_SCORE){
			if (played>=limitM) {
				return BRANCH_INDEX_MASTER;
			}
		}
		return BRANCH_INDEX_NONE;
	}
//	/**
//	 * 
//	 * @param aTimeMillisSinceStarted
//	 *            The time in milliseconds since start() is called
//	 * @param aHit
//	 *            one of HIT_NONE, HIT_FACE and HIT_SIDE
//	 * @return true if and only if the playing is not finished.
//	 */
//	public final boolean onEvent(long aTimeMillisSinceStarted, int aHit) {
//		// There is no more bar to play!
//		if (!mBars[mLastPlayingBarIndex].preprocessed) {
//			// It's time to mark end time
//			if (0 == mEndTimeMillis)
//				mEndTimeMillis = aTimeMillisSinceStarted;
//			// The time has passed FIXED_END_TIME_OFFSET since the mark of end
//			// time
//			else if (mEndTimeMillis - aTimeMillisSinceStarted > FIXED_END_TIME_OFFSET)
//				return false;
//		}
//
//		PlayerMessage playerMessage = mPlayerMessage;
//
//		// The first not reached yet
//		// if (aTimeMillisSinceStarted + JUDGE_MISSED < iStartOffsetTimeMillis)
//		// return true;
//
//		/**
//		 * The time in milliseconds since first bar
//		 */
//		long currentTimeMillisSinceFirstBar = aTimeMillisSinceStarted
//				- mStartOffsetTimeMillis;
//
//		int barIndex = mLastPlayingBarIndex;
//		int noteIndex = mLastPlayingNoteIndex;
//		for (; mBars[barIndex].preprocessed; barIndex = nextIndexOfBar(barIndex)) {
//			Bar playingBar = mBars[barIndex];
//
//			// Process the iUnprocessedCommand here
//			processUnprocessedCommands(playingBar, playerMessage, mSectionStat);
//
//			// Process the notes
//			noteIndex = handleNotes(playingBar, noteIndex,
//					currentTimeMillisSinceFirstBar, aHit);
//
//			if (noteIndex >= playingBar.notes.length) {
//				// Set the bar unused
//				playingBar.preprocessed = false;
//				noteIndex = 0;
//			} else {
//				break;
//			}
//		}
//
//		if (mLastPlayingBarIndex != barIndex) {
//			mLastPlayingBarIndex = barIndex;
//			while (PlayPreprocessor.PROCESS_RESULT_OK == tryPreprocessNextBar())
//				;
//		}
//
//		mLastPlayingNoteIndex = noteIndex;
//
//		if (mLastEventTimeMillis < currentTimeMillisSinceFirstBar)
//			mLastEventTimeMillis = currentTimeMillisSinceFirstBar;
//
//		mPlayerMessage.notePosCount = translateNotePos(
//				mPlayerMessage.notePosArray, mLastEventTimeMillis * 1000,
//				mBars, mLastPlayingBarIndex);
//
//		return true;
//	}
//
//	private final int handleNotes(Bar aPlayingBar, int aNoteIndex,
//			long aCurrentTimeMillisSinceFirstBar, int aHit) {
//		PlayerMessage playerMessage = mPlayerMessage;
//		PreprocessedNote[] notes = aPlayingBar.notes;
//		long playingBarStartTimeMillis = aPlayingBar.offsetTimeMicros / 1000;
//		long currentTimeMillisSincePlayBar = aCurrentTimeMillisSinceFirstBar
//				- playingBarStartTimeMillis;
//
//		// Traverse all note
//		// Cannot use the statement 'for(note:playingBar)' since we need the
//		// index
//		for (; aNoteIndex < notes.length; ++aNoteIndex) {
//			PreprocessedNote note = notes[aNoteIndex];
//			int noteType = note.noteType;
//
//			// Ignore all notes if the rolling is not going to be stopped
//			if (playerMessage.rollingState > ROLLING_NONE
//					&& noteType != NOTE_STOP_ROLLING) {
//				noteType = NOTE_NONE;
//			}
//
//			/**
//			 * The event time offset of current event time since current note
//			 * time
//			 */
//			long eventOffset = currentTimeMillisSincePlayBar
//					- note.offsetTimeMillis;
//
//			/**
//			 * Indicates if the current note has passed Used only for rolling
//			 * notes/states
//			 */
//			boolean noteHasPassed = eventOffset >= 0; /*
//													 * + (NOTE_FACE<=noteType &&
//													 * noteType<=NOTE_BIG_SIDE ?
//													 * JUDGE_MISSED : 0);
//													 */
//
//			// In all cases 'handled' will be true if 'noteHasPassed' is true
//			// In most cases 'handled' will be false if 'noteHasPassed' is false
//			// NOTE_BARLINE or NOTE_NONE is not taken into account
//			// The following code will handle the exceptions
//			boolean handled = false;
//
//			switch (noteType) {
//			case NOTE_BARLINE:
//			case NOTE_NONE:
//				break;
//
//			case NOTE_STOP_ROLLING:
//				handled = noteHasPassed
//						|| handleNoteTypeStopRolling(playerMessage, note,
//								noteHasPassed);
//				break;
//
//			case NOTE_START_ROLLING_LENDA:
//				if (noteHasPassed) {
//					playerMessage.rollingCount = 0;
//					playerMessage.rollingState = ROLLING_LENDA_BAR;
//					note.noteType = NOTE_NONE;
//					handled = true;
//				}
//				break;
//
//			case NOTE_START_ROLLING_BIG_LENDA:
//				if (noteHasPassed) {
//					playerMessage.rollingCount = 0;
//					playerMessage.rollingState = ROLLING_BIG_LENDA_BAR;
//					note.noteType = NOTE_NONE;
//					handled = true;
//				}
//				break;
//
//			case NOTE_START_ROLLING_BALOON:
//				if (noteHasPassed) {
//					playerMessage.rollingCount = mCourse.balloons[++mRollingBaloonIndex];
//					playerMessage.rollingState = ROLLING_BALLOON;
//					note.noteType = NOTE_NONE;
//					handled = true;
//				}
//				break;
//
//			case NOTE_START_ROLLING_POTATO:
//				if (noteHasPassed) {
//					playerMessage.rollingCount = mCourse.balloons[++mRollingBaloonIndex];
//					playerMessage.rollingState = ROLLING_POTATO;
//					note.noteType = NOTE_NONE;
//					handled = true;
//				}
//				break;
//
//			case NOTE_FACE:
//			case NOTE_SIDE:
//			case NOTE_BIG_FACE:
//			case NOTE_BIG_SIDE:
//				handled = handleNoteTypeFaceOrSide(playerMessage, note,
//						eventOffset, mSectionStat, aHit, mGaugePerNote,
//						mScorePerNote, mScoreInit, mScoreDiff);
//				break;
//			}
//
//			if (!handled) {
//				if (playerMessage.rollingState > ROLLING_NONE) {
//					switch (playerMessage.rollingState) {
//					case ROLLING_LENDA_BAR:
//						if (HIT_NONE != aHit) {
//							playerMessage.addedScore = 300;
//							playerMessage.score += 300;
//							playerMessage.numTotalRolled++;
//							playerMessage.rollingCount++;
//						}
//						break;
//
//					case ROLLING_BIG_LENDA_BAR:
//						if (HIT_NONE != aHit) {
//							playerMessage.addedScore = 600;
//							playerMessage.score += 600;
//							playerMessage.numTotalRolled++;
//							playerMessage.rollingCount++;
//						}
//						break;
//
//					case ROLLING_BALLOON:
//						if (HIT_FACE == aHit) {
//							playerMessage.rollingCount--;
//
//							playerMessage.numTotalRolled++;
//							if (0 == playerMessage.rollingCount) {
//								playerMessage.addedScore = 3300;
//								playerMessage.score += 3300;
//								playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_BALLOON_FINISHED;
//							} else {
//								playerMessage.addedScore = 300;
//								playerMessage.score += 300;
//							}
//						}
//						break;
//
//					case ROLLING_POTATO:
//						if (HIT_FACE == aHit) {
//							playerMessage.rollingCount--;
//
//							playerMessage.numTotalRolled++;
//							if (0 == playerMessage.rollingCount) {
//								playerMessage.addedScore = 3300;
//								playerMessage.score += 3300;
//								playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_POTATO_FINISHED;
//							} else {
//								playerMessage.addedScore = 300;
//								playerMessage.score += 300;
//							}
//						}
//						break;
//
//					}
//				}
//
//				// Exit here, for note if no actual note handled
//				break;
//			}
//		}
//		return aNoteIndex;
//	}
//
//	/**
//	 * Handles case when note.iNoteType == NOTE_STOP_ROLLING
//	 * 
//	 * @param playerMessage
//	 * @param note
//	 * @param noteHasPassed
//	 * @return true if handled, false otherwise.
//	 */
//	private static final boolean handleNoteTypeStopRolling(
//			PlayerMessage playerMessage, PreprocessedNote note,
//			boolean noteHasPassed) {
//		switch (playerMessage.rollingState) {
//		case ROLLING_NONE: // Finished rolling (balloon/potato)
//			// This case happens when
//			// The balloon/potato finished rolling before NOTE_STOP_ROLLING
//			note.noteType = NOTE_NONE;
//			return true;
//
//		case ROLLING_NONE_LENDA: // Finished rolling (len-da)
//		case ROLLING_NONE_BIG_LENDA: // Finished rolling (big len-da)
//			// This case happens when the len-da bar has passed
//			// Cannot set note to NOTE_NONE until it exit the screen
//			if (note.offsetPos < -BEAT_DIST) {
//				playerMessage.rollingState = ROLLING_NONE;
//				note.noteType = NOTE_NONE;
//				return true;
//			}
//			break;
//
//		default: // Still rolling
//			if (noteHasPassed)
//			// This case means the rolling time passed
//			{
//				switch (playerMessage.rollingState) {
//				case ROLLING_LENDA_BAR:
//					playerMessage.rollingState = ROLLING_NONE_LENDA;
//					break;
//
//				case ROLLING_BIG_LENDA_BAR:
//					playerMessage.rollingState = ROLLING_NONE_BIG_LENDA;
//					break;
//
//				case ROLLING_BALLOON:
//					playerMessage.rollingState = ROLLING_NONE;
//					if (playerMessage.rollingCount > 0)
//						playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_BALLOON_FAILED;
//					break;
//
//				case ROLLING_POTATO:
//					playerMessage.rollingState = ROLLING_NONE;
//					if (playerMessage.rollingCount > 0)
//						playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_POTATO_FAILED;
//					break;
//				}
//
//				note.noteType = NOTE_NONE;
//				return true;
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * Handles case when aNote.iNoteType ==
//	 * NOTE_FACE/NOTE_SIDE/NOTE_BIG_FACE/NOTE_BIG_SIDE
//	 * 
//	 * @param aPlayerMessage
//	 * @param aNote
//	 * @param anEventOffset
//	 * @param aSectionStat
//	 * @param aHit
//	 * @param aGaugePerNote
//	 * @param aScorePerNote
//	 * @param aScoreInit
//	 * @param aScoreDiff
//	 * @return
//	 */
//	private static final boolean handleNoteTypeFaceOrSide(
//			PlayerMessage aPlayerMessage, PreprocessedNote aNote,
//			long anEventOffset, SectionStat aSectionStat, int aHit,
//			int[] aGaugePerNote, int[][] aScorePerNote, int aScoreInit,
//			int aScoreDiff) {
//		if (anEventOffset > TIME_JUDGE_NORMAL) {
//			// Handling cases MISSED and BREAK
//
//			// Is it a BREAK?
//			boolean isBreak = anEventOffset > TIME_JUDGE_MISSED;
//
//			// I try to make it work compatible with lag case
//			// So we don't think it is a MISSED even
//			// if there is no hit within TIME_JUDGE_NORMAL.
//			// However if event(hit or no hit) > TIME_JUDGE_MISSED
//			// or there is a hit within (TIME_JUDGE_NORMAL, TIME_JUDGE_MISSED]
//			// we can conclude that it is a BREAK or MISSED
//			if (isBreak || HIT_NONE != aHit) {
//				aPlayerMessage.noteJudged = isBreak ? PlayerMessage.JUDGED_BREAK
//						: PlayerMessage.JUDGED_MISSED;
//				aSectionStat.precisionTotal += 2;
//
//				aPlayerMessage.gauge -= aGaugePerNote[GAUGE_OR_SCORE_INDEX_TWICE]; // Gauge
//																					// reduce
//																					// twice
//				if (aPlayerMessage.gauge < 0)
//					aPlayerMessage.gauge = 0;
//
//				aPlayerMessage.numCombos = 0;
//				aPlayerMessage.numTotalNotes++;
//
//				aNote.noteType = NOTE_NONE;
//
//				return true;
//			}
//		} else {
//			int noteType = aNote.noteType;
//
//			if (HIT_FACE == aHit
//					&& (NOTE_FACE == noteType || NOTE_BIG_FACE == noteType)
//					|| HIT_SIDE == aHit
//					&& (NOTE_SIDE == noteType || NOTE_BIG_SIDE == noteType)) {
//				// Handling cases GOOD and NORMAL
//
//				// It is good only hit correctly within TIME_JUDGE_GOOD
//				boolean isGood = anEventOffset <= TIME_JUDGE_GOOD;
//
//				// Judge
//				aPlayerMessage.noteJudged = PlayerMessage.JUDGED_NORMAL;
//
//				// SECTION statistics
//				aSectionStat.precisionTotal += 2;
//				aSectionStat.precisionPlayed += isGood ? 2 : 1;
//
//				// For both gauge and score
//				int addedShifts = 0;
//				if (isGood)
//					addedShifts++;
//
//				if (NOTE_BIG_FACE == noteType || NOTE_BIG_SIDE == noteType)
//					addedShifts++;
//
//				// Gauge
//				int gauge = aPlayerMessage.gauge;
//				if (gauge < MAX_GAUGE) {
//					gauge += aGaugePerNote[addedShifts];
//					if (gauge > MAX_GAUGE)
//						gauge = MAX_GAUGE;
//				}
//				aPlayerMessage.gauge = gauge;
//
//				// Combo counter
//				if (aPlayerMessage.numCombos++ == aPlayerMessage.numMaxCombos)
//					aPlayerMessage.numMaxCombos++;
//
//				// Scores
//				if (aPlayerMessage.numCombos <= 100
//						&& aPlayerMessage.numCombos % 10 == 0) {
//					int scorePerNote = aScoreInit + aScoreDiff
//							* (aPlayerMessage.numCombos / 10);
//					int scorePerNoteGGT = (int) (scorePerNote * 1.2f) / 10 * 10;
//
//					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_FULL] = scorePerNote;
//					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_FULL] = scorePerNoteGGT;
//
//					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = scorePerNote << 1;
//					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = scorePerNoteGGT << 1;
//
//					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((scorePerNote / 10) >> 1) * 10;
//					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((scorePerNoteGGT / 10) >> 1) * 10;
//				}
//
//				int ggtIndex = aPlayerMessage.isGGT ? SCORE_INDEX_GGT
//						: SCORE_INDEX_NOT_GGT;
//				aPlayerMessage.addedScore = aScorePerNote[ggtIndex][addedShifts];
//				aPlayerMessage.score += aPlayerMessage.addedScore;
//
//				// Note counter
//				aPlayerMessage.numTotalNotes++;
//				if (isGood)
//					aPlayerMessage.numGoodNotes++;
//				else
//					aPlayerMessage.numNormalNotes++;
//
//				aNote.noteType = NOTE_NONE;
//
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//	public final String playTimeError() {
//		return mPlayTimeError;
//	}
//
//	private final int tryPreprocessNextBar() {
//		int preprocessedBarIndex = mPreprocessedBarIndexRef.value;
//		if (-1 == preprocessedBarIndex
//				|| !mBars[preprocessedBarIndex].hasBranchStartNextBar) {
//			// No #BRANCHSTART in next bar
//
//			return mPreprocessor.processNextBar(mBars, mNotation,
//					mPreprocessedCommandIndexRef, mPreprocessedBarIndexRef,
//					mBranchExitIndexRef);
//		} else {
//			Bar playingBar = mBars[preprocessedBarIndex];
//			PlayerMessage playerMessage = mPlayerMessage;
//			// The last unprocessed branch is always
//			// #BRANCHSTART
//			@SuppressWarnings("unused")
//			TJACommand cmdBranchStart = playingBar.unprocessedCommand[playingBar.unprocessedCommand.length - 1];
//			// TODO Handle case: #BRANCHSTART in next bar
//			// TODO May change mPreprocessedCommandIndex
//			// TODO May change mPreprocessedBarIndex
//			// TODO May add a virtual pre-processed bar
//
//			processUnprocessedCommands(playingBar, playerMessage, mSectionStat);
//
//			return PlayPreprocessor.PROCESS_RESULT_OK;
//		}
//	}
//
//	private static final void processUnprocessedCommands(Bar aPlayingBar,
//			PlayerMessage aPlayerMessage, SectionStat aSectionStat) {
//		for (TJACommand cmd : aPlayingBar.unprocessedCommand) {
//			switch (cmd.commandType) {
//			case TJAFormat.COMMAND_TYPE_GOGOSTART:
//				aPlayerMessage.isGGT = true;
//				break;
//
//			case TJAFormat.COMMAND_TYPE_GOGOEND:
//				aPlayerMessage.isGGT = false;
//				break;
//
//			case TJAFormat.COMMAND_TYPE_SECTION:
//				aSectionStat.reset();
//				break;
//
//			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
//				// Ignored
//			}
//		}
//
//	}

	private final void resetGauge(int totalGaugeNotes) {
		int course = mCourse.courseIndex;
		int level = Math.min(mCourse.level, MAX_LEVEL_OF[course]);

		int gauge = (int) (MAX_GAUGE / (MAX_GAUGE_RATES[course][level] * totalGaugeNotes));

		if ((gauge & 1) == 1)
			--gauge;

		mGaugePerNote[GS_INDEX_FULL] = gauge;
		mGaugePerNote[GS_INDEX_HALF] = gauge >> 1;
		mGaugePerNote[GS_INDEX_TWICE] = gauge << 1;
	}

	private final void resetScores(float totalScoringNotes) {
		TJACourse course = mCourse;
		if (course.scoreInit > 0 && course.scoreDiff > 0) {
			mScoreInit = course.scoreInit;
			mScoreDiff = course.scoreDiff;
		} else {
			int fullScore; // approximate value of full score
			if (course.level <= MAX_LEVEL_OF[course.courseIndex])
				fullScore = FULL_SCORES[course.courseIndex][course.level];
			else
				fullScore = FULL_SCORES[course.courseIndex][MAX_LEVEL_OF[course.courseIndex]]
						+ 100000 * (MAX_LEVEL_OF[course.courseIndex] - course.level);
			float fullNormalNote = (float) fullScore / totalScoringNotes;
			mScoreInit = (int) Math.floor(fullNormalNote * 0.08f) * 10;
			mScoreDiff = (int) Math.floor(fullNormalNote * 0.02f) * 10;
		}
	}

	/**
	 * Get the total number of notes 1,2,3,4 for score computation and the total gauge <br>
	 * Choose the branch with maximum scored note if encounters branches.<br>
	 * The score scales in extra cases: <br>
	 * <ul>
	 * <li> +20% if in GGT </li>
	 * <li> Half if note index less than 100</li>
	 * <li> Doubled if the note is "big" </li>
	 * </ul>
	 * @param notation
	 * @param totalGaugeNotesRef
	 * @return
	 */
	private static final float computeScoringNotesAndGaugeNotes(
			TJANotation notation, AtomicInteger totalGaugeNotesRef) {
		TJANotation.Bar[] maxBranch = notation.normalBranch;
		float maxScoringNotes = computeScoringNotes(maxBranch);
		if (notation.easyBranch!=null) {
			float scoringNotes = computeScoringNotes(notation.easyBranch);
			if (maxScoringNotes < scoringNotes) {
				maxScoringNotes = scoringNotes;
				maxBranch = notation.easyBranch;
			}
			scoringNotes = computeScoringNotes(notation.masterBranch);
			if (maxScoringNotes < scoringNotes) {
				maxScoringNotes = scoringNotes;
				maxBranch = notation.masterBranch;
			}
		}
		totalGaugeNotesRef.set(computeGaugeNotes(maxBranch));
		return maxScoringNotes;
	}
	
	private static final float computeScoringNotes(TJANotation.Bar[] branch) {
		float scoredNotes = 0;
		int numNotes = 0;
		int len = branch.length;
		int i;

		boolean inGGT = false;

		for (i = 0; i < len;) {
			Bar bar = branch[i];
			if (!bar.isNoteBar) {
				switch (bar.command.commandValue) {
				case TJANotation.COMMAND_GOGOSTART:
					inGGT = true;
					break;
				case TJANotation.COMMAND_GOGOEND:
					inGGT = false;
					break;

				default:
					continue;
				}
			} else {
				for (Note note : bar.noteBar.notes) {
					switch (note.noteValue) {
					case TJANotation.NOTE_FACE:
					case TJANotation.NOTE_SIDE:
						scoredNotes += numNotes < 100 ? 0.5f : 1.0f;
						if (inGGT)
							scoredNotes += numNotes < 100 ? .1f : .2f;
						++numNotes;
						break;

					case TJANotation.NOTE_BIG_FACE:
					case TJANotation.NOTE_BIG_SIDE:
						scoredNotes += numNotes < 100 ? 1.0f : 2.0f;
						if (inGGT)
							scoredNotes += numNotes < 100 ? .2f : .4f;
						++numNotes;
						break;

					default:
						;
					}
				}
			}			

			i++;
		}
		return scoredNotes;		
	}

	/**
	 * Get the total number of notes 1,2,3,4 for gauge of the specified branch.
	 * 
	 * @return
	 */
	private static final int computeGaugeNotes(TJANotation.Bar[] branch) {
		int gaugeNotes = 0;

		for (TJANotation.Bar bar:branch) {
			if (!bar.isNoteBar) {
				continue;
			}
			for (TJANotation.Note note : bar.noteBar.notes) {
				switch (note.noteValue) {
				case NOTE_FACE:
				case NOTE_SIDE:
				case NOTE_BIG_FACE:
				case NOTE_BIG_SIDE:
					++gaugeNotes;
					break;
				}
			}
		}
		return gaugeNotes;
	}

//	/**
//	 * Translate the playing bars into the display purpose NotePos
//	 * 
//	 * @param aNotePos
//	 *            [out]
//	 * @param aCurrentTimeMicros
//	 * @param aBars
//	 * @param aPlayingBarIndex
//	 * @return The number of translated note positions
//	 */
//	private static final int translateNotePos(PlayerMessage.NotePos[] aNotePos,
//			long aCurrentTimeMicros, Bar[] aBars, int aPlayingBarIndex) {
//		int notePosCount = 0;
//		for (int barIndex = aPlayingBarIndex; aBars[barIndex].preprocessed; barIndex = nextIndexOfBar(barIndex)) {
//			Bar bar = aBars[barIndex];
//			// bar.iNotes may be null
//			if (null == bar.notes)
//				continue;
//
//			// Cache some value to speed up
//			long barRuntimeOffset = aCurrentTimeMicros - bar.offsetTimeMicros;
//			long barSpeed = bar.speed * 1000;
//
//			// FIXME Please help to improve the performance
//			// of computing notePos.iNotePos
//			for (PreprocessedNote pnote : bar.notes) {
//				if (NOTE_NONE == pnote.noteType)
//					continue;
//				PlayerMessage.NotePos notePos = aNotePos[notePosCount++];
//				notePos.noteType = pnote.noteType;
//				notePos.notePos = (int) (barRuntimeOffset / barSpeed);
//			}
//		}
//
//		return notePosCount;
//	}

	private static void safeSetIntegerRef(IntegerRef ref, int value) {
		if (ref==null) {
			return;
		}
		ref.value = value;
	}
}
