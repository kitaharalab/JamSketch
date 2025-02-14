package jp.kthrlab.jamsketch.engine

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.misc.ChordSymbol2

/**
 * IJamSketchEngineMultichannel does not need JamSketchEngine methods,
 * but inherits them to make it recognized as JamSketchEngine.
 */
interface IJamSketchEngineMultichannel : JamSketchEngine {

    fun init(scc: SCC)

    fun setMelodicOutline(channel: Int, measure: Int, tick: Int, value: Double)

    fun getMelodicOutline(channel: Int, measure: Int, tick: Int): Double

    fun getChord(channel: Int, measure: Int, tick: Int): ChordSymbol2?

    fun resetMelodicOutline(channel: Int)

}