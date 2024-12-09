package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.inference.MusicCalculator
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorTF1

class JamSketchEngineTF1: JamSketchEngineAbstract() {

    private lateinit var noteSeqGeneratorTf1: NoteSeqGeneratorTF1

    override fun initMusicRepresentationLocal() {
            mr.addMusicLayer(MELODY_LAYER, Array(config.tf.tf_note_con_col_start){it})
        }

        override fun initLocal() {
            noteSeqGeneratorTf1 = NoteSeqGeneratorTF1(
                config.music.num_of_measures,
                config.music.division,
                config.music.melody_execution_span,
                config.tf.tf_model_dir,
                config.tf.tf_model_layer,
                config.tf.tf_note_num_start,
                config.tf.tf_model_input_col,
                config.tf.tf_model_output_col,
                config.tf.tf_rest_col,
                config.tf.tf_chord_col_start,
                config.tf.tf_num_of_melody_element,
            )

        }

//    override fun parameters(): Map<String, Double> {
//        return emptyMap()
//    }
//
//    override fun paramDesc(): Map<String, String> {
//        return emptyMap()
//    }

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

