package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.music.generator.NoteSeqGeneratorSimple
import jp.kthrlab.jamsketch.music.generator.SCCGenerator
import kotlin.random.Random

class JamSketchEngineMultichannelSimple : JamSketchEngineMultichannelAbstract(){

    override fun initLocal() {
        config.channels.forEach { channel ->
            scc.toDataSet().addPart(Random(channel.number).nextInt(), channel.number)
                .addProgramChange(0, channel.program)
        }
    }

    override fun initMusicRepresentation() {
        val mr = CMXController.createMusicRepresentation(config.music.num_of_measures, config.music.division)

        mr.addMusicLayerCont(Layer.OUTLINE)

        mr.addMusicLayer(
            Layer.CHORD,
            listOf<ChordSymbol2>(ChordSymbol2.C, ChordSymbol2.F, ChordSymbol2.G),
            config.music.division
        )
        config.music.chordprog.forEachIndexed { index, chord ->
            mr.getMusicElement(Layer.CHORD, index, 0).setEvidence(ChordSymbol2.parse(chord))

        }

        // temp
        mr.addMusicLayer(Layer.GEN, (0..11).toList())
        config.channels.forEach { channel ->
            channelMrSet.add(Pair(channel.number, mr))
            val mapCalc = mutableMapOf(Pair(Layer.OUTLINE, musicCalculatorForOutline()), Pair(Layer.GEN, musicCalculatorForGen(channel.number)))
            channelCalcSet.add(Pair(channel.number, mapCalc))
        }

    }

    override fun musicCalculatorForOutline(): MusicCalculator {
        return NoteSeqGeneratorSimple(
            noteLayer = Layer.GEN,
            chordLayer = Layer.CHORD,
            beatsPerMeas = config.music.num_of_measures,
            modelPath =  config.simple.model_file,
            entropy_bias = config.simple.ent_bias,
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
//        println("    mr == $mr")

        val e = mr?.getMusicElement(Layer.OUTLINE, measure, tick)
//        println("    e == $e")
        e?.resumeUpdate()

        val noteSeqGenerator = channelCalcSet.find { it.first == channel }?.second?.get(Layer.OUTLINE)
//        println("    noteSeqGenerator == $noteSeqGenerator")
        noteSeqGenerator?.updated(measure, tick, Layer.OUTLINE, mr)

        val sccGenerator = channelCalcSet.find { it.first == channel }?.second?.get(Layer.GEN)
        sccGenerator?.updated(measure, tick, Layer.GEN, mr)
    }


}