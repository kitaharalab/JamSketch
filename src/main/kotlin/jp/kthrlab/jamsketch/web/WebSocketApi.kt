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
        // メッセージをコンソールに出力する
        println(message)

        try {
            // 操作クラスのインスタンスを取得する
            val serviceLocator = GetInstance()
            val controller = serviceLocator.contoller

            // JSONで送られたメッセージをデコードする
            val mapper = ObjectMapper()
            val info = mapper.readValue(message, ClientParameter::class.java)

            // 操作クラスを使って楽譜データを更新する
            controller!!.updateCurve(info.from, info.thru, info.y, info.nn)
        } catch (e: Exception) {
            // 例外をコンソールに出力する
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
