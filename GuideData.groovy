import groovy.json.JsonSlurper

import static JamSketch.CFG

class GuideData {
    def curveGuide
    def curveGuideView // y coord per pixel
    def from = 0
    def size

    GuideData(size) {
        this.size = size
        // TODO: create curveGuide
        def json = new JsonSlurper()
        curveGuide = json.parseText((new File("curve.json")).text)
        updateCurve(from, size)
    }

    def shiftCurve() {
        curveGuideView = [null] * size
        updateCurve(from += size, from + size)
    }

    def updateCurve(from, to) {
        if (from < curveGuide.size()) {
            def toIndex = (to <= curveGuide.size()) ? to : curveGuide.size()
//            println("from = ${from}, toIndex = ${toIndex}")
            curveGuideView = curveGuide.subList(from, toIndex)
//            println(curveGuideView)
        }
    }

}