package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.misc.ChordSymbol2

public class Config {


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
    companion object {
        const val MIDFILENAME = "blues01.mid"
        val chordprog = listOf<ChordSymbol2>(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.C, ChordSymbol2.G, ChordSymbol2.F, ChordSymbol2.C, ChordSymbol2.G)
        const val NUM_OF_MEASURES = 12
        const val DIVISION = 12
        const val BEATS_PER_MEASURE = 4
        const val INITIAL_BLANK_MEASURES = 2
        const val REPEAT_TIMES = 4
        const val CALC_LENGTH = 1
        const val MODEL_FILE = "model20180321.json"
        const val LOG_DIR = "log/"
        const val EXPRESSION = false // LITE version supports "false" only
        const val MELODY_RESETING = true
        const val CURSOR_ENHANCED = true
        const val ON_DRAG_ONLY = true
        const val EYE_MOTION_SPEED = 30
        const val FORCED_PROGRESS = false
        val MOTION_CONTROLLER = null // LITE version supports "null" only

        //  def OCTAVE_PROGRAM = [win: "octave-4.0.1.exe"]

        const val GA_TIME = 500
        const val GA_POPUL_SIZE = 200
        const val GA_INIT = "tree" // "random" or "tree"

        // NewMelodyEngine
        const val JAMSKETCH_ENGINE = "jp.kthrlab.jamsketch.JamSketchEngineSimple"
        const val ENT_BIAS = 0.0
        const val RHYTHM_DENSITY = 6.0
        const val SIM_THRESHOLD = 10


        // Android
        const val SHOW_ONBOARDING = true
    }

}
