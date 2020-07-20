package com.superguo.jiroplayer;

import java.util.ArrayList;

import android.util.Log;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;
import com.superguo.jiroplayer.TJANotation.Note;
import com.superguo.jiroplayer.TJANotation.NoteBar;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand;

public class TJANotationCompiler {
	private static final String TAG = "TJANotationCompiler";
	public static final int NOTATION_INDEX_SINGLE = 0;
	public static final int NOTATION_INDEX_P1     = 1;
	public static final int NOTATION_INDEX_P2     = 2;
	/** Special branch index used for the case outside #BRANCHSTART */
	private static final int BRANCH_INDEX_NORMAL = 0;
	private static final int BRANCH_INDEX_EASY   = 1;
	private static final int BRANCH_INDEX_MASTER   = 2;
	
	private TJANotation mNotation;
	private TJAFormat mTja;
	private TJACommand[] mNotationCommands;
	private long mStartWaitTimeMillis;
	private long mEndWaitTimeMillis;
	private TJACourse mCourse;
	private int mBeatDist;
	private int mScrollBandLeft;
	private int mScrollBandRight;
	private int mTargetNoteCenter;
	
	public static final class TJANotationCompilerException extends Exception {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -6481065923819947596L;

		public TJANotationCompilerException(int lineNo, String line, String msg) {
			super("Line " + lineNo + " " + msg + "\n"
					+ (line == null ? "" : line));
		}
	
		public TJANotationCompilerException(int lineNo, String line, Throwable r) {
			super("Line " + lineNo + "\n" + (line == null ? "" : line), r);
		}
	}

	/**
	 * Compile the specified notation of a TJA format into a compiled TJANotaion
	 * 
	 * @param tja
	 *            The TJA format data.
	 * @param courseIndex
	 *            The index of the course stored in TJA format data.
	 * @param notationIndex
	 *            The index of the notation to compile. The index is one of
	 *            {@link #NOTATION_INDEX_SINGLE}, {@link #NOTATION_INDEX_P1} or
	 *            {@link #NOTATION_INDEX_P2}
	 * @param startWaitTimeMillis
	 *            The time to wait before the notation or music (choose the
	 *            earlier) starts to scroll.
	 * @param endWaitTimeMillis
	 *            The time to wait after notation ends
	 * @param beatDist
	 *            The distance between two 16th tja notes in standard scrolling
	 *            speed, which also equals the diameter of one tja note
	 * @param scrollBandLeft
	 *            The leftmost x position of the scroll band
	 * @param scrollBandRight
	 *            The rightmost x position of the scroll band
	 * @param targetNoteCenter
	 *            The x position of the target beat note's center point
	 * @return
	 */
	public TJANotation compile(TJAFormat tja, int courseIndex,
			int notationIndex, long startWaitTimeMillis, long endWaitTimeMillis, int beatDist,
			int scrollBandLeft, int scrollBandRight, int targetNoteCenter) {
		mCourse = tja.courses[courseIndex];
		if (mCourse == null) {
			return null;
		}
		TJACommand[][] uncompiledNotations = { 
				mCourse.notationSingle, mCourse.notationP1, mCourse.notationP2 };
		TJACommand[] notationCommands = uncompiledNotations[notationIndex];
		if (notationCommands == null) {
			return null;
		}
		mNotation = new TJANotation();
		mTja = tja;
		mNotationCommands = notationCommands;
		mStartWaitTimeMillis = startWaitTimeMillis;
		mEndWaitTimeMillis = endWaitTimeMillis;
		mBeatDist = beatDist;
		mScrollBandLeft = scrollBandLeft;
		mScrollBandRight = scrollBandRight;
		mTargetNoteCenter = targetNoteCenter;
		doCompile();
		return mNotation;
	}

	private void doCompile() {
		if (mTja.offset<0) {
			// If the music starts earlier than the notation
			mNotation.musicStartMillis = mStartWaitTimeMillis;
		} else {
			// If the music starts later than the notation - we should avoid this case
			mNotation.musicStartMillis = mStartWaitTimeMillis + Math.round(mTja.offset * 1000);
		}
		boolean hasBranch = mCourse.hasBranches;

		ArrayList<TJANotation.Bar> normalBranch = compileBranch(BRANCH_INDEX_NORMAL);

		if (hasBranch) {
			ArrayList<TJANotation.Bar> easyBranch = compileBranch(BRANCH_INDEX_EASY);
			ArrayList<TJANotation.Bar> masterBranch = compileBranch(BRANCH_INDEX_MASTER);
			
//			int numBars = normalBranch.size();
//			for (int i=0; i<numBars; ++i) {
//				 // Check all sub-branches' duration consistency 
//			}
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
			mNotation.easyBranch   = (Bar[]) easyBranch.toArray();
			mNotation.masterBranch = (Bar[]) masterBranch.toArray();
		} else {
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
		}
	}

	private ArrayList<Bar> compileBranch(final int branchIndex) {
		int length = mNotationCommands.length;
		
		/** The branch to emit */
		ArrayList<Bar> branch = new ArrayList<Bar>(length);
		
		final int beatDist = mBeatDist;
		
		final int scrollBandWidth = mScrollBandRight - mScrollBandLeft;
		
		final int scrollBandWidthFromTarget = mScrollBandRight - mTargetNoteCenter;
		
		/** The compiling bar's BPM */
		double bpm = mCourse.BPM;
		
		/**
		 * The BPM to change to, from #BPMCHANGE. negative means the BPM is not
		 * to be changed.
		 */
		double bpmToChange = -1.0;
		
		/** The compiling bar's X in MEASURE X/Y */
		int measureX = 4;
		
		/** The compiling bar's Y in MEASURE X/Y */
		int measureY = 4;
		
		/** The compiling bar's scroll value  */
		double scroll = 1.0;
		
		/** The scroll to change to, from #SCROLL. negative means the scroll is not
		 * to be changed */
		double scrollToChange = -1.0;
		
		/** The visibility of the bar line of the compiling note bar */
		boolean isBarLineOn = true;
		
		/** The compiling bar's flag indicating if the last compiled note is rolling */
		boolean isLastNoteRolling = false;
		
		/** The the last rolling note. */
		Note lastRollingNote = null;
		
		/**
		 * The accumulated delay in millisecond before the next note bar starts.
		 * It will be reseted to 0.0 once after the next note bar is compiled
		 */
		double delay = 0.0;
	
		/**  The first note bar's beat time */
		double firstBarBeatTime;
		
		if (mTja.offset<0) {
			// If the music starts earlier than the notation
			firstBarBeatTime = mStartWaitTimeMillis - Math.round(mTja.offset*1000);
		} else {
			// If the music starts later than the notation - we should avoid this case
			firstBarBeatTime = mStartWaitTimeMillis;
		}
		
		/** The time offset of the beginning of the current note bar */
		double barBeatMillis = firstBarBeatTime;
		
		/** The bar's speed in pixels per millisecond */
		double barSpeed = computeBarSpeed(bpm, scroll, beatDist);
		
		/** The current #BRANCHSTART. It is not null until #BRANCHEND is reached */
		StartBranchCommand startBranchCmd = null;

		/**
		 * If we are in #N, #E or #M corresponding to current branchIndex. It is
		 * valid only when startBranchCmd is not null
		 */
		boolean inCompilingBranch = false;
		
		NoteBar lastNoteBar = null;
		
		for (int i=0; i<length; ++i) {
			TJACommand oCmd = mNotationCommands[i];
			Bar bar = new Bar();
			ArrayList<Note> compiledNotes = new ArrayList<Note>();
			
			/*
			 * Ignore the command if #BRANCHSTART is encountered but not in the
			 * compiling branch, except that it is a branch control command
			 */
			if (startBranchCmd != null && inCompilingBranch) {
				boolean ignoreCommand = true;
				switch (oCmd.commandType) {
				case TJAFormat.COMMAND_TYPE_BRANCHEND:
				case TJAFormat.COMMAND_TYPE_N:
				case TJAFormat.COMMAND_TYPE_E:
				case TJAFormat.COMMAND_TYPE_M:
					ignoreCommand = false;
				}
				if (ignoreCommand) {
					bar.isNoteBar = false;
					bar.command = new TJANotation.Command(TJANotation.COMMAND_EMPTY);
					branch.add(bar);
					continue;
				}
			}
			
			switch (oCmd.commandType) {
			case TJAFormat.COMMAND_TYPE_NOTE: {
				bar.isNoteBar = true;
				NoteBar noteBar = bar.noteBar = new NoteBar();
				noteBar.beatMillis = Math.round(barBeatMillis + delay);
				noteBar.isBarLineOn = isBarLineOn;
				double appearMillis;
				short[] millisDistances;
				/* One TJA note's width is 16th note (semiquaver) length.
				 * So 1 beat = 1 quarter notes(crochets) = Four 16th notes = 4 TJA notes.
				 * The mBeatDist is actually a TJA note's width
				 */
				boolean isBpmChanging = bpmToChange > 0;
				boolean isScrollChanging = scrollToChange > 0;
				
				if ( ! mTja.bmScroll && ! mTja.hbScroll || !isBpmChanging && Math.abs(delay)<0.001 ) {
					// BPM or scroll changes immediately if neither BMSCROLL nor HBSCROLL is on, 
					// or neither #BPMCHANGE nor #DELAY is present before this note begins
					if (isBpmChanging) {
						bpm = bpmToChange;
						bpmToChange = -1.0;
					}
					if (isScrollChanging) {
						scroll = scrollToChange;
						scrollToChange = -1.0;
					}
					if (isBpmChanging || isScrollChanging) {
						// Re-compute barSpeed if either bpm or scroll has just changed
						barSpeed = computeBarSpeed(bpm, scroll, beatDist);	
					}
					
					appearMillis = barBeatMillis
							- (scrollBandWidthFromTarget + beatDist / 2.0) / barSpeed;
					// maxBarWidth is the total width of the bar, i.e. the
					// distance between the beginning of this bar and next bar
					double maxBarWidth = (double)measureX / measureY * 4 * beatDist * scroll;
					// barVisibleDuration is the total time when the bar passes through the scroll band 
					int barVisibleDuration = (int) (Math.round((maxBarWidth + scrollBandWidth) / barSpeed)) + 1;
					millisDistances = new short[barVisibleDuration];
					double preciseXCoord = mScrollBandRight + beatDist/2.0;
					millisDistances[0] =(short) preciseXCoord;
					for (int t=1; t<barVisibleDuration; ++t) {
						preciseXCoord -= barSpeed; // * 1.0;
						millisDistances[t] =(short) preciseXCoord;
					}
					
					noteBar.appearMillis = Math.round(appearMillis); 
					noteBar.millisDistances = millisDistances;
					
				} else { // When HBSCROLL or BMSCROLL is on, the bar's speed
						 // does not change until it is passing the target beat note

					if (mTja.hbScroll && isScrollChanging) {
						// When HBSCROLL is on, the post-beat-note bar speed
						// takes the scroll value into account, on the condition
						// that the scroll value has just changed
						barSpeed = computeBarSpeed(bpm, scrollToChange, beatDist);
					}
					// postBarSpeed is the actual speed immediately after the
					// bar enters the target beat note
					double postBarSpeed = computeBarSpeed(
							isBpmChanging ? bpmToChange : bpm,
							isScrollChanging ? scrollToChange : scroll,
							beatDist); 

					// The minimum precision of delay is 0.001 second 
					if (delay >= 0.001) {
						// If delay is positive, the bar stops before target
						// beat note until the delayed time has elapsed.
						// In this case, we leave some space - the radius of the
						// beatDist - before the the bar hit the target beat note
						double spaceBetweenDelayAndBeat = beatDist / 2.0;
						appearMillis = barBeatMillis
								- (scrollBandWidthFromTarget + beatDist / 2.0) / barSpeed;
						double maxBarWidth = (double) measureX / measureY * 4 * beatDist
								* (mTja.hbScroll && isScrollChanging ? 
										scrollToChange : scroll);
						double visibleDurationBeforeDelay = (scrollBandWidth 
								- mTargetNoteCenter - spaceBetweenDelayAndBeat) / barSpeed;
						
						int barVisibleDuration = (int) (Math.round(
								delay
								+ scrollBandWidth / barSpeed
								+ maxBarWidth / postBarSpeed)) + 1;
						millisDistances = new short[barVisibleDuration];
						double preciseXCoord = mScrollBandRight + beatDist/2.0;
						millisDistances[0] =(short) preciseXCoord;
						double xCoordOnStop = mTargetNoteCenter + spaceBetweenDelayAndBeat;
						for (int t=1; t<barVisibleDuration; ++t) {
							if (t<visibleDurationBeforeDelay) {
								preciseXCoord -= barSpeed;
							} else if ( t < visibleDurationBeforeDelay + delay) {
								// preciseXCoord remains unchanged
								preciseXCoord = xCoordOnStop;
							} else {
								preciseXCoord -= postBarSpeed;
							}
							millisDistances[t] =(short) preciseXCoord;
						}
						
						noteBar.appearMillis = Math.round(appearMillis); 
						noteBar.millisDistances = millisDistances;
					} else {
						// The delay is 0 or negative
						appearMillis = barBeatMillis
								- (scrollBandWidthFromTarget + beatDist / 2.0) / barSpeed;
						double maxBarWidth = (double) measureX / measureY * 4 * beatDist
								* (mTja.hbScroll && isScrollChanging ? 
										scrollToChange : scroll);
						double visibleDurationBeforeBeat = (scrollBandWidthFromTarget)
								/ barSpeed;
						
						int barVisibleDuration = (int) (Math.round(
								+ scrollBandWidth / barSpeed
								+ maxBarWidth / postBarSpeed)) + 1;
						millisDistances = new short[barVisibleDuration];
						double preciseXCoord = mScrollBandRight + beatDist/2.0;
						millisDistances[0] =(short) preciseXCoord;
						for (int t=1; t<barVisibleDuration; ++t) {
							if (t < visibleDurationBeforeBeat) {
								preciseXCoord -= barSpeed;
							} else {
								preciseXCoord -= postBarSpeed;
							}
							millisDistances[t] =(short) preciseXCoord;
						}
						
					} // End of delay

					if (isBpmChanging) {
						bpm = bpmToChange;
						bpmToChange = -1.0;
					}
					if (isScrollChanging) {
						scroll = scrollToChange;
						scrollToChange = -1.0;
					}

				} // End of hbscroll and bmscroll
				noteBar.appearMillis = Math.round(appearMillis); 
				noteBar.millisDistances = millisDistances;
				
				int[] oNotes = oCmd.args;
				
				compiledNotes.clear();
				double noteBeatTime = barBeatMillis;
				double noteBeatTimeSpan = 15000.0 / bpm; // == 0.25 / bpm * 60000;
				double noteBeatDistSpan = barSpeed * noteBeatTimeSpan;
				double offsetX = 0.0;
				
				for (int noteValue : oNotes) {
					if (isLastNoteRolling) {
						if (noteValue == 8) { // 8 means finished
							lastRollingNote.handleEndMillis = (long) noteBeatTime;
							lastRollingNote.offsetX2 = (int) offsetX;
							isLastNoteRolling = false;
						}
					} else {
						switch (noteValue) {
						case 1:		// face
						case 2:		// side
						case 3:		// big face
						case 4: {	// big side
							Note note = new Note();
							note.noteValue = noteValue;
							note.beatMillis = (long) noteBeatTime;
							note.handleStartMillis = note.beatMillis - PlayModel.TIME_JUDGE_BAD;
							note.handleEndMillis = note.beatMillis + PlayModel.TIME_JUDGE_BAD;
							note.offsetX = (int) offsetX;
							compiledNotes.add(note);
							break;
						}

						case 5: 	// len-da
						case 6: 	// Big len-da
						case 7: 	// Balloon
						case 9: {	// Potato
							isLastNoteRolling = true;
							Note note = lastRollingNote = new Note();
							note.noteValue = noteValue;
							note.beatMillis = (long) noteBeatTime;
							note.handleStartMillis = note.beatMillis;
							note.offsetX = (int) offsetX;
							compiledNotes.add(note);
							break;
						}
							
						case 0:
						case 8: // Bad note here
						default:
							break;
						}
					}
					noteBeatTime += noteBeatTimeSpan;
					offsetX += noteBeatDistSpan;
				}
				// Compute next beat time
				barBeatMillis += (double)measureX / measureY / bpm * 60000.0;
				// Emit notes
				noteBar.notes = (Note[]) compiledNotes.toArray();
				// Set last note bar's nextNoteBarIndex to this note bar's index 
				if (lastNoteBar!=null) {
					lastNoteBar.nextNoteBarIndex = i;
				}
				// Clear values
				delay = 0.0;
				// Set last note bar
				lastNoteBar = noteBar;
				break;
			}
			
			case TJAFormat.COMMAND_TYPE_BPMCHANGE:
				bpmToChange = (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOSTART:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_GOGOSTART);
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOEND:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_GOGOEND);
				break;
				
			case TJAFormat.COMMAND_TYPE_MEASURE: 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
				measureX = oCmd.args[0];
				measureY = oCmd.args[1];
				break;

			case TJAFormat.COMMAND_TYPE_SCROLL: 	// float(0.1 - 16.0)
				scrollToChange = (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_DELAY: 	// float(>0.001)
				// NOTE here that the delay value is to be accumulated rather
				// than be set
				delay += (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_SECTION:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_SECTION);
				break;

			case TJAFormat.COMMAND_TYPE_BRANCHSTART: {
				bar.isNoteBar = false;
				startBranchCmd = new TJANotation.StartBranchCommand();
				startBranchCmd.compileTJAFormatArgs(oCmd.args);
				bar.command = startBranchCmd;
				break;
			}

			case TJAFormat.COMMAND_TYPE_BRANCHEND:
			case TJAFormat.COMMAND_TYPE_N:
			case TJAFormat.COMMAND_TYPE_E:
			case TJAFormat.COMMAND_TYPE_M:
				assert startBranchCmd != null;
				if (inCompilingBranch) {
					bar.isNoteBar = false;
//					bar.command = new TJANotation.Command(TJANotation.COMMAND_EXITBRANCH);
					bar.command = new TJANotation.Command(TJANotation.COMMAND_EMPTY);
					inCompilingBranch = false;
				} else if (matchesBranch(oCmd.commandType, branchIndex)) {
					// We are entering the compiling branch
					inCompilingBranch = true;
					// Just ignore the command
					bar.isNoteBar = false;
					bar.command = new TJANotation.Command(TJANotation.COMMAND_EMPTY);
				} else {
					// We are here because we encounter another branch or reach the end of branch
					inCompilingBranch = false;
					// Just ignore the command
					bar.isNoteBar = false;
					bar.command = new TJANotation.Command(TJANotation.COMMAND_EMPTY);
				}
				// Set startBranchCmd to be null if we encounter #BRANCHEND
				if (oCmd.commandType == TJAFormat.COMMAND_TYPE_BRANCHEND) {
					startBranchCmd = null;
				}
				break;

			case TJAFormat.COMMAND_TYPE_LEVELHOLD:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_LEVEL_HOLD);
				break;

				
			case TJAFormat.COMMAND_TYPE_BARLINEOFF:
				isBarLineOn = false;
				break;
				
			case TJAFormat.COMMAND_TYPE_BARLINEON:
				isBarLineOn = true;
				break;
			
			default:;
			}
			
			branch.add(bar);
		}
		long endMillis = (long)barBeatMillis + mEndWaitTimeMillis;
		if (mNotation.endMillis == 0) {
			mNotation.endMillis = endMillis;
		} else if (mNotation.endMillis != endMillis) {
			Log.w(TAG, "Inconsistent branch duration");
			if (mNotation.endMillis < endMillis) {
				mNotation.endMillis = endMillis;
			}
		}
		return branch;
	}

	/**
	 * The bar's speed (pixel per millisecond) is 
	 * bpm * 4 * beatDist / 60000.0 * scroll
	 * @param bpm
	 * @param scroll
	 * @param beatDist
	 * @return
	 */
	private static double computeBarSpeed(double bpm, double scroll, final int beatDist) {
		return bpm * beatDist / 15000.0 * scroll;
	}
	
	private static boolean matchesBranch(int commandType, int branchIndex) {
		int c=commandType;
		int b=branchIndex;
		return b == BRANCH_INDEX_NORMAL && c == TJAFormat.COMMAND_TYPE_N
				|| b == BRANCH_INDEX_EASY && c == TJAFormat.COMMAND_TYPE_E
				|| b == BRANCH_INDEX_MASTER && c == TJAFormat.COMMAND_TYPE_M;
	}
}
