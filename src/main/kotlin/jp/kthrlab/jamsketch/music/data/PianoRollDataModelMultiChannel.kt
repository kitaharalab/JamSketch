package jp.kthrlab.jamsketch.music.data

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.filewrappers.SCCDataSet.Part
import jp.crestmuse.cmx.misc.PianoRoll
import jp.kthrlab.jamsketch.config.Channel

class PianoRollDataModelMultiChannel(
    var displaysFromMeasure: Int,
    var displaysToMeasure: Int,
    private var beatPerMeasure: Int,
    var channels: MutableList<Channel>,
    var scc: SCC,
) : PianoRoll.DataModel {

    fun getPart(channel: Int): Part? {
        return scc.toDataSet().getFirstPartWithChannel(channel)
    }

    override fun getMeasureNum(): Int {
        return this.displaysToMeasure - this.displaysFromMeasure
    }

    override fun getBeatNum(): Int {
        return this.beatPerMeasure
    }

    override fun getFirstMeasure(): Int {
        return this.displaysFromMeasure
    }

    override fun setFirstMeasure(measure: Int) {
        this.displaysToMeasure = measure + this.measureNum
        this.displaysFromMeasure = measure
    }

    override fun isSelectable(): Boolean {
        return false
    }

    override fun isEditable(): Boolean {
        return false
    }

    override fun selectNote(measure: Int, beat: Double, notenum: Int) {
        // Do nothing
    }

    override fun isSelected(measure: Int, beat: Double, notenum: Int): Boolean {
        return false
    }

    override fun drawData(pianoroll: PianoRoll?) {
        // Do nothing
    }

    override fun tick2measure(tick: Long): Int {
        return (tick / this.scc.division / this.beatNum).toInt() - this.displaysFromMeasure
    }

    override fun tick2beat(tick: Long): Double {
        return (tick / this.scc.division - (tick / this.scc.division / this.beatNum * this.beatNum)).toDouble()
    }

    override fun shiftMeasure(measure: Int) {
        this.displaysFromMeasure += measure;
        this.displaysToMeasure += measure;
    }
}