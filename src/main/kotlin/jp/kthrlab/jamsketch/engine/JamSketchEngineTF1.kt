package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorTF1
import jp.kthrlab.jamsketch.music.generator.SCCGenerator

class JamSketchEngineTF1: JamSketchEngineAbstract() {

    override fun initLocal() {
        // Do nothing
    }

    override fun initMusicRepresentation() {
        config.channels.forEach { channel ->
            val mr = CMXController.createMusicRepresentation(config.music.num_of_measures, config.music.division)

            // Layer.OUTLINE
            mr.addMusicLayerCont(Layer.OUTLINE)

            // Layer.CHORD
            mr.addMusicLayer(
                Layer.CHORD,
                listOf<ChordSymbol2>(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.G),
                config.music.division
            )
            config.music.chordprog.forEachIndexed { index, chord ->
                mr.getMusicElement(Layer.CHORD, index, 0).setEvidence(ChordSymbol2.parse(chord))
            }

            // Layer.GEN
            mr.addMusicLayer(Layer.GEN, Array(config.tf.tf_note_con_col_start){it})

            channelMrSet.add(Pair(channel.channel_number, mr))
            val mapCalc = mutableMapOf(
                Pair(Layer.OUTLINE, musicCalculatorForOutline(channel.channel_number)),
                Pair(Layer.GEN, musicCalculatorForGen(channel.channel_number))
            )
            channelCalcSet.add(Pair(channel.channel_number, mapCalc))
        }

    }

    override fun musicCalculatorForOutline(channel: Int): MusicCalculator {
        return NoteSeqGeneratorTF1(
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

    override fun musicCalculatorForGen(channel: Int): MusicCalculator {
        return SCCGenerator(
            scc.toDataSet().getFirstPartWithChannel(channel),
            scc.division,
            Layer.OUTLINE,
            null,
            config.music.division,
            config.music.beats_per_measure,
        )
    }

    override fun outlineUpdated(channel: Int, measure: Int, tick: Int) {
        println("outlineUpdated($channel, $measure, $tick)")

        val mr = channelMrSet.find { it.first == channel }?.second
        val e = mr?.getMusicElement(Layer.OUTLINE, measure, tick)
        e?.resumeUpdate()

        val calcMap = channelCalcSet.find { it.first == channel }?.second
        calcMap?.get(Layer.OUTLINE)?.updated(measure, tick, Layer.OUTLINE, mr)
        calcMap?.get(Layer.GEN)?.updated(measure, tick, Layer.GEN, mr)
    }

}

