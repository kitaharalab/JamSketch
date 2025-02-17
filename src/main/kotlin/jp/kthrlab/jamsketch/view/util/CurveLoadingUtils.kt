package jp.kthrlab.jamsketch.view.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll
import jp.kthrlab.jamsketch.music.data.MusicData
import processing.core.PApplet
import java.io.File

fun loadFileSelected(
    selection: File?,
    musicData: MusicData,
    pApplet: PApplet,
    width: Int,
    keyboardWidth: Int,
    notenum2y: (Double) -> Double,
        ) {
//    // What is this?
//    var debugModeDraw: Int = 0
//
//    if (selection == null) {
//        SimplePianoRoll.println("Window was closed or the user hit cancel.")
//    } else {
//        val absolutePath = selection.absolutePath
//        SimplePianoRoll.println("User selected $absolutePath")
//        if (absolutePath.endsWith(".json")) {
//            val objectMapper = jacksonObjectMapper()
//            musicData.curve1 = objectMapper.readValue(selection)
//            val count: Int = (musicData.curve1 as Array<*>).size
//// TODO: call updateCurve after this method
////            updateCurve(0, timelineWidth - debugModeDraw)
//        } else if (selection.canonicalPath.endsWith(".txt")) {
//            val table = pApplet.loadTable(absolutePath, "csv")
//            musicData.curve1 = arrayOfNulls<Int>(width - debugModeDraw).toMutableList()
//            val n = table.rowCount
//            val m: Int = (musicData.curve1 as Array<Int>).size
//            for (i in IntRange(keyboardWidth.toInt(), (m - 1))) {
//                val from = (i - keyboardWidth) as Int * n / m
//                val thru = ((i + 1) - keyboardWidth) as Int * n / m - 1
//                val range = (from..thru).toList()
//                val collect = range.map { notenum2y(table.getFloat(it, 0) as Double) }
//                val sum = collect.sum()
//                (musicData.curve1 as Array<Int>)[i] = (sum / range.size).toInt()
//
//            }
//// TODO: call updateCurve after this method
////            updateCurveLocal(0, timelineWidth - debugModeDraw)
//
//        } else {
//            SimplePianoRoll.println("File is not supported")
//            return
//        }
//    }
}

fun loadOutlineLayerData(inputFilePath: String, musicData: MusicData) {
    val jsonFile = File(inputFilePath)
    val mapper = jacksonObjectMapper()
    var jsonData : Array<Int> = mapper.readValue(jsonFile)
//    jsonData.forEachIndexed { index, value ->
//        (musicData.curve1 as Array<Int>)[index] = value
//    }
// TODO: call updateCurve after this method
//    updateCurveLocal(0, timelineWidth)
}

