package jp.kthrlab.jamsketch.view

import javax.swing.JOptionPane
import javax.swing.JPanel

/**
 * JamSketchのイベントリスナー
 */
class JamSketchEventListenerImpl
/**
 * コンストラクタ
 *
 * @param panel ダイアログ
 */(private val panel: JPanel) : JamSketchEventListener {
    /**
     * 切断された状態を通知する
     */
    override fun disconnected() {
        JOptionPane.showConfirmDialog(null, panel, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
    }
}
