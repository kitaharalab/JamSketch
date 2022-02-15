package jp.kthrlab.jamsketch

class ExpressionGenerator {
    var tflClient = TFLTestClient(JamSketchActivity.myResources!!.assets)
    init {
        tflClient.load()
    }
    fun execute(fromTick: Int, thruTick: Int, division: Int) {
        tflClient.recommend(fromTick, division)
    }

}
