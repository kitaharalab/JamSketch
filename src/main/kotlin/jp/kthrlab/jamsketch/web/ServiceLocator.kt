package jp.kthrlab.jamsketch.web

import jakarta.websocket.Session
import jp.kthrlab.jamsketch.controller.IJamSketchController

/**
 * サービスロケーター
 * サーバークラスに引数でオブジェクトを渡せないため、そのために用意した
 */
class ServiceLocator
/**
 * コンストラクタ
 */
private constructor() {
    /**
     * 操作クラスを設定する
     *
     * @param controller 操作クラスのインスタンス
     */
    fun setContoller(controller: IJamSketchController): IJamSketchController {
        return controller.also { this.contoller = it }
    }

    /**
     * セッションを設定する
     *
     * @param session セッションのインスタンス
     */
    fun setSession(session: Session): Session {
        return session.also { this.session = it }
    }

    /**
     * 操作クラスを取得する
     */
    var contoller: IJamSketchController? = null
        private set

    /**
     * セッションを取得する
     */
    var session: Session? = null
        private set

    companion object {
        /**
         * インスタンスを取得する
         */
        fun GetInstance(): ServiceLocator {
            return instance
        }

        private val instance = ServiceLocator()
    }
}
