package jp.kthrlab.jamsketch.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jp.kthrlab.jamsketch.web.ServerParameter
import jp.kthrlab.jamsketch.web.ServiceLocator
import jp.kthrlab.jamsketch.web.WebSocketApi
import org.glassfish.tyrus.server.Server
import java.io.IOException

/**
 * JamSketchを操作するクラスにサーバー送信機能を持たせたもの
 * JamSketchの操作自体は委譲によって実現する
 */
class JamSketchServerController(host: String?, port: Int, private val innerController: IJamSketchController) :
    IJamSketchController {
    /**
     * 初期化する
     */
    override fun init() {
        server.start()
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
        // 内部で持つ操作クラスで更新操作をする
        // サーバーは更新時にデータは送らないため、他の処理はしない
        innerController.updateCurve(from, thru, y, nn)
    }

    override fun updateCurve(channel: Int, from: Int, thru: Int, y: Int) {
        innerController.updateCurve(channel, thru, from, thru)
    }

    override fun storeCurveCoordinates(i: Int, y: Int) {
        innerController.storeCurveCoordinates(i, y)
    }

    override fun storeCurveCoordinates(channel: Int, i: Int, y: Int) {
        TODO("Not yet implemented")
    }

    override fun setMelodicOutline(measure: Int, tick: Int, value: Double) {
        innerController.setMelodicOutline(measure, tick, value)
    }

    /**
     * リセットする
     */
    override fun reset() {
        // 内部で持つ操作クラスでリセット処理をする
        innerController.reset()
        resetClients()
    }

//    override fun addListener(listener: JamMouseListener?) {
//        innerController.addListener(listener)
//    }
//
//    override fun mouseReleased(p: Point?) {
//        innerController.mouseReleased(p)
//    }

    fun resetClients() {
        val serviceLocator = ServiceLocator.GetInstance()
        val currentSession = serviceLocator.session

        currentSession?.let {
            val sessions = it.openSessions
            for (session in sessions) {
                try {
                    session.basicRemote.sendText(ObjectMapper().writeValueAsString(ServerParameter("reset")))
                    println(ObjectMapper().writeValueAsString(ServerParameter("reset")))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private val server =
        Server(host, port, "/websockets", null, WebSocketApi::class.java)

    /**
     * コンストラクタ
     *
     * @param host       自身のホスト
     * @param port       自身のポート
     * @param controller 内部で持つ操作クラス
     */
    init {
        this.init()
    }
}
