package jp.kthrlab.jamsketch.view.element

import processing.core.PApplet
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

class Particle(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    val size: Float = 0.0f,
    var lifespan: Int = 0,
) {

    fun update(pApplet: PApplet) {
        // Make particles float upwards
        y -= (0.5 + Math.random()).toFloat()

        // Add horizontal movement to the particles
        x += (-0.5 + Math.random()).toFloat()

        // Decrease the lifespan
        if (lifespan >= 2) {
            lifespan -= 2
        } else
            particles.remove(this)
    }

    fun display(pApplet: PApplet) {
        val randomColor = colors.random()
        with(pApplet) {
            fill(
                color(randomColor.rgb),
                (lifespan * 255 / 100).toFloat(),
            )
            noStroke()
            ellipse(x, y, size, size)

            update(pApplet)
        }
    }
}

fun addParticle(pApplet: PApplet) {
    with(pApplet) {
        particles.add(
            Particle(
                mouseX.toFloat(),
                mouseY.toFloat(),
                (5 + Math.random() * 10).toFloat(),
                100
            )
        )
    }
}

fun drawParticles(pApplet: PApplet) {
    particles.forEach {
        it.display(pApplet)
    }
}

private val particles = CopyOnWriteArrayList<Particle>()

private val colors =
    listOf("#F1C40F", "#BDC3C7", "#E74C3C", "#ECF0F1", "#95A5A6").map {
        hexToColor(it)
    }

// Function to convert hex string to color
private fun hexToColor(hex: String): Color {
    val colorInt = java.lang.Integer.parseInt(hex.removePrefix("#"), 16)
    val r = (colorInt shr 16) and 0xFF
    val g = (colorInt shr 8) and 0xFF
    val b = colorInt and 0xFF
    return Color(r, g, b)
}