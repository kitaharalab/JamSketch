package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorSimpleGuided

class JamSketchEngineSimpleGuided : JamSketchEngineAbstract() {

    override fun initLocal() {
        // Do nothing
    }

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

    override fun musicCalculatorForOutline(): NoteSeqGeneratorSimpleGuided {
        val chGuide: Int = config.music.channel_guide
        val partGuide: SCCDataSet.Part = scc!!.toDataSet().getFirstPartWithChannel(chGuide)
        return NoteSeqGeneratorSimpleGuided(
            noteLayer = MELODY_LAYER,
            chordLayer = CHORD_LAYER,
            guidepart = partGuide,
            mr = mr,
            initial_blank_measures = config.music.initial_blank_measures,
            beatsPerMeas = config.music.beats_per_measure
        )
    }

    override fun outlineUpdated(measure: Int, tick: Int) {
        // do nothing
    }

    override fun automaticUpdate(): Boolean {
        return true
    }

}
