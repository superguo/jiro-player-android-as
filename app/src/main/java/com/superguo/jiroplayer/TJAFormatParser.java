package com.superguo.jiroplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.superguo.jiroplayer.TJAFormat.*;

public final class TJAFormatParser
{
	public static final class TJAFormatParserException extends Exception {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = 375325974443057166L;

		public TJAFormatParserException(int lineNo, String line, String msg) {
			super("Line " + lineNo + " " + msg + "\n"
					+ (line == null ? "" : line));
		}
	
		public TJAFormatParserException(int lineNo, String line, Throwable r) {
			super("Line " + lineNo + "\n" + (line == null ? "" : line), r);
		}
	}

	// parse states
	private float 				  mBPM;
	private TJAFormat 			  mFormat;
	private int					  mLineNo;
	private String				  mLine;
	private LinkedList<TJACourse> mParsedCourses 	= new LinkedList<TJACourse>();
	private LinkedList<TJACommand>  mParsedCommands;
	private TJACourse 			  mParsingCourse;
	private IntBuffer			  mParsingNotes;
	private boolean				  mIsParsingDouble;
	private boolean				  mIsParsingP2;
	private boolean				  mIsStarted;
	private boolean				  mIsGoGoStarted;
	private boolean				  mIsBranchStarted;
	private TJACommand  mCommandOfStartedBranch;
	
	
	/**
	 * @param reader [in]
	 * @return 
	 * @throws TJAFormatParserException 
	 */
	public TJAFormat parse(BufferedReader reader)
			throws IOException, TJAFormatParserException {
		mFormat = new TJAFormat();
		mLineNo = 0;
		mParsingCourse = new TJACourse();
		mParsedCommands = new LinkedList<TJACommand>();
		mParsingNotes = IntBuffer.wrap(new int[500]);
		System.gc();

		for (mLine = reader.readLine(); mLine != null; mLine = reader.readLine()) {
			++mLineNo;
			mLine = tidy(mLine);
			if (mLine.length() == 0)
				continue;

			char firstChar = mLine.charAt(0);
			int colonPos;
			if (firstChar == '#') { // command begins with #
				if (mParsingNotes.position() > 0)
					emitNotes();

				mLine = mLine.toUpperCase(Locale.ENGLISH);
				parseCommandOfCurrentLine();
			} else if (Character.isDigit(firstChar)) { // 0-9, it is note
				parseNotesOfCurrentLine();
			} else if ((colonPos = mLine.indexOf(':')) >= 0) {
				try {
					if (mIsStarted) {
						throwEx("No header allowed after #START without #END");
					}

					if (mParsingNotes.position() > 0) {
						emitNotes();
					}

					setHeader(mLine.substring(0, colonPos).trim(), mLine
							.substring(colonPos + 1).trim());
				} catch (RuntimeException e) {
					throwEx(e);
				}
			} else {
				throwEx("Unknown header or command");
			}
		}

		if (mIsStarted) {
			throwEx("Missing #END");
		}

		if (mParsingCourse.notationSingle != null
				|| mParsingCourse.notationP1 != null
				&& mParsingCourse.notationP2 != null) {
			emitCourse();
		}

		if (mParsingCourse.notationP1 != null
				&& mParsingCourse.notationP2 == null) {
			throwEx("Missing #START P2");
		}

		if (mParsedCourses.size() == 0) {
			throwEx("Missing #START");
		} else {
			mFormat.courses = mParsedCourses
					.toArray(new TJACourse[mParsedCourses.size()]);
		}
		
		return mFormat;
	}

	private static String tidy(String iLine) {
		String r = iLine;
		int commentPos = r.indexOf("//");
		if (commentPos >= 0) {
			r = r.substring(0, commentPos);
		}
		return r.trim();
	}

	private void throwEx(String msg) throws TJAFormatParserException {
		throw new TJAFormatParserException(mLineNo, mLine, msg);
	}

	private void throwEx(Throwable r) throws TJAFormatParser.TJAFormatParserException {
		throw new TJAFormatParserException(mLineNo, mLine, r);
	}

