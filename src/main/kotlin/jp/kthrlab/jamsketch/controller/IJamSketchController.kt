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
     * Update Curve
     *
     * @param from 始点
     * @param thru 終点
     * @param y    Y-coordinate
     * @param nn   note number（Y座標をnote numberに変換した値）
     */
    fun updateCurve(from: Int, thru: Int, y: Int, nn:Double)

    /**
     * Update Curve by channel
     *
     * @param channel   MIDI channel
     * @param from
     * @param thru
     * @param y         Y-coordinate
     * @param nn        note number（Y座標をnote numberに変換した値）
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
    fun storeCurveCoordinates(channel : Int, i: Int, y: Int)


    // TODO: develop
    fun setMelodicOutline(measure: Int, tick: Int, value: Double)


    /**
     * Reset JamSketch
     */
    fun reset()

    /**
     * 仮実装
     */
//    fun addListener(listener: JamMouseListener?)
//
//    fun mouseReleased(p: Point?)
}
