package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.CMXApplet
import jp.crestmuse.cmx.processing.CMXController
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll

class MelodyData2(
        private var filename: String,
        private var width: Int,
        private var cmxcontrol: CMXApplet,
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
        scc = CMXController.readSMFAsSCC(JamSketchActivity.getMyResources().getAssets().open(filename)).toDataSet()
        scc.repeat(
            (Config.INITIAL_BLANK_MEASURES * Config.BEATS_PER_MEASURE * scc.division).toLong(),
            ((Config.INITIAL_BLANK_MEASURES + Config.NUM_OF_MEASURES) *
                    Config.BEATS_PER_MEASURE * scc.division).toLong(),
            Config.REPEAT_TIMES - 1)
        var target_part = scc.getFirstPartWithChannel(1)
    //    engine = new JamSketchEngineSimple()
        engine = Class.forName(Config.JAMSKETCH_ENGINE).newInstance() as JamSketchEngineAbstract
        engine.init(scc, target_part, cfg)
        engine.model
        resetCurve()
    }


  fun resetCurve() {
//    curve1 =  [null] * width
    curve1 = arrayOfNulls<Int>(width).toMutableList() //[null] * width
    engine.resetMelodicOutline()
  }

  fun updateCurve(from: Int, thru: Int) {
    var nMeas: Int = Config.NUM_OF_MEASURES
    var div: Int = Config.DIVISION
    var size2: Int = nMeas * div
    for (i in from..thru) {
      var nn: Double? = if(curve1!![i] == null) null else pianoroll.y2notenum(curve1!![i]!!.toDouble())
      var ii: Int = i - 100
      var position: Int = (ii * size2 / (curve1!!.size - 100))
      if (position >= 0) {
          println("engine.setMelodicOutline((${position} / ${div}), ${position} % ${div}, ${nn}})")
        engine.setMelodicOutline((position / div), position % div, nn)
      }
    }
  }
}	    