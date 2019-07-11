package jp.kthrlab.jamsketch;

import java.util.Arrays;
import java.util.List;

import jp.crestmuse.cmx.misc.ChordSymbol2;

public class Config {
    static String MIDFILENAME = "blues01.mid";
    static List<ChordSymbol2> chordprog = Arrays.asList(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.C, ChordSymbol2.G, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.G);
    static int NUM_OF_MEASURES = 12;
    static int DIVISION = 12;
    static int BEATS_PER_MEASURE = 4;
    static int INITIAL_BLANK_MEASURES = 2;
    static int REPEAT_TIMES = 2;
    static int CALC_LENGTH = 1;
    static String MODEL_FILE = "model20161104b.json";
    static String LOG_DIR = "log/";
    static boolean EXPRESSION = false; // LITE version supports "false" only
    static boolean MELODY_RESETING = true;
    static boolean CURSOR_ENHANCED = true;
    static boolean ON_DRAG_ONLY = true;
    static int EYE_MOTION_SPEED = 30;
    static boolean FORCED_PROGRESS = false;
    static String MOTION_CONTROLLER = null; // LITE version supports "null" only

    //  def OCTAVE_PROGRAM = [win: "octave-4.0.1.exe"]

    static int GA_TIME = 50;
    static int GA_POPUL_SIZE = 200;
    static String GA_INIT = "tree";  // "random" or "tree"

/*
  def MODEL_FILENAME_BASE = "./expressive/models/takeTheATrain_KNN"
  def MODEL_FILENAMES =
    [dur: MODEL_FILENAME_BASE + "_durRat.model",
     energy: MODEL_FILENAME_BASE + "_energyRat.model",
     onset: MODEL_FILENAME_BASE + "_onsetDev.model"];
  def LIMITS =
    [onset: [-0.0833, 0.0833], dur: [0.0625, 1.25], energy: [0.0, 1.99]]
    //[onset: [-0.1250, 0.1250], dur: [0.0625, 1.5], energy: [0.0, 1.99]]
    //[onset: [-0.0833, 0.0833], dur: [0.0, 1.25], energy: [0.0, 1.99]]
    //[onset: [-0.0625, 0.0625], dur: [0.0, 1.25], energy: [0.0, 1.99]]
  */

}
