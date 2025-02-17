package jp.kthrlab.jamsketch.view.util

import processing.core.PApplet
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import java.awt.geom.AffineTransform

fun getScalePercentage(pApplet: PApplet, viewWidth: Int, viewHeight: Int): Float {
    val gd: GraphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    val transform: AffineTransform = gd.defaultConfiguration.defaultTransform
    return minOf(pApplet.displayWidth.toFloat() / viewWidth.toFloat() / transform.scaleX.toFloat(),
            pApplet.displayHeight.toFloat() / viewHeight.toFloat() / transform.scaleY.toFloat())

}