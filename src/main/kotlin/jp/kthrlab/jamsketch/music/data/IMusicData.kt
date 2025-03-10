package jp.kthrlab.jamsketch.music.data

import jp.crestmuse.cmx.filewrappers.SCC

interface IMusicData {
    val filename: String
    val size: Int
    val initial_blank_measures: Int
    val beats_per_measure: Int
    val num_of_measures: Int
    val repeat_times: Int
    val division: Int
    var scc: SCC

    /**
     * Reset curves and notes
     */
    fun resetMusicData()

    fun resetCurves()
    fun resetNotes()
    fun storeCurveCoordinates(i: Int, y: Int)
    fun storeCurveCoordinates(from: Int, thru: Int, y: Int)
    fun storeCurveCoordinatesByChannel(channel: Int, i: Int, y: Int)
    fun storeCurveCoordinatesByChannel(channel: Int, from: Int, thru: Int, y: Int)
    fun addCurveByChannel(channel: Int, curve: MutableList<Int?>)
}