package jp.kthrlab.jamsketch.music.data

import jp.crestmuse.cmx.filewrappers.SCC
import jp.crestmuse.cmx.filewrappers.SCCDataSet
import jp.crestmuse.cmx.processing.CMXApplet

// TODO: Stop passing notenum2y & beat2x
class GuideData(
    filename: String?,
    val size: Int,
    val initial_blank_measures: Int,
    val beats_per_measure: Int,
    val num_of_measures: Int,
    val repeat_times: Int,
    val guide_smoothness: Int,
    val channel_guide: Int,
    val keyboard_width: Int,
    val notenum2y: (Double) -> Double,
    val beat2x: (Int, Double) -> Double,
) {
    var curveGuide: List<Int?>
    var curveGuideView: List<Int?>? = null
    var fromMeasure: Int = 0
    var sccDataSet: SCCDataSet = ((CMXApplet.readSMFAsSCC(javaClass.getResource("/${filename}").path)) as SCCDataSet)

    init {
        val guide_part = sccDataSet.getFirstPartWithChannel(channel_guide)
        val smoothness: Int = guide_smoothness
        curveGuide = createCurve(guide_part, smoothness,)
        updateCurveGuideView(0, size)
    }

    fun createCurve(part: SCC.Part,
                    smoothness: Int,
    ): List<Int?> {
        val curve = arrayOfNulls<Int>(size * repeat_times).toMutableList()
        val beats = beats_per_measure
        val initial = initial_blank_measures
        part.noteList.forEach { note ->
                try {
                    val y: Double = notenum2y(note.notenum() - 0.5)
                    val onset: Int = (note.onset(480) / 480).toInt()
                    val m1: Int = (onset / beats)
                    val b1 = onset - m1 * beats
                    val x1: Double = beat2x(m1 - initial, b1.toDouble()) - keyboard_width
                    val offset: Int = (note.offset(480) / 480).toInt()
                    val m2: Int = (offset / beats)
                    val b2 = offset - m2 * beats
                    val x2: Double = beat2x(m2 - initial, b2.toDouble()) - keyboard_width
                    for (x in x1.toInt()..x2.toInt()) curve[x] = y.toInt()
                } catch (e: UnsupportedOperationException) {
                }
            }
        return smoothCurve(curve, smoothness)
    }

    fun smoothCurve(curve: List<Int?>, K: Int): List<Int?> {
        println("K=$K")
        val curve2 = curve.toTypedArray().clone()
        for (i in 0 until curve.size) {
            if (curve[i] != null) {
                var n = 1
                for (k in IntRange(1, K)) {
                    if (i - k >= 0 && curve[i - k] != null) {
                        curve2[i] = curve2[i]!! + curve[i - k]!!
                        n++
                    }
                    if (i + k < curve.size && curve[i + k] != null) {
                        curve2[i] = curve2[i]!! + curve[i+k]!!
                        n++
                    }
                }
                curve2[i] = curve2[i]!! / n
            }
        }
        return curve2.toList()
    }

    fun shiftCurveGuideView() {
        fromMeasure += num_of_measures
        updateCurveGuideView(fromMeasure/num_of_measures * size, (fromMeasure + num_of_measures)/num_of_measures * size)
    }

    fun updateCurveGuideView(from: Int, to: Int) {
        curveGuideView = arrayOfNulls<Int?>(size).toMutableList()
        if (from < curveGuide.size) {
            val toIndex = if ((to <= curveGuide.size)) to else curveGuide.size
            //            println("from = ${from}, toIndex = ${toIndex}")
            curveGuideView = curveGuide.subList(from, toIndex)
            //            println(curveGuideView)
        }
    }

}
