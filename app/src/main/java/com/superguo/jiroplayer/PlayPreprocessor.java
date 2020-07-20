package com.superguo.jiroplayer;

import java.util.LinkedList;


import com.superguo.jiroplayer.TJAFormat.TJACommand;

/** class used by PlayModel
 * 
 * @author superguo
 *
 */
final class PlayPreprocessor
{
	/** The pre-processing BPM */
	private float mBpm;
	
	/** The pre-processing X in MEASURE X/Y, available until changed */
	private int mMeasureX;
	
	/** The pre-processing Y in MEASURE X/Y, available until changed */
	private int mMeasureY;
	
	/** The pre-processing  microseconds per beat = 60 000 000 / BPM, available until changed */
	private long mMicSecPerBeat;
	
	/** The pre-processing speed of one note in pixels per 1000 seconds, available until changed */
	private int mSpeed;
	
	/** The pre-processing beat distance = BEAT_DIST * scroll, available until changed */
	private double mBeatDist;
	
	/** The pre-processing iBarLine, with value of on/off, available until changed */
	private boolean mBarLine;
	
	/** The pre-processing flag indicating if the last pre-processed note is rolling */
	private boolean mLastNoteRolling;

	/** Reset all pre-processing state variables */
	public final void reset(float bpm) {
		setBpm(bpm);
		setScroll(1.0f);
		mMeasureX = 4;
		mMeasureY = 4;
		mLastNoteRolling = false;
		mBarLine = true;
	}
	
	private final void calcSpeed() {
		mSpeed = (int) (mBeatDist * 2 * mBpm / 60 * 1000);
	}
	
	public final void setBpm(float aBpm) {
		mBpm = aBpm;
		mMicSecPerBeat = (long) (60000000 / aBpm);
		calcSpeed();
	}
	
	public final void setScroll(float scroll) {
		mBeatDist = PlayModel.BEAT_DIST * scroll;
		calcSpeed();
	}

	private final void processCmdNote(TJANotation.Bar bar, int[] barNotes, float delay) {
		// The number of beats in a bar is measureX / measureY
		double numBeats = (double) mMeasureX / mMeasureY;

		// The duration in minutes is numBeats / BPM
		// To convert the minutes to microseconds, just make it times 60 000 000
		bar.durationMicros = (long) (mMicSecPerBeat * numBeats);

		// When scroll is 1.0, one beat is two notes' length in pixels
		bar.length = (int) (mBeatDist * 2 * numBeats);

		bar.speed = mSpeed; // = bar.iLength / bar.iDuration * 1e9

		bar.numPreprocessedNotes = 0;

		int numNotes = barNotes.length;
		if (mBarLine) {
			PreprocessedNote noteOffset = bar.notes[bar.numPreprocessedNotes++];
			noteOffset.noteType = PlayModel.NOTE_BARLINE;
			noteOffset.offsetTimeMillis = 0;
			noteOffset.offsetPos = 0;
		}

		// transfer field variable to local variable
		boolean isLastNoteRolling = mLastNoteRolling;

		for (int i = 0; i < numNotes; ++i) {
			int note = barNotes[i];
			if (isLastNoteRolling) { // rolling is not complete last time
				if (note == 8) { // 8 means finished
					isLastNoteRolling = false;
					bar.addPreprocessedNote(PlayModel.NOTE_STOP_ROLLING, i,
							numNotes);
				}
			} else {
				switch (note) {
				case 5: // len-da (combo)
				case 6: // Big len-da
				case 7: // Balloon
				case 9: // Potato
					isLastNoteRolling = true;
					// DO NOT break here
				case 1:
				case 2:
				case 3:
				case 4:
					bar.addPreprocessedNote(note, i, numNotes);
					break;

				case 0:
				case 8: // Bad note here
				default:
					break;
				}
			}
		}

		// The iDelay will change iDuration & iLength
		// And cannot be handled before bar.addPreprocessedNote(), which uses
		// iDuration & iLength
		if (delay > 0.001f) {
			bar.durationMicros += delay * 1000000;
			bar.length += delay * bar.speed / 1000;

			// Reset after being pre-processed
			delay = 0.0f;
		}
		// transfer local variable back to field variable
		mLastNoteRolling = isLastNoteRolling;
	}

	/** At least one command is processed */
	public static final int PROCESS_RESULT_OK = 0;
	
	/** All command are processed */
	public static final int PROCESS_RESULT_EOF = 1;
	
	/** No free bar to process the command. Try later again */
	public static final int PROCESS_RESULT_BARS_FULL = 2;
	
	/** The branch is to exit but specified to be 0.
	 *  Zero or more commands are processed.
	 *  Try later again */
	public static final int PROCESS_RESULT_BRANCH_PENDING = 3;
	
