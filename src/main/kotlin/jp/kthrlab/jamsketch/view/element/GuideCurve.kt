package jp.kthrlab.jamsketch.view.element

import jp.kthrlab.jamsketch.music.data.GuideData
import processing.core.PApplet

/**
 * Draw curves for the guide
 */
fun drawGuideCurve(pApplet: PApplet, guideData: GuideData, keyboardWidth: Int) {
    with(pApplet) {
        strokeWeight(3f)
        stroke(100f, 200f, 200f)
        with(guideData) {
            (0 until (curveGuideView!!.size - 1)).forEach { i ->
                if (curveGuideView!![i] != null &&
                    curveGuideView!![i+1] != null) {
                    line((i + keyboardWidth).toFloat(), curveGuideView!![i]!!.toFloat(),
                        (i+ keyboardWidth + 1).toFloat(), curveGuideView!![i+1]!!.toFloat())
                }
            }
        }

    }
}
