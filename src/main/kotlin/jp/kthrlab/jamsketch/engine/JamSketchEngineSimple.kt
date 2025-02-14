package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorSimple

open class JamSketchEngineSimple : JamSketchEngineAbstract() {
    override fun initMusicRepresentation() {
        this.mr = CMXController.createMusicRepresentation(config.music.num_of_measures, config.music.division)
        mr.addMusicLayerCont(OUTLINE_LAYER)

        mr.addMusicLayer(
            CHORD_LAYER,
            listOf<ChordSymbol2>(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.G),
            config.music.division)
        config.music.chordprog.forEachIndexed { index, chord ->
            mr.getMusicElement(CHORD_LAYER, index, 0).setEvidence(ChordSymbol2.parse(chord))
        }

        mr.addMusicLayer(MELODY_LAYER, (0..11).toList())
    }

    override fun initLocal() {
        // Do nothing
    }

    override fun musicCalculatorForOutline(): MusicCalculator? {
        val noteSeqGenerator =
            NoteSeqGeneratorSimple(
                noteLayer = MELODY_LAYER,
                chordLayer = CHORD_LAYER,
                beatsPerMeas = config.music.beats_per_measure,
                entropy_bias = config.simple.ent_bias,
                modelPath = config.simple.model_file)
        return noteSeqGenerator
    }

    override fun outlineUpdated(measure: Int, tick: Int) {
        // do nothing
    }

    override fun automaticUpdate(): Boolean {
        return true
    }

}