	/**
	 * 
	 * @param bars
	 * @param notation
	 * @param processedCommandIndexRef [in/out]
	 * @param processedBarIndexRef [in/out]
	 * @param branchExitIndexRef [in/out] The command index of the exit branch.
	 * 	Use 0 to specify the case that processing branch is not
	 * 	the playing branch. It is set to 0 once the branch exits
	 * 
	 * @return
	 */
	public int processNextBar(Bar[] bars, TJACommand[] notation,
			IntegerRef processedCommandIndexRef,
			IntegerRef processedBarIndexRef, IntegerRef branchExitIndexRef) {
		int commandIndex = processedCommandIndexRef.value;
		if (commandIndex >= notation.length - 1) {
			return PROCESS_RESULT_EOF;
		}
		int lastProcessedBarIndex = processedBarIndexRef.value;

		// Get the next unprocessed bar
		int barIndex = PlayModel.nextIndexOfBar(lastProcessedBarIndex);

		if (barIndex == lastProcessedBarIndex) {
			return PROCESS_RESULT_BARS_FULL;
		}
		if (bars[barIndex].preprocessed) {
			return PROCESS_RESULT_BARS_FULL;
		}

		// Initialize the bar value
		TJANotation.Bar bar = bars[barIndex];
		// bar.iPreprocessed = false;
		bar.hasBranchStartNextBar = false;
		bar.numPreprocessedNotes = 0;

		LinkedList<TJACommand> unprocCmd = null;
		// Do not allocate memory until next command is not COMMAND_TYPE_NOTE
		if (notation[commandIndex + 1].commandType != TJAFormat.COMMAND_TYPE_NOTE) {
			unprocCmd = new LinkedList<TJACommand>();
		}

		float delay = 0.0f;

		for (commandIndex = nextCommandIndex(notation, commandIndex,
				branchExitIndexRef); !bar.preprocessed
				&& commandIndex < notation.length; commandIndex = nextCommandIndex(
				notation, commandIndex, branchExitIndexRef)) {
			// Return true in case aBranchExitIndex is 0
			// and the branch exit
			// This happens if this branch is not yet played
			// so the aBranchExitIndex is not yet determined
			if (-1 == commandIndex)
				return PROCESS_RESULT_BRANCH_PENDING;
			TJACommand cmd = notation[commandIndex];
			switch (cmd.commandType) {
			case TJAFormat.COMMAND_TYPE_NOTE:
				// Emit notes
				processCmdNote(bar, cmd.args, delay);

				// Set runtime offset
				if (lastProcessedBarIndex == -1) {
					bar.offsetTimeMicros = 0;
				} else {
					bar.offsetTimeMicros = bars[lastProcessedBarIndex].offsetTimeMicros
							+ bars[lastProcessedBarIndex].durationMicros;
				}
				// Check #BRACHSTART
				for (int i = commandIndex + 1; i < notation.length; ++i) {
					TJACommand cmd2 = notation[i];
					if (cmd2.commandType == TJAFormat.COMMAND_TYPE_NOTE) {
						break;
					} else if (cmd2.commandType == TJAFormat.COMMAND_TYPE_BRANCHSTART) {
						bar.hasBranchStartNextBar = true;
						break;
					}
				}

				// Emit unprocessed commands
				if (unprocCmd != null && unprocCmd.size() > 0) {
					bar.unprocessedCommand = unprocCmd
							.toArray(new TJACommand[unprocCmd.size()]);
				} else {
					bar.unprocessedCommand = null;
				}

				// Set processed
				bar.preprocessed = true;

				break;

			case TJAFormat.COMMAND_TYPE_BPMCHANGE: 
				setBpm(Float.intBitsToFloat(cmd.args[0]));
				break;

			case TJAFormat.COMMAND_TYPE_MEASURE:
				mMeasureX = cmd.args[0];
				mMeasureY = cmd.args[1];
				break;

			case TJAFormat.COMMAND_TYPE_SCROLL: {
				float scroll = Float.intBitsToFloat(cmd.args[0]);
				setScroll(scroll);
				break;
			}

			case TJAFormat.COMMAND_TYPE_GOGOSTART:
			case TJAFormat.COMMAND_TYPE_GOGOEND:
			case TJAFormat.COMMAND_TYPE_SECTION:
				// Will be executed in running bar
				unprocCmd.addLast(cmd.clone());
				break;

			case TJAFormat.COMMAND_TYPE_DELAY:
				delay = Float.intBitsToFloat(cmd.args[0]);
				break;

			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				// Emit a special bar containing no notes

				// No length at all
				bar.durationMicros = 0;
				bar.length = 0;

				// Will be executed in running bar before this!
				unprocCmd.addLast(cmd.clone());

				// Emit unprocessed commands.
				// The last command is always #BRANCHSTART
				bar.unprocessedCommand = unprocCmd
						.toArray(new TJACommand[unprocCmd.size()]);

				// Set processed
				bar.preprocessed = true;

				break;

			case TJAFormat.COMMAND_TYPE_BRANCHEND:
			case TJAFormat.COMMAND_TYPE_N:
			case TJAFormat.COMMAND_TYPE_E:
			case TJAFormat.COMMAND_TYPE_M:
				// Ignored
				break;

			case TJAFormat.COMMAND_TYPE_LEVELHOLD:
				// TODO
				// Not supported
				break;

			case TJAFormat.COMMAND_TYPE_BARLINEOFF:
				mBarLine = false;
				break;

			case TJAFormat.COMMAND_TYPE_BARLINEON:
				mBarLine = true;
				break;

			default:
				;
			}
		}
		processedCommandIndexRef.value = commandIndex;
		processedBarIndexRef.value = barIndex;
		return PROCESS_RESULT_OK;
	}
	
	private static int nextCommandIndex(TJACommand[] notation,
			int commandIndex, IntegerRef branchExitIndexRef) {
		if (-1 == commandIndex) {
			return 0;
		}
		if (commandIndex >= notation.length) {
			return notation.length;
		}
		switch (notation[commandIndex].commandType) {
		case TJAFormat.COMMAND_TYPE_BRANCHEND:
		case TJAFormat.COMMAND_TYPE_N:
		case TJAFormat.COMMAND_TYPE_E:
		case TJAFormat.COMMAND_TYPE_M: {
			int branchExitIndex = branchExitIndexRef.value;
			if (branchExitIndex <= 0) {
				return -1;
			}
			branchExitIndexRef.value = 0;
			return branchExitIndex;
		}

		default:
			return commandIndex + 1;
		}
	}
}
