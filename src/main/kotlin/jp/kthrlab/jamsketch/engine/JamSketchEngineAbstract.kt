package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.inference.MusicCalculator
import jp.crestmuse.cmx.inference.MusicRepresentation
import jp.crestmuse.cmx.misc.ChordSymbol2
import jp.crestmuse.cmx.misc.ChordSymbol2.NON_CHORD
import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.config.AccessibleConfig
import jp.kthrlab.jamsketch.config.IConfigAccessible
import jp.kthrlab.jamsketch.music.generator.SCCGenerator
import kotlin.random.Random

abstract class JamSketchEngineAbstract : JamSketchEngine, IConfigAccessible {
    override var config = AccessibleConfig.config
    var cmx = CMXController.getInstance()
    lateinit var scc: SCC

    abstract fun initLocal()
    abstract fun outlineUpdated(channel: Int, measure: Int, tick: Int)
    abstract fun initMusicRepresentation()
    abstract fun musicCalculatorForOutline(channel: Int): MusicCalculator
    abstract fun musicCalculatorForGen(channel: Int): MusicCalculator

    var channelMrSet: MutableSet<Pair<Int, MusicRepresentation>> = mutableSetOf()
    var channelCalcSet: MutableSet<Pair<Int, MutableMap<String, MusicCalculator>>> = mutableSetOf()

    val fullChordProgression: Any
        get() = List(config.music.initial_blank_measures) { NON_CHORD } +
                List(config.music.repeat_times) { config.music.chordprog.toList()}.flatten()

    object Layer {
        const val OUTLINE = "outline"
        const val GEN = "gen"
        const val CHORD = "chord"
    }

    override fun init(scc: SCC) {
        this.scc = scc

        config.channels.forEach { channel ->
            scc.toDataSet().addPart(Random(channel.channel_number).nextInt(), channel.channel_number)
                .addProgramChange(0, channel.program_number)
        }

        initLocal()
        initMusicRepresentation()
    }

    override fun setMelodicOutline(channel: Int, measure: Int, tick: Int, value: Double) {
        println("setMelodicOutline($channel, $measure, $tick, $value)")
        val mr = channelMrSet.find { it.first == channel }?.second
//        println("mr == $mr ${channelMrSet.size}")
        val e = mr?.getMusicElement(Layer.OUTLINE, measure, tick)
        e?.let {
//            it.suspendUpdate()
            it.setEvidence(value)
            outlineUpdated(channel, measure, tick)
        }
    }

    override fun getMelodicOutline(channel: Int, measure: Int, tick: Int): Double {
        val channelMr = channelMrSet.find { it.first == channel }?.second
        return if (channelMr != null) {
            (channelMr.getMusicElement(Layer.OUTLINE, measure, tick)?.mostLikely) as Double
        } else {
            Double.NaN
        }
    }

    override fun getChord(channel: Int, measure: Int, tick: Int): ChordSymbol2? {
        val mr = channelMrSet.find { it.first == channel }?.second
        return if (mr != null) {
            (mr.getMusicElement(Layer.CHORD, measure, tick).mostLikely) as ChordSymbol2
        } else  {
            return ChordSymbol2.NON_CHORD
        }
    }

    override fun resetMelodicOutline(channel: Int) {
        val myMr = channelMrSet.find { it.first == channel }?.second
        myMr?.let {
            it.getMusicElementList(Layer.OUTLINE).forEach { element ->
                it.getMusicElement(Layer.OUTLINE, element.measure(), element.tick()).setEvidence(Double.NaN)
            }
        }
    }

    override fun getChord(measure: Int, tick: Int): ChordSymbol2? {
        // Do nothing for multichannel
        return ChordSymbol2.NON_CHORD
    }

    override fun setFirstMeasure(number: Int) {
        SCCGenerator.firstMeasure = number
    }

    override fun resetMelodicOutline() {
        // Do nothing
    }
}