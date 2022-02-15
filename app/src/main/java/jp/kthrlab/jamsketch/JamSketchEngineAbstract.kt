package jp.kthrlab.jamsketch

import com.beust.klaxon.Parser
import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.misc.ChordSymbol2.*
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
//    var json = JsonSlurper()
      //json.parseText((File(Config.MODEL_FILE)).readText())
//    model =  Parser.default().parse((File(Config.MODEL_FILE)).readText()) as JsonArray<JsonObject>
//    return new BufferedReader(new InputStreamReader(jamSketchActivity.getResources().getAssets().open(path))).lines().collect(Collectors.joining());
    model =  Parser.default().parse(
            InputStreamReader(JamSketchActivity.myResources?.getAssets()?.open(Config.MODEL_FILE))) //as JsonArray<JsonObject>
    cmx = CMXController.getInstance()
    mr = CMXController.createMusicRepresentation(Config.NUM_OF_MEASURES,
            Config.DIVISION)
    mr!!.addMusicLayerCont(OUTLINE_LAYER)
    mr!!.addMusicLayer(MELODY_LAYER, Array(12){it})
//    mr!!.addMusicLayer(CHORD_LAYER,
//                     [C, F, G] as ChordSymbol2[],
//            Config.DIVISION)
    mr!!.addMusicLayer(CHORD_LAYER,
            listOf<ChordSymbol2>(C,
                    F,
                    G),
            Config.DIVISION)
    Config.chordprog.toList().forEachIndexed { index, any ->
      mr!!.getMusicElement(CHORD_LAYER, index, 0).setEvidence(any)
    }
    var sccgen = SCCGenerator(target_part as SCCDataSet.Part, scc.division,
    OUTLINE_LAYER, ExpressionGenerator(), cfg)
    sccgen.cmx = cmx

    mr!!.addMusicCalculator(MELODY_LAYER, sccgen)
    mr!!.addMusicCalculator(OUTLINE_LAYER,
                          musicCalculatorForOutline())
  }

  abstract fun musicCalculatorForOutline(): MusicCalculator

  override fun setMelodicOutline(measure: Int, tick: Int, value: Double?) {
    var e = mr!!.getMusicElement(OUTLINE_LAYER, measure, tick)
    if (!automaticUpdate()) {
      e.suspendUpdate()
    }
    e.setEvidence(value)
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
      (0..Config.DIVISION-1).forEach { j ->
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