	private void emitNotes() {
		if (mParsingNotes.position() <= 0)
			return;

		TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_NOTE);
		cmd.args = new int[mParsingNotes.position()];
		System.arraycopy(mParsingNotes.array(), 0, cmd.args, 0,
				mParsingNotes.position());
		mParsedCommands.add(cmd);
		mParsingNotes.clear();
	}

	private void parseCommandOfCurrentLine() throws TJAFormatParserException {
		String fields[];

		// judge if started
		if (!mIsStarted && !mLine.startsWith("#START")
				&& !mLine.equals("#BMSCROLL") && !mLine.equals("#HBSCROLL")) {
			throwEx("Must put #START before this command");
		}

		if (mLine.startsWith("#START")) {
			// can start?
			if (mIsStarted)
				throwEx("Cannot put #START here");
			fields = mLine.split("\\s");

			if (fields.length == 1) {
				if (mIsParsingDouble)
					throwEx("Must #START P1 here");
				if (mParsingCourse.notationSingle != null)
					throwEx("Cannot #START again");

				mIsStarted = true;
			} else if (fields.length == 2) {
				fields[1] = fields[1].trim();
				mIsParsingDouble = true;
				if (!mIsParsingDouble)
					throwEx("Must #START here");

				if (fields[1].equals("P1")) {
					if (mParsingCourse.notationP1 != null)
						throwEx("Cannot #START P1 again");
					mIsStarted = true;
					mIsParsingP2 = false;
				} else if (fields.length == 2 && fields[1].equals("P2")) {
					if (mParsingCourse.notationP1 == null)
						throwEx("Must #START P1 first");

					if (mParsingCourse.notationP2 != null)
						throwEx("Cannot #START P2 again");

					mIsStarted = true;
					mIsParsingP2 = true;
				} else
					throwEx("Unknown #START command");
			} else
				throwEx("Unknown #START command");
		} else if (mLine.equals("#END")) {
			if (!mIsStarted)
				throwEx("#END without #START");
			if (mIsGoGoStarted)
				throwEx("missing #GOGOEND");
			if (mIsBranchStarted) // #BRANCHSTART without #BRANCHEND
				emitBranch();
			emitNotation();
		} else if (mLine.startsWith("#BPMCHANGE")) {
			fields = mLine.split("\\s");
			if (fields.length != 2)
				throwEx("Unknown #BPMCHANGE command");
			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_BPMCHANGE);
			cmd.args = new int[1];
			float arg = Float.parseFloat(fields[1]);
			arg = (float) (Math.floor(arg * 1000) / 1000);
			cmd.args[0] = Float.floatToIntBits(arg);
			mParsedCommands.add(cmd);
		} else if (mLine.equals("#GOGOSTART")) {
			if (mIsGoGoStarted)
				throwEx("Already exist #GOGOSTART before #GOGOEND");
			mIsGoGoStarted = true;
			mParsedCommands
					.add(new TJACommand(TJAFormat.COMMAND_TYPE_GOGOSTART));
		} else if (mLine.equals("#GOGOEND")) {
			if (!mIsGoGoStarted)
				throwEx("Missing #GOGOSTART before #GOGOEND");
			mIsGoGoStarted = false;
			mParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_GOGOEND));
		} else if (mLine.startsWith("#MEASURE")) {
			Pattern p = Pattern.compile("#MEASURE\\s+(\\d+)\\s*/\\s*(\\d+)");
			Matcher m = p.matcher(mLine);
			if (m.find()) {
				TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_MEASURE);
				cmd.args = new int[2];
				cmd.args[0] = Integer.parseInt(m.group(1));
				cmd.args[1] = Integer.parseInt(m.group(2));
				if (cmd.args[0] < 0 || cmd.args[0] > 100 || cmd.args[1] < 0
						|| cmd.args[1] > 100)
					throwEx("#MEASURE arguments must be 1-99");
				mParsedCommands.add(cmd);
			} else
				throwEx("Unknown #MEASURE command");
		} else if (mLine.startsWith("#SCROLL")) {
			fields = mLine.split("\\s");
			if (fields.length != 2)
				throwEx("Unknown #SCROLL command");
			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_SCROLL);
			float arg = Float.parseFloat(fields[1]);
			if (arg < 0.1f || arg > 16.0f)
				throwEx("#SCROLL arguments must be 0.1f - 16.0f");
			cmd.args = new int[1];
			cmd.args[0] = Float.floatToIntBits(arg);
			mParsedCommands.add(cmd);
		} else if (mLine.startsWith("#DELAY")) {
			fields = mLine.split("\\s");
			if (fields.length != 2)
				throwEx("Unknown #DELAY command");
			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_DELAY);
			float arg = Float.parseFloat(fields[1]);
			arg = (float) (Math.floor(arg * 1000) / 1000);
			cmd.args[0] = Float.floatToIntBits(arg);

			mParsedCommands.add(cmd);
		} else if (mLine.equals("#SECTION")) {
			mParsingCourse.hasBranches = true;
			mParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_SECTION));
		} else if (mLine.startsWith("#BRANCHSTART")) {
			mParsingCourse.hasBranches = true; // #SECTION may be missing
			fields = mLine.substring(12).trim().split(",");
			if (fields.length != 3)
				throwEx("Unknown #BRANCHSTART command");
			fields[0] = fields[0].trim();
			if (fields[0].length() != 1)
				throwEx("Unknown #BRANCHSTART command");

			if (mIsBranchStarted) // #BRANCHSTART again
				emitBranch();

			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_BRANCHSTART);
			cmd.args = new int[7];

			switch (fields[0].charAt(0)) {
			case 'R':
				cmd.args[0] = TJAFormat.BRANCH_JUDGE_ROLL;
				break;
			case 'P':
				cmd.args[0] = TJAFormat.BRANCH_JUDGE_PRECISION;
				break;
			case 'S':
				cmd.args[0] = TJAFormat.BRANCH_JUDGE_SCORE;
				break;
			default:
				throwEx("Unknown #BRANCHSTART command");
			}
			cmd.args[1] = Float.floatToIntBits(Float.parseFloat(fields[1]));
			cmd.args[2] = Float.floatToIntBits(Float.parseFloat(fields[2]));
			mParsedCommands.add(cmd);
			mIsBranchStarted = true;
			mCommandOfStartedBranch = cmd;
		} else if (mLine.equals("#N")) {
			if (!mIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			mParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_N));

			// fill back
			if (0 != mCommandOfStartedBranch.args[3])
				throwEx("Duplicated #N");
			mCommandOfStartedBranch.args[3] = mParsedCommands.size() - 1;
		} else if (mLine.equals("#E")) {
			if (!mIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			mParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_E));

			// fill back
			if (0 != mCommandOfStartedBranch.args[4])
				throwEx("Duplicated #E");
			mCommandOfStartedBranch.args[4] = mParsedCommands.size() - 1;
		} else if (mLine.equals("#M")) {
			if (!mIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			mParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_M));

			// fill back
			if (0 != mCommandOfStartedBranch.args[5])
				throwEx("Duplicated #M");
			mCommandOfStartedBranch.args[5] = mParsedCommands.size() - 1;
		} else if (mLine.equals("#BRANCHEND")) {
			emitBranch();
			mParsedCommands
					.add(new TJACommand(TJAFormat.COMMAND_TYPE_BRANCHEND));
			mIsBranchStarted = false;
		} else if (mLine.equals("#LEVELHOLD")) {
			mParsedCommands
					.add(new TJACommand(TJAFormat.COMMAND_TYPE_LEVELHOLD));
		}
	}

	private void emitNotation() throws TJAFormatParserException {
		if (mParsedCommands.size() == 0) {
			throwEx("No notes at all!");
		}
		TJACommand[] notation = mParsedCommands
				.toArray(new TJACommand[mParsedCommands.size()]);
		mParsedCommands.clear();

		boolean hasNote = false;
		for (TJACommand cmd : notation) {
			if (TJAFormat.COMMAND_TYPE_NOTE == cmd.commandType) {
				hasNote = true;
				break;
			}
		}

		if (!hasNote) {
			throwEx("No note at all!");
		}

		if (!mIsParsingDouble) {
			mParsingCourse.notationSingle = notation;
		} else if (!mIsParsingP2) {
			mParsingCourse.notationP1 = notation;
			mIsParsingP2 = false;
		} else {
			mParsingCourse.notationP2 = notation;
			mIsParsingDouble = false;
		}

		mIsStarted = false;
		mIsBranchStarted = false;
	}

	private void emitCourse() {
		mParsedCourses.add(mParsingCourse);
		mParsingCourse = new TJACourse();
		mParsingCourse.BPM = mBPM;
		mIsParsingP2 = false;
		mIsParsingDouble = false;
		mIsStarted = false;
		mIsBranchStarted = false;
	}

	private void parseNotesOfCurrentLine() {
		char[] lineChars = mLine.toCharArray();
		for (char c : lineChars) {
			if ('0' <= c && c <= '9') {
				mParsingNotes.put(c - '0');
			} else if (Character.isWhitespace(c)) {
				continue;
			} else if (c == ',') {
				emitNotes();
			}
		}
	}

	private void setHeader(String name, String value) throws TJAFormatParserException {
		// omit the empty value string
		if (value.length() == 0) {
			return;
		}

		if (name.equals("TITLE")) {
			mFormat.title = new String(value);
		} else if (name.equals("LEVEL")) {
			mParsingCourse.level = Integer.parseInt(value);
			if (mParsingCourse.level < 1 || mParsingCourse.level > 12)
				throwEx("Bad level");
		} else if (name.equals("BPM")) {
			mParsingCourse.BPM = mBPM = Float.parseFloat(value);
			if (mParsingCourse.BPM < 50 || mParsingCourse.BPM > 250) {
				throwEx("BPM must be 50-250");
			}
		} else if (name.equals("WAVE")) {
			mFormat.wave = new String(value);
		} else if (name.equals("OFFSET")) {
			mFormat.offset = Float.parseFloat(value);
		} else if (name.equals("BALLOON")) {
			String[] balloons = value.split(",");
			mParsingCourse.balloons = new int[balloons.length];
			for (int i = 0; i < mParsingCourse.balloons.length; ++i) {
				mParsingCourse.balloons[i] = Integer.parseInt(balloons[i]
						.trim());
			}
		} else if (name.equals("SONGVOL")) {
			mFormat.songVol = Float.parseFloat(value);
			if (mFormat.songVol < 0 || mFormat.songVol > 100) {
				throwEx("SONGVOL must be 0-100");
			}
		} else if (name.equals("SEVOL")) {
			mFormat.seVol = Float.parseFloat(value);
			if (mFormat.seVol < 0 || mFormat.seVol > 100) {
				throwEx("SEVOL must be 0-100");
			}
		} else if (name.equals("SCOREINIT")) {
			mParsingCourse.scoreInit = Integer.parseInt(value);
			if (mParsingCourse.scoreInit < 0) {
				throwEx("SCOREINIT must be positive");
			}
		} else if (name.equals("SCOREDIFF")) {
			mParsingCourse.scoreDiff = Integer.parseInt(value);
			if (mParsingCourse.scoreDiff < 0) {
				throwEx("SCOREDIFF must be positive");
			}
		} else if (name.equals("COURSE")) {
			if (mParsingCourse.notationSingle != null
					|| mParsingCourse.notationP1 != null
					&& mParsingCourse.notationP2 != null) {
				emitCourse();
			}

			if (mParsingCourse.notationP1 != null
					&& mParsingCourse.notationP2 == null) {
				throwEx("Missing #START P2");
			}

			value = value.trim();

			if (value.equalsIgnoreCase("Easy")
					|| value.equals(Integer.toString(TJAFormat.COURSE_EASY))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_EASY;
			} else if (value.equalsIgnoreCase("Normal")
					|| value.equals(Integer.toString(TJAFormat.COURSE_NORMAL))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_NORMAL;
			} else if (value.equalsIgnoreCase("Hard")
					|| value.equals(Integer.toString(TJAFormat.COURSE_HARD))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_HARD;
			} else if (value.equalsIgnoreCase("Oni")
					|| value.equals(Integer.toString(TJAFormat.COURSE_ONI))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_ONI;
			} else if (value.equalsIgnoreCase("Edit")
					|| value.equals(Integer.toString(TJAFormat.COURSE_EDIT))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_EDIT;
			} else if (value.equalsIgnoreCase("Tower")
					|| value.equals(Integer.toString(TJAFormat.COURSE_TOWER))) {
				mParsingCourse.courseIndex = TJAFormat.COURSE_TOWER;
			} else {
				throwEx("Unknown course");
			}
		} else if (name.equals("STYLE")) {
			if (value.equalsIgnoreCase("Single")) {
				mIsParsingDouble = false;
			} else if (value.equalsIgnoreCase("Double")
					|| value.equalsIgnoreCase("Couple")) {
				mIsParsingDouble = true;
			} else {
				throwEx("STYLE must be Single, Double or Couple");
			}
		} else if (name.equals("DEMOSTART")) {
			mFormat.demoStart = Float.parseFloat(value);
		} else if (name.equals("SIDE")) {
			if (value.equalsIgnoreCase("Normal")
					|| value.equals(TJAFormat.SIDE_NORMAL)) {
				mFormat.side = TJAFormat.SIDE_NORMAL;
			} else if (value.equalsIgnoreCase("Ex")
					|| value.equals(TJAFormat.SIDE_EX)) {
				mFormat.side = TJAFormat.SIDE_EX;
			} else if (value.equalsIgnoreCase("Both")
					|| value.equals(TJAFormat.SIDE_BOTH)) {
				mFormat.side = TJAFormat.SIDE_BOTH;
			}
		} else if (name.equals("SUBTITLE")) {
			mFormat.subTitle = new String(value);
		} else if (name.equals("GAME")) {
			if (!value.equalsIgnoreCase("Taiko")) {
				throwEx("Unsupported game mode: " + value);
			}
		}
	}

	private void emitBranch() throws TJAFormatParserException {
		if (mCommandOfStartedBranch.args[3] == 0) {
			throwEx("Missing #N");
		} else if (mCommandOfStartedBranch.args[4] == 0) {
			throwEx("Missing #E");
		} else if (mCommandOfStartedBranch.args[5] == 0) {
			throwEx("Missing #M");
		}

		mCommandOfStartedBranch.args[6] = mParsedCommands.size();
	}
}