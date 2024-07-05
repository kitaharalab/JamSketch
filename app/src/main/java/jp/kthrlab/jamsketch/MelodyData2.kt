package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.CMXApplet
import jp.crestmuse.cmx.processing.CMXController
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import kotlin.math.roundToInt

class MelodyData2(
        private var filename: String,
        private var width: Int,
        private var cmx: CMXApplet,
        private var pianoroll: SimplePianoRoll,
        private var cfg: Config
) {

    var engine: JamSketchEngineAbstract
        get() = field
    var curve1: MutableList<Int?>? = null
        get() = field
    var scc: SCCDataSet
        get() = field

    init {
        scc = CMXController.readSMFAsSCC(JamSketchActivity.jamSketchResources?.getAssets()?.open(filename)).toDataSet()
        scc.repeat(
            (Config.INITIAL_BLANK_MEASURES * Config.BEATS_PER_MEASURE * scc.division).toLong(),
            ((Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES) *
                    Config.BEATS_PER_MEASURE * scc.division).toLong(),
            Config.REPEAT_TIMES - 1)
        var target_part = scc.getFirstPartWithChannel(1)
        engine = Class.forName(Config.JAMSKETCH_ENGINE).newInstance() as JamSketchEngineAbstract
        engine.init(scc, target_part, cfg)
        engine.model
        resetCurve()
    }

  fun resetCurve() {
    curve1 = arrayOfNulls<Int>(width).toMutableList() //[null] * width
    engine.resetMelodicOutline()
  }

  fun updateCurve(from: Int, thru: Int) {
    val nMeas: Int = Config.NUM_OF_MEASURES
    val div: Int = Config.getDivision()
    val size2: Int = nMeas * div

      for (i in from..thru) {
          val ii: Int = i - pianoroll.keyboardWidth.roundToInt()
          if(curve1!![ii] != null
              && curve1!![i] != null
          ) {
              val nn: Double = pianoroll.y2notenum(curve1!![i]!!.toDouble())
              println("var nn: ${nn} curve1!![i] == ${curve1!![i]}")
              val position: Int = (ii * size2 / (curve1!!.size))
              if (position >= 0) {
                  if (BuildConfig.DEBUG) println("engine.setMelodicOutline((${position} / ${div}), ${position} % ${div}, ${nn}})")
                  engine.setMelodicOutline((position / div), position % div, nn)
              }
          }
      }
  }
}	    