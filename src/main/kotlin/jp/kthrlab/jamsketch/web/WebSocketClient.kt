package jp.kthrlab.jamsketch.web

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.websocket.*
import jp.kthrlab.jamsketch.view.JamSketchEventListener
import java.net.URI

/**
 * WebSocketのクライアントクラス
 */
@ClientEndpoint
class WebSocketClient {
    /**
     * コンストラクタ
     *
     * @param listner イベントリスナー
     */
    constructor()
//    fun WebSocketClinet(): Any {
//        return invokeMethod("super", arrayOfNulls<Any>(0))
//    }

    /**
     * 接続時のコールバック
     *
     * @param session セッション
     */
    @OnOpen
    fun onOpen(session: Session?) {
    }

    /**
     * メッセージ受信時のコールバック
     *
     * @param message メッセージ
     */
    @OnMessage
    fun onMessage(message: String?) {
        println(message)

        try {
            val serviceLocator =
                ServiceLocator.GetInstance()
            val controller = serviceLocator.contoller
            val mapper = ObjectMapper()
            val info = mapper.readValue(message, ServerParameter::class.java)

            if (info.mode == "reset") {
                controller!!.reset()
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    /**
     * エラー時のコールバック
     *
     * @param th エラー内容
     */
    @OnError
    fun onError(th: Throwable?) {
        listener!!.disconnected()
    }

    /**
     * 終了時のコールバック
     *
     * @param session セッション
     */
    @OnClose
    fun onClose(session: Session?) {
        listener!!.disconnected()
    }

    /**
     * メッセージ送信処理
     *
     * @param object 送信オブジェクト
     */
    fun Send(`object`: Any?) {
        session!!.basicRemote.sendText(ObjectMapper().writeValueAsString(`object`))
    }

    /**
     * 初期化処理
     *
     * @param host 接続先ホスト
     * @param port 接続先ポート
     */
    fun Init(host: String, port: Int, listener: JamSketchEventListener?) {
        // 初期化のため WebSocket コンテナのオブジェクトを取得する
        val container = ContainerProvider.getWebSocketContainer()
        // サーバー・エンドポイントの URI
        val uri = URI.create("ws://$host:$port/websockets/WebSocketApi")
        this.session = container.connectToServer(this, uri)

        this.listener = listener
    }

    private var session: Session? = null
    private var listener: JamSketchEventListener? = null
}
