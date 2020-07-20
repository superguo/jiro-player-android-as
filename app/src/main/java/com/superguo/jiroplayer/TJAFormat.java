package com.superguo.jiroplayer;

import java.io.*;

import com.superguo.jiroplayer.TJAFormatParser.TJAFormatParserException;

/**
 * The parsed TJA format.
 * @author superguo
 *
 */
public final class TJAFormat {
	// unsupported header: GAME, LIFE
	public static final int COURSE_EASY 	= 0;
	public static final int COURSE_NORMAL 	= 1;
	public static final int COURSE_HARD 	= 2;
	public static final int COURSE_ONI 		= 3;
	public static final int COURSE_EDIT 	= 4;
	public static final int COURSE_TOWER 	= 5;
	public static final int SIDE_NORMAL		= 1;
	public static final int SIDE_EX			= 2;
	public static final int SIDE_BOTH		= 3;
	public static final int BRANCH_JUDGE_ROLL 		= 0;
	public static final int BRANCH_JUDGE_PRECISION 	= 1;
	public static final int BRANCH_JUDGE_SCORE 		= 2;
	public static final int COMMAND_TYPE_NOTE		= 0; 	// iNotes
	public static final int COMMAND_TYPE_BPMCHANGE 	= 1; 	// iFloatArg
	public static final int COMMAND_TYPE_GOGOSTART 	= 2;
	public static final int COMMAND_TYPE_GOGOEND 	= 3;
	public static final int COMMAND_TYPE_MEASURE  	= 4; 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
	public static final int COMMAND_TYPE_SCROLL 	= 5; 	// float(0.1 - 16.0)
	public static final int COMMAND_TYPE_DELAY 		= 6; 	// float(>0.001)
	public static final int COMMAND_TYPE_SECTION 	= 7;
	public static final int COMMAND_TYPE_BRANCHSTART  = 8; 	// BRANCH_JUDGE_*(r/p/s, int), X(float), Y(float), #N index, #E index, #M index, exit index(may be invalid) 
	public static final int COMMAND_TYPE_BRANCHEND 	= 9;
	public static final int COMMAND_TYPE_N 			= 10;
	public static final int COMMAND_TYPE_E 			= 11;
	public static final int COMMAND_TYPE_M 			= 12;
	public static final int COMMAND_TYPE_LEVELHOLD 	= 13;
	public static final int COMMAND_TYPE_BARLINEOFF = 14;
	public static final int COMMAND_TYPE_BARLINEON 	= 15;

	// global
	public String 	title;
	public String 	subTitle;
	public int		side	= SIDE_BOTH;
	public String	wave;
	public float	offset;	// -5 ~ +5
	public float	demoStart = 0.0f;
	public float	songVol = 100.0f;	// 0 ~ 100, default 100
	public float	seVol = 100.0f;		// 0 ~ 100, default 100
	public boolean	bmScroll = false;
	public boolean	hbScroll = false;
	public TJACourse courses[];

	public static final class TJACourse {
		public int courseIndex = COURSE_ONI; //
		public int level; // 1 ~ 12
		public boolean hasBranches;
		public float BPM; // 50 ~ 250
		public int[] balloons; // number of balloons
		public int scoreInit; // 1 ~ 100000, 0 means auto
		public int scoreDiff; // 1 ~ 100000, 0 means auto
		public TJACommand[] notationSingle; // cannot be null if iStyle is
												// STYLE_SIGNLE
		public TJACommand[] notationP1; // cannot be null if iStyle is
											// STYLE_DOUBLE
		public TJACommand[] notationP2; // cannot be null if iStyle is
											// STYLE_DOUBLE
	}

	public static final class TJACommand {
		public int commandType;
		public int args[];

		public TJACommand(int commandType) {
			this.commandType = commandType;
		}

		@Override
		public TJACommand clone() {
			try {
				TJACommand copy = (TJACommand) super.clone();
				copy.args = args.clone();
				return copy;
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}

	public TJAFormat() {
	}

	public static TJAFormat fromReader(TJAFormatParser parser,
			BufferedReader reader) throws IOException, TJAFormatParserException {
		return parser.parse(reader);
	}
}
