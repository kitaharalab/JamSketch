package jp.kthrlab.jamsketch.controller

import jp.crestmuse.cmx.processing.CMXController
import jp.kthrlab.jamsketch.engine.JamSketchEngine
import jp.kthrlab.jamsketch.music.data.MusicData

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
    private var musicData: MusicData,
    private val engine: JamSketchEngine,
    private val setPianoRollDataModelFirstMeasure: (Int) -> Unit,
    ) : IJamSketchController
{
//    override fun addListener(listener: JamMouseListener?) {
//        listeners.add(listener)
//    }
//
//    override fun mouseReleased(p: Point?) {
//        for (m in listeners) m.mouseReleased(p)
//    }

    /**
     * 初期化する
     */
    override fun init() {
        // do nothing
    }

    /**
     * Curveを更新する
     *
     * @param from 始点
     * @param thru 終点
     * @param y    Y座標
     * @param nn   note number（Y座標をnote numberに変換した値）
     */
    override fun updateCurve(from: Int, thru: Int, y: Int, nn: Double) {

        val size2 = musicData.num_of_measures * musicData.division
        val curveSize = musicData.curve1.size

        for (i in (from..thru)) {
            if (0 <= i) {
                // Store CursorPosition
                storeCursorPosition(i, y)

                // setEvidence (OUTLINE_LAYER)
                println("var nn: $nn curve1[$i] == ${musicData.curve1[i]}")
                val position: Int = (i * size2 / curveSize)
                if (position >= 0) {
                    setMelodicOutline((position / musicData.division), position % musicData.division, nn)
                }
            }
        }
    }

    override fun storeCursorPosition(i: Int, y: Int) {
        musicData.storeCursorPosition(i, y)
    }

   override fun setMelodicOutline(measure: Int, tick: Int, value: Double) {
        engine.setMelodicOutline(measure, tick, value)
    }

    /**
     * リセットする
     */
    override fun reset() {

        this.musicData.resetCurve()

        // removing generated note
        // tickPosition must be 0
        CMXController.getInstance().setTickPosition(0)
        val part = (this.musicData.scc.toDataSet()).getFirstPartWithChannel(musicData.channel_acc)
        part.remove(part.noteList.toList())

        // reset PianoRollDataModel
        setPianoRollDataModelFirstMeasure(musicData.initial_blank_measures)

        this.engine.resetMelodicOutline()
        this.engine.setFirstMeasure(this.musicData.initial_blank_measures)

    }
}
