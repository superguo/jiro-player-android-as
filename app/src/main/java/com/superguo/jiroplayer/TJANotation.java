package com.superguo.jiroplayer;

/**
 * The compiled TJA notation optimized for play time. <br>
 * So some TJA commands mentioned in {@link TJAFormat}  are remade. <br>
 * And some are eliminated: 
 * <ul>
 * <li>#MEASURE</li>
 * <li>#DELAY</li>
 * <li>#SCROLL</li>
 * <li>#N</li>
 * <li>#E</li>
 * <li>#M</li>
 * <li>#BPMCHANGE</li>
 * </ul>
 * And the following commands are added:
 * <ul>
 * <li>note commands</li>
 * <li>#GOTO</li>
 * </ul>
 * @author superguo
 */
public final class TJANotation {
	
	public final static int NOTE_EMPTY		= 0;
	public final static int NOTE_FACE		= 1;
	public final static int NOTE_SIDE		= 2;
	public final static int NOTE_BIG_FACE	= 3;
	public final static int NOTE_BIG_SIDE	= 4;
	public final static int NOTE_LENDA		= 5;
	public final static int NOTE_BIG_LENDA	= 6;
	public final static int NOTE_BALLOON	= 7;
	public final static int NOTE_POTATO		= 9;
//	public final static int NOTE_ROLLING_END = 8;

	public final static int COMMAND_EMPTY		= 0;
	public final static int COMMAND_GOGOSTART	= 1;
	public final static int COMMAND_GOGOEND		= 2;
	public final static int COMMAND_SECTION		= 3;
	public final static int COMMAND_STARTBRANCH	= 4;
//	public final static int COMMAND_EXITBRANCH	= 5;
	public final static int COMMAND_LEVEL_HOLD	= 6;
	
	/** Judge type rolling */
	public static final int BRANCH_JUDGE_ROLL 		= 0;
	
	/** Judge type precision */
	public static final int BRANCH_JUDGE_PRECISION 	= 1;
	
	/** Judge type score */
	public static final int BRANCH_JUDGE_SCORE 		= 2;
	
	/** The start time for the music to play since the beginning of the game */
	public long musicStartMillis;
	
	/** The end time */
	public long endMillis;

	/** The normal branch for branched notation; otherwise it is the only branch */
	public Bar[] normalBranch;
	
	/** null if no branches exist */
	public Bar[] easyBranch;
	
	/** null if no branches exist */
	public Bar[] masterBranch;
	
	public static final class Bar {
		/** Indicates that whether it is a NoteBar or CommandBar.
		 * If it is set true, noteBar is not null and commandBar is null.
		 * Otherwise commandBar is not null and noteBar is null.<br>
		 * I don't make noteBar inherit {@link #CommandBar} to reduce 
		 * the class cast consuming.  */
		public boolean isNoteBar;
		
		/** Not null if {@link #isNoteBar} is true; null if {@link #isNoteBar} is false */
		public NoteBar noteBar;
		
		/** Not null if {@link #isNoteBar} is false; null if {@link #isNoteBar} is true */
		public Command command;
	}
	
	public static final class NoteBar {
		/** The start time when the bar enters the center 
		 * of the beat since the beginning of the game */
		public long beatMillis;

		/** The time when the bar appears in the screen 
		 * to player, always earlier than beatTimeMillis */
		public long appearMillis;

		/** Indicates whether the bar line is visible */
		public boolean isBarLineOn;
		
		/** The pre-computed x coordinations of the bar of every millisecond
		 * relative to {@link #appearMillis}
		 */
		public short millisDistances[];
		
		/** The bar's width in pixels */
		public int width;

		/** The notes */
		public Note[] notes;
		
		/** The index of bar that is its next note bar */
		public int nextNoteBarIndex = -1;
	}

	public static final class Note {
		/** The note value */
		public int noteValue;

		/** The beat time since the beginning of the game */
		public long beatMillis;

		/**
		 * Start time to handle i.e. beatTimeMillis - TIME_JUDGE_BAD for normal
		 * note, and beatTimeMillis for rolling notes
		 */
		public long handleStartMillis;

		/**
		 * End time to handle i.e. beatTimeMillis + TIME_JUDGE_BAD for normal
		 * note
		 */
		public long handleEndMillis;

		/** The distance relative to the beginning of its note bar */
		public int offsetX;

		/** For len-da note. The ending distance relative to the beginning of its note bar*/
		public int offsetX2;
	}
	
	public static class Command {
		/** The command value */
		public int commandValue;
		
		public Command(int value) {
			commandValue = value;
		}
	}
	
	public static class StartBranchCommand extends Command {

		public int normalIndex;
		public int easyIndex;
		public int masterIndex;
		public int exitIndex;
		public BranchJudge branchJudge;
		
		public StartBranchCommand() {
			super(COMMAND_STARTBRANCH);
		}
		
		public void compileTJAFormatArgs(int[] args) {
			normalIndex	= args[3];
			easyIndex	= args[4];
			masterIndex	= args[5];
			exitIndex	= args[6];
			switch (args[0]) {
			case TJAFormat.BRANCH_JUDGE_ROLL:
				branchJudge = new BranchJudgeRoll(args[1], args[2]);
				break;
			case TJAFormat.BRANCH_JUDGE_PRECISION:
				branchJudge = new BranchJudgePrecision(args[1], args[2]);
				break;
			case TJAFormat.BRANCH_JUDGE_SCORE:
				branchJudge = new BranchJudgeScore(args[1], args[2]);
				break;
			default:
				throw new RuntimeException("Invalid branch type " + args[0]);
			}
		}
		
		public static abstract class BranchJudge {
			
			/** Judge type, ranged in one of the following values:
			 * <ul>
			 * <li> {@link TJANotation#BRANCH_JUDGE_ROLL} </li>
			 * <li> {@link TJANotation#BRANCH_JUDGE_PRECISION} </li>
			 * <li> {@link TJANotation#BRANCH_JUDGE_SCORE} </li>
			 * </ul>
			 * */
			public int judgeType;
			
			public BranchJudge(int judgeType) {
				this.judgeType = judgeType;
			}
		}
		
		public static class BranchJudgeRoll extends BranchJudge {
			
			/** The minimum rolling count to reach EASY(玄人) class */
			public int easyRollingCount;
			
			/** The minimum rolling count to reach MASTER(達人) class */
			public int masterRollingCount;
			
			public BranchJudgeRoll(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_ROLL);
				easyRollingCount = tjaFormatArg1;
				masterRollingCount = tjaFormatArg2;
			}
			
		}
		
		public static class BranchJudgePrecision extends BranchJudge {

			/** The minimum beat precision in percentage
			 *  to reach EASY(玄人) class */
			public float easyBeatPrecision;
			
			/** The minimum beat precision in percentage
			 *  to reach MASTER(達人) class */
			public float masterBeatPrecision;
			
			public BranchJudgePrecision(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_PRECISION);
				easyBeatPrecision = Float.intBitsToFloat(tjaFormatArg1);
				masterBeatPrecision = Float.intBitsToFloat(tjaFormatArg2);
			}
		}
		
		public static class BranchJudgeScore extends BranchJudge {

			/** The minimum score to reach EASY(玄人) class */
			public int easyScore;
			
			/** The minimum score to reach MASTER(達人) class */
			public int masterScore;
			
			public BranchJudgeScore(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_SCORE);
				easyScore = tjaFormatArg1;
				masterScore = tjaFormatArg2;
			}
			
		}
	}
	
	
}
