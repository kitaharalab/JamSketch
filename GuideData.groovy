import groovy.json.JsonSlurper
import static JamSketch.CFG

class GuideData {
    def curveGuide
    def curveGuideView // y coord per pixel
    def from = 0
    def size
    def scc

    GuideData(filename, size, cmxcontrol) {
        this.size = size
        scc = cmxcontrol.readSMFAsSCC(filename)
        def guide_part = scc.getFirstPartWithChannel(CFG.CHANNEL_GUIDE)

        curveGuide = createCurve(guide_part)
        updateCurve(from, size)
    }

    def createCurve(part) {
        // TODO: create curveGuide
        def json = new JsonSlurper()
        return json.parseText((new File("curve.json")).text)
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