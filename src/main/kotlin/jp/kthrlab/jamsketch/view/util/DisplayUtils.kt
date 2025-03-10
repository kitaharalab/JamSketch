package jp.kthrlab.jamsketch.view.util

import java.awt.Frame
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.geom.AffineTransform


fun getScalePercentage(viewWidth: Int, viewHeight: Int): Float {
    val gd: GraphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    val usableBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
    val transform: AffineTransform = gd.defaultConfiguration.defaultTransform
    val frame = Frame().let {
        it.isUndecorated = false
        it.setSize(0,0)
        it.isVisible = true
        it
    }
    val scalePercentage = minOf( usableBounds.width.toFloat() / viewWidth.toFloat() / transform.scaleX.toFloat(),
        (usableBounds.height.toFloat() - frame.insets.top) / viewHeight.toFloat() / transform.scaleY.toFloat())

    frame.dispose()

    return  scalePercentage
}