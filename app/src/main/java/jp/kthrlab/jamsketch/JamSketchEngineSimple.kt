package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.inference.MusicCalculator

class JamSketchEngineSimple : JamSketchEngineAbstract() {

  override fun musicCalculatorForOutline(): MusicCalculator {
    var noteSeqGenerator = NoteSeqGenerator(MELODY_LAYER, CHORD_LAYER, Config.BEATS_PER_MEASURE,
            Config.ENT_BIAS, model!!)
    noteSeqGenerator.cmx = cmx
    return noteSeqGenerator
  }

  override fun outlineUpdated(measure: Int, tick: Int) {
    // do nothing
  }

  override fun automaticUpdate(): Boolean {
    return true
  }

  override fun parameters(): Map<String, Double> {
    return mapOf<String, Double>()
  }

  override fun paramDesc(): Map<String, String> {
    return mapOf<String, String>()
  }

}
