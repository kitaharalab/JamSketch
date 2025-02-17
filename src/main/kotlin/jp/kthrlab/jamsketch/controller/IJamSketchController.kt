package jp.kthrlab.jamsketch.controller

/**
 * JamSketchの操作クラスのインターフェース
 */
interface IJamSketchController {
    /**
     * 初期化する
     */
    fun init()

    /**
     * Update Curve by channel
     *
     * @param channel   MIDI channel
     * @param from
     * @param thru
     * @param y         Y-coordinate
     */
    fun updateCurve(channel: Int, from: Int, thru: Int, y: Int)


    /**
     * Store curve coordinates
     *
     * @param i    index（X-coordinate）
     * @param y    Y-coordinate
     */
    fun storeCurveCoordinates(i: Int, y: Int)

    /**
     * Store curve coordinates by channel
     *
     * @param channel   MIDI channel
     * @param i         index（X-coordinate）
     * @param y         Y-coordinate
     */
    fun storeCurveCoordinatesByChannel(channel : Int, i: Int, y: Int)


    /**
     * Reset JamSketch
     */
    fun reset()

}
