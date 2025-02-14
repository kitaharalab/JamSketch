package jp.kthrlab.jamsketch.music.data

open class ObservableMusicData(
    val delegate: IMusicData,
    var onChange: ((channel: Int, from: Int, thru: Int, y: Int) -> Unit)? = null
) : IMusicData by delegate {

    override fun storeCurveCoordinatesByChannel(channel: Int, from: Int, thru: Int, y: Int) {
        delegate.storeCurveCoordinatesByChannel(channel, from, thru, y)
        onChange?.invoke(channel, from, thru, y)
    }

    override fun storeCurveCoordinatesByChannel(channel: Int, i: Int, y: Int) {
        delegate.storeCurveCoordinatesByChannel(channel, i, i, y)
        onChange?.invoke(channel, i, i, y)
    }

    override fun storeCurveCoordinates(from: Int, thru: Int, y: Int) {
        delegate.storeCurveCoordinates(from, thru, y)
        onChange?.invoke(channel_gen, from, thru, y)
    }

    override fun storeCurveCoordinates(i: Int, y: Int) {
        delegate.storeCurveCoordinates(i, y)
        onChange?.invoke(channel_gen, i, i, y)
    }
}
