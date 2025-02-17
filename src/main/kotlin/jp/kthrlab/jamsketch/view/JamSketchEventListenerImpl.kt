package jp.kthrlab.jamsketch.view

import javax.swing.BoxLayout
import javax.swing.JLabel
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
    override fun disconnected(message: String) {
        showDialog(message)
    }

    override fun error(message: String) {
        showDialog(message)
    }

    private fun showDialog(message: String) {
        val component = panel.getComponent(0)
        if (component is JLabel) {
            component.text = message
        }
        JOptionPane.showConfirmDialog(null, panel, "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE)
    }
}
