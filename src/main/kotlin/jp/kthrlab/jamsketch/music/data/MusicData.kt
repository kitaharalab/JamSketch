package jp.kthrlab.jamsketch.music.data

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.misc.PianoRoll
import jp.crestmuse.cmx.processing.CMXController

/**
 * 音楽生成の入出力データ
 *
 * @param filename                  伴奏ファイル名
 * @param size                     カーブの座標を保持するリストのサイズ（タイムラインのwidth）
 * @param initial_blank_measures
 * @param beats_per_measure
 * @param num_of_measures
 * @param repeat_times
 * @param division                 config.music.division (scc.division は ticksPerBeat?)
 */
open class MusicData(
    override val filename: String,
    override val size: Int,
    override val initial_blank_measures: Int,
    override val beats_per_measure: Int,
    override val num_of_measures: Int,
    override val repeat_times: Int,
    override val division: Int,
    override val channel_gen: Int,
) : IMusicData {
    override var curve1: MutableList<Int?> = arrayOfNulls<Int>(size).toMutableList()
    override var scc: SCC = CMXController.readSMFAsSCC(javaClass.getResource(filename).path)

    // multi-channel
    var channelCurveSet: Set<Pair<Int, MutableList<Int?>>> = mutableSetOf()

    init {
        scc.toDataSet().repeat(
            (initial_blank_measures * beats_per_measure * scc.division).toLong(),
            ((initial_blank_measures + num_of_measures) * beats_per_measure * scc.division).toLong(),
            repeat_times - 1
        )
    }

//    fun getPianoRollDataModel(displaysFrom: Int, displaysTo: Int): PianoRoll.DataModel {
//        return PianoRollDataModelMultiChannel(displaysTo, displaysFrom,  division, beats_per_measure)
//    }

    // IMusicData
    override fun addCurveByChannel(channel: Int, curve: MutableList<Int?>) {
        channelCurveSet += Pair(channel, curve)
    }

    override fun resetCurves() {
        channelCurveSet.forEach { (channel, curve) ->
            curve.fill(null)
        }
    }

    override fun resetNotes() {
        // remove notes for curve1 and curves
        // tickPosition must be 0
        CMXController.getInstance().setTickPosition(0)
        // remove notes for curve1
        val part = scc.toDataSet().getFirstPartWithChannel(channel_gen)
        part.remove(part.noteList.toList())

        // remove notes for curves
        channelCurveSet.forEach { (channel, curve) ->
            val channelPart = scc.toDataSet().getFirstPartWithChannel(channel)
            channelPart.remove(channelPart.noteList.toList())
        }

    }

    override fun storeCurveCoordinatesByChannel(channel: Int, from: Int, thru: Int, y: Int) {
        channelCurveSet.find { it.first == channel }?.let { (_, curve) ->
            (from..thru).forEach { i: Int -> curve[i] = y }
        }
    }

    override fun storeCurveCoordinatesByChannel(channel: Int, i: Int, y: Int) {
        channelCurveSet.find { it.first == channel }?.let { (_, curve) ->
            curve[i] = y
        }
    }

    override fun resetCurve() {
        curve1.fill(null)
        resetCurves()
    }

    override fun storeCurveCoordinates(from: Int, thru: Int, y: Int) {
        (from..thru).forEach { i: Int -> curve1[i] = y }
    }

    override fun storeCurveCoordinates(i: Int, y: Int) {
        curve1[i] = y
    }

}
