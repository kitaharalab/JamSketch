package jp.kthrlab.jamsketch.controller

import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.engine.JamSketchEngine
import jp.kthrlab.jamsketch.music.data.IMusicData

/**
 * JamSketchの操作クラス
 * サーバーやクライアントとして使う場合、このクラスのインスタンスを委譲する
 */
class JamSketchController
    /**
     * コンストラクタ
     *
     * @param musicData         楽譜データ
     * @param engine            JamSketchEngine
     * @param setPianoRollDataModelFirstMeasure   CurrentBarの描画をresetするためのメソッド
     */
    (
    private var musicData: IMusicData,
    private val engine: JamSketchEngine,
    private val setPianoRollDataModelFirstMeasure: (Int) -> Unit,
    ) : IJamSketchController
{

    /**
     * 初期化する
     */
    override fun init() {
        // do nothing
    }

    /**
     * Curveを更新する
     *
     * @param channel channel
     * @param from X座標始点
     * @param thru X座標終点
     * @param y    Y座標
     */
    override fun updateCurve(channel: Int, from: Int, thru: Int, y: Int) {
        musicData.storeCurveCoordinatesByChannel(channel, from, thru, y)
    }

    override fun storeCurveCoordinates(i: Int, y: Int) {
        musicData.storeCurveCoordinates(i, y)
    }

    override fun storeCurveCoordinatesByChannel(channel: Int, i: Int, y: Int) {
        musicData.storeCurveCoordinatesByChannel(channel, i, y)
    }

    /**
     * Reset
     */
    override fun reset() {
        // reset curves
        this.musicData.resetCurves()

        // removing generated note
        this.musicData.resetNotes()

        // reset PianoRollDataModel
        this.setPianoRollDataModelFirstMeasure(musicData.initial_blank_measures)

        this.engine.resetMelodicOutline()
        this.engine.setFirstMeasure(this.musicData.initial_blank_measures)
    }
}
