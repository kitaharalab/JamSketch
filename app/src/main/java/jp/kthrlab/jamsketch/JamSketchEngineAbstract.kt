package jp.kthrlab.jamsketch

import com.beust.klaxon.Parser
import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.misc.ChordSymbol2.C
import jp.crestmuse.cmx.misc.ChordSymbol2.F
import jp.crestmuse.cmx.misc.ChordSymbol2.G
import jp.crestmuse.cmx.processing.CMXController
import java.io.InputStreamReader

abstract class JamSketchEngineAbstract : JamSketchEngine {
  var mr: MusicRepresentation? = null
  var cmx: CMXController? = null
  var cfg: Config? = null
  var model: Any? = null
  val OUTLINE_LAYER = "curve"
  val MELODY_LAYER = "melody"
  val CHORD_LAYER = "chord"

  override fun init(scc: SCC, target_part: SCC.Part, cfg: Config) {
    this.cfg = cfg
    model =  Parser.default().parse(
              InputStreamReader(
                JamSketchActivity
                  .jamSketchResources?.assets?.open(Config.MODEL_FILE)
              )) //as JsonArray<JsonObject>
    cmx = CMXController.getInstance()
    mr = CMXController.createMusicRepresentation(
      Config.NUM_OF_MEASURES,
      Config.getDivision())

    // add Music Layers
    mr!!.addMusicLayerCont(OUTLINE_LAYER)
    mr!!.addMusicLayer(CHORD_LAYER,
            listOf<ChordSymbol2>(
              C,
              F,
              G,
            ),
      Config.getDivision()
    )
    addMusicLayerLocal()

    Config.chordprog.toList().forEachIndexed { index, any ->
      mr!!.getMusicElement(CHORD_LAYER, index, 0).setEvidence(any)
    }


    var sccgen = SCCGenerator(target_part as SCCDataSet.Part, scc.division,
    OUTLINE_LAYER, ExpressionGenerator(), cfg)
    sccgen.cmx = cmx

    mr!!.addMusicCalculator(MELODY_LAYER, sccgen)
    var calc = musicCalculatorForOutline()
    if (calc != null) {
      mr!!.addMusicCalculator(OUTLINE_LAYER, calc)
    }
    initLocal()
  }

  abstract fun addMusicLayerLocal()
  abstract fun initLocal()
  abstract fun musicCalculatorForOutline(): MusicCalculator?

  override fun setMelodicOutline(measure: Int, tick: Int, value: Double?) {
//    if (BuildConfig.DEBUG) println("+++++++++ beginning of JamSketchEngineAbstract::setMelodicOutline(${measure}, ${tick}, ${value})")

    var e = mr!!.getMusicElement(OUTLINE_LAYER, measure, tick)
    if (!automaticUpdate()) {
      e.suspendUpdate()
    }
    if (value != null) {
      e.setEvidence(value)
    }
    outlineUpdated(measure, tick)
  }

  fun getMelodicOutline(measure: Int, tick: Int): Any {
    return mr!!.getMusicElement(OUTLINE_LAYER, measure, tick).
      getMostLikely()
  }

  abstract fun outlineUpdated(measure: Int, tick: Int)

  abstract fun automaticUpdate(): Boolean

  override fun resetMelodicOutline() {
    (0..Config.NUM_OF_MEASURES-1).forEach { i ->
      (0..Config.getDivision()-1).forEach { j ->
        mr!!.getMusicElement(OUTLINE_LAYER, i, j).
        setEvidence(Double.NaN)
      }
    }
  }

  override fun setFirstMeasure(number: Int) {
    SCCGenerator.firstMeasure = number
    println("SCCGenerator.firstMeasure = ${SCCGenerator.firstMeasure}")
  }

  override fun getChord(measure: Int, tick: Int): ChordSymbol2 {
    return mr!!.getMusicElement(CHORD_LAYER, measure, tick).
      getMostLikely() as ChordSymbol2
  }
}
