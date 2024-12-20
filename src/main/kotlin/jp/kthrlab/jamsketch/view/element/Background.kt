package jp.kthrlab.jamsketch.view.element

import processing.core.PApplet
import processing.core.PImage

fun drawBGImage(pApplet: PApplet) {
    with(pApplet) {
        if (backgroundImage == null) {
            backgroundImage = loadImage(javaClass.getResource("/images/background.png").path)
        }
        tint(255f, 64f);
        image(
            backgroundImage,
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
        )
        noTint()
    }
}

private var backgroundImage: PImage? = null