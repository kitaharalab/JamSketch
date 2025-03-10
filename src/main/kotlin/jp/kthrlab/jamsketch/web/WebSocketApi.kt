package jp.kthrlab.jamsketch.web

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint
import jp.kthrlab.jamsketch.web.ServiceLocator.Companion.GetInstance
import java.io.IOException

/**
 * WebSocketのサーバークラス
 */
@ServerEndpoint("/WebSocketApi")
class WebSocketApi {
    /**
     * 接続時のコールバック
     *
     * @param session セッション
     * @param ec      エンドポイントの設定
     */
    @OnOpen
    fun onOpen(session: Session?, ec: EndpointConfig?) {
        // セッションを記録する
        this.currentSession = session
        val serviceLocator = GetInstance()
        serviceLocator.setSession(currentSession!!)
    }

    /**
     * メッセージ受信時のコールバック
     *
     * @param message メッセージ
     */
    @OnMessage
    @Throws(IOException::class)
    fun receiveMessage(message: String?) {
        // Print a message to the console
        println(message)

        try {
            // Get an instance of the controller class
            val serviceLocator = GetInstance()
            val controller = serviceLocator.contoller

            // Decode messages sent in JSON
            val mapper = ObjectMapper()
            val info = mapper.readValue(message, ClientParameter::class.java)

            // Update music data using controller classes
//            if (info.channel == null) {
//                controller!!.updateCurve(info.from, info.thru, info.y, info.nn)
//            } else {
                controller!!.updateCurve(info.channel!!, info.from, info.thru, info.y)
//            }
        } catch (e: Exception) {
            // Print an exception to the console
            println(e)
        }
    }

    /**
     * 接続終了時のコールバック
     *
     * @param session セッション
     * @param reason  終了原因
     */
    @OnClose
    fun onClose(session: Session?, reason: CloseReason?) {
    }

    /**
     * 接続エラー時のコールバック
     *
     * @param t エラー内容
     */
    @OnError
    fun onError(t: Throwable?) {
    }

    var currentSession: Session? = null
}
