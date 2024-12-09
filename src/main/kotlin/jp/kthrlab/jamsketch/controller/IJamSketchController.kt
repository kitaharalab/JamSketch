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
     * Curveを更新する
     *
     * @param from 始点
     * @param thru 終点
     * @param y    Y座標
     * @param nn   note number（Y座標をnote numberに変換した値）
     */
    fun updateCurve(from: Int, thru: Int, y: Int, nn:Double)

    /**
     * Curveを更新する
     *
     * @param i    インデックス（X座標）
     * @param y    Y座標
     */
    fun storeCursorPosition(i: Int, y: Int)

    // TODO: develop
    fun setMelodicOutline(measure: Int, tick: Int, value: Double)


        /**
     * リセットする
     */
    fun reset()

    /**
     * 仮実装
     */
//    fun addListener(listener: JamMouseListener?)
//
//    fun mouseReleased(p: Point?)
}
