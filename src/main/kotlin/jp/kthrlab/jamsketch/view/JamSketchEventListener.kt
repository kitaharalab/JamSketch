package jp.kthrlab.jamsketch.view

import java.util.*

/**
 * JamSketchのイベントリスナー
 */
interface JamSketchEventListener : EventListener {
    /**
     * 切断された状態を通知する
     */
    fun disconnected()
}
