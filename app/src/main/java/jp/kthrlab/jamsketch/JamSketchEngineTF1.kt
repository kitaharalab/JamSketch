package jp.kthrlab.jamsketch

import jp.crestmuse.cmx.inference.MusicCalculator

class JamSketchEngineTF1 : JamSketchEngineAbstract() {

    private var tf1Worker: TF1Worker = TF1Worker()

    override fun addMusicLayerLocal() {
        mr!!.addMusicLayer(MELODY_LAYER, Array(Config.TF_NOTE_CON_COL_START){it})
    }

    override fun initLocal() {
        // Do nothing
    }

    override fun parameters(): Map<String, Double> {
        return emptyMap()
    }

    override fun paramDesc(): Map<String, String> {
        return emptyMap()
    }

    override fun musicCalculatorForOutline() : MusicCalculator? {
        return null
    }

    override fun outlineUpdated(measure: Int, tick: Int) {
        tf1Worker.updated(measure, tick, OUTLINE_LAYER, mr)
    }

    override fun automaticUpdate(): Boolean {
        return false
    }
}

