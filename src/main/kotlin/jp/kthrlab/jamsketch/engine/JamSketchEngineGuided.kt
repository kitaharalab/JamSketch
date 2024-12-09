package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorSimpleGuided

class JamSketchEngineGuided : JamSketchEngineAbstract() {

    override fun initLocal() {
        // Do nothing
    }

    override fun initMusicRepresentationLocal() {
        // Do nothing
    }

    override fun musicCalculatorForOutline(): NoteSeqGeneratorSimpleGuided {
        val chGuide: Int = config.music.channel_guide
        val partGuide: SCCDataSet.Part = scc!!.toDataSet().getFirstPartWithChannel(chGuide)
        return NoteSeqGeneratorSimpleGuided(
            MELODY_LAYER,
            CHORD_LAYER,
            partGuide,
            mr,
            config.music.initial_blank_measures,
            config.music.beats_per_measure
        )
    }

    override fun outlineUpdated(measure: Int, tick: Int) {
        // do nothing
    }

    override fun automaticUpdate(): Boolean {
        return true
    }

    fun parameters(): Map<String, Double> {
        return LinkedHashMap()
    }

    fun paramDesc(): Map<String, String> {
        return LinkedHashMap()
    }
}
