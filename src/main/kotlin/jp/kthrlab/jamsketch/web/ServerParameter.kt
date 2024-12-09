package jp.kthrlab.jamsketch.web

/**
 * サーバーの送信用パラメータ
 */
class ServerParameter {
    /**
     * デフォルトコンストラクタ
     * これが無いとJSONをデリシアライズできない
     */
    constructor()

    /**
     * コンストラクタ
     *
     * @param mode 操作モード
     */
    constructor(mode: String?) {
        this.mode = mode
    }

    var mode: String? = null
}
