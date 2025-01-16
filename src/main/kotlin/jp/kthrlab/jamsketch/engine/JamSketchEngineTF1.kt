package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorTF1

class JamSketchEngineTF1: JamSketchEngineAbstract() {

    private lateinit var noteSeqGeneratorTf1: NoteSeqGeneratorTF1

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

        mr.addMusicLayer(MELODY_LAYER, Array(config.tf.tf_note_con_col_start){it})
    }

        override fun initLocal() {
            noteSeqGeneratorTf1 = NoteSeqGeneratorTF1(
                config.music.num_of_measures,
                config.music.melody_execution_span,
                config.tf.tf_model_dir,
                config.tf.tf_model_layer,
                config.tf.tf_note_num_start,
                config.tf.tf_rest_col,
                config.tf.tf_chord_col_start,
                config.tf.tf_num_of_melody_element,
            )

        }

    override fun musicCalculatorForOutline() : MusicCalculator? {
        return null
    }

    override fun outlineUpdated(measure: Int, tick: Int) {
        noteSeqGeneratorTf1.updated(measure, tick, OUTLINE_LAYER, mr)
    }

    override fun automaticUpdate(): Boolean {
        return false
    }
}

