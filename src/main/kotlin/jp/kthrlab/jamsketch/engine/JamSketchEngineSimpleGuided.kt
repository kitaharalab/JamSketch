package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorSimpleGuided
import jp.kthrlab.jamsketch.music.generator.SCCGenerator

class JamSketchEngineSimpleGuided : JamSketchEngineAbstract() {

    override fun initLocal() {
        // Do nothing
    }

    override fun outlineUpdated(channel: Int, measure: Int, tick: Int) {
        println("outlineUpdated($channel, $measure, $tick)")

        val mr = channelMrSet.find { it.first == channel }?.second
        val e = mr?.getMusicElement(Layer.OUTLINE, measure, tick)
        e?.resumeUpdate()

        val channelCalc = channelCalcSet.find { it.first == channel }
        val noteSeqGenerator = channelCalc?.second?.get(Layer.OUTLINE)
        noteSeqGenerator?.updated(measure, tick, Layer.OUTLINE, mr)

        val sccGenerator = channelCalc?.second?.get(Layer.GEN)
        sccGenerator?.updated(measure, tick, Layer.GEN, mr)
    }

    override fun initMusicRepresentation() {
        config.channels.forEach { channel ->

            val mr = CMXController.createMusicRepresentation(config.music.num_of_measures, config.music.division)

            mr.addMusicLayerCont(Layer.OUTLINE)

            mr.addMusicLayer(
                Layer.CHORD,
                listOf<ChordSymbol2>(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.G),
                config.music.division)
            config.music.chordprog.forEachIndexed { index, chord ->
                mr.getMusicElement(Layer.CHORD, index, 0).setEvidence(ChordSymbol2.parse(chord))
            }

            mr.addMusicLayer(Layer.GEN, (0..11).toList())

            channelMrSet.add(Pair(channel.channel_number, mr))
            val mapCalc = mutableMapOf(
                Pair(Layer.OUTLINE, musicCalculatorForOutline(channel.channel_number)),
                Pair(Layer.GEN, musicCalculatorForGen(channel.channel_number))
            )
            channelCalcSet.add(Pair(channel.channel_number, mapCalc))
        }
    }

    override fun musicCalculatorForOutline(channel: Int): MusicCalculator {
        val mr = channelMrSet.find { it.first == channel }?.second
        val chGuide: Int = config.music.channel_guide
        val partGuide: SCCDataSet.Part = scc!!.toDataSet().getFirstPartWithChannel(chGuide)
        return NoteSeqGeneratorSimpleGuided(
            noteLayer = Layer.GEN,
            chordLayer = Layer.CHORD,
            guidepart = partGuide,
            mr = mr!!,
            initial_blank_measures = config.music.initial_blank_measures,
            beatsPerMeas = config.music.beats_per_measure
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

//    override fun musicCalculatorForOutline(): NoteSeqGeneratorSimpleGuided {
//        val chGuide: Int = config.music.channel_guide
//        val partGuide: SCCDataSet.Part = scc!!.toDataSet().getFirstPartWithChannel(chGuide)
//        return NoteSeqGeneratorSimpleGuided(
//            noteLayer = MELODY_LAYER,
//            chordLayer = CHORD_LAYER,
//            guidepart = partGuide,
//            mr = mr,
//            initial_blank_measures = config.music.initial_blank_measures,
//            beatsPerMeas = config.music.beats_per_measure
//        )
//    }

}
