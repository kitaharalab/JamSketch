package jp.kthrlab.jamsketch.view.util

import controlP5.ControlP5
import jp.kthrlab.jamsketch.config.Channel
import java.awt.Color

fun addButtons(p5ctrl: ControlP5, mode: String, scalePercentage: Float = 1.0f) {
    val buttonWidth = 100
    val buttonHeight = 40
    val spacing = 10
    val y = 645f
    if (mode == "client") {
        p5ctrl.addButton("reconnect")
            .setLabel("Reconnect")
            .setPosition(spacing * scalePercentage, y * scalePercentage)
            .setSize((buttonWidth * scalePercentage).toInt(), (buttonHeight * scalePercentage).toInt())
    } else {
        p5ctrl.addButton("startMusic")
            .setLabel("Start / Stop")
            .setPosition(spacing * scalePercentage, y * scalePercentage)
            .setSize((buttonWidth * scalePercentage).toInt(), (buttonHeight * scalePercentage).toInt())
//            .onClick {
//                (it.controller as Button).mouseReleased()
//            }
        p5ctrl.addButton("loadCurve")
            .setLabel("Load")
            .setPosition((spacing*3 + buttonWidth*2) * scalePercentage, y * scalePercentage)
            .setSize((buttonWidth * scalePercentage).toInt(), (buttonHeight * scalePercentage).toInt())
        p5ctrl.addButton("panic")
            .setColorBackground(Color.LIGHT_GRAY.rgb)
            .setColorForeground(Color.RED.rgb)
            .setLabel("Panic!")
            .setPosition((spacing*4 + buttonWidth*3) * scalePercentage, y * scalePercentage)
            .setSize((buttonHeight * scalePercentage).toInt(), (buttonHeight * scalePercentage).toInt())
    }
    p5ctrl.addButton("resetMusic")
        .setLabel("Reset")
        .setPosition((spacing*2 + buttonWidth) * scalePercentage, y * scalePercentage)
        .setSize((buttonWidth * scalePercentage).toInt(), (buttonHeight * scalePercentage).toInt())
}

fun addInstrumentSelector(p5ctrl: ControlP5, channels: List<Channel>, color: (v1: Int, v2: Int, v3: Int) -> Int, scalePercentage: Float = 1.0f) {
    val p5Radio = p5ctrl.addRadioButton("setInstrument")
        .setPosition(550f * scalePercentage, 645f * scalePercentage)
        .setSize((20 * scalePercentage).toInt(), (20 * scalePercentage).toInt())
        .setItemsPerRow(6)
        .setSpacingColumn((80 * scalePercentage).toInt())
        .setColorBackground(Color.LIGHT_GRAY.rgb)
        .setColorLabel(0x000000)
        .setNoneSelectedAllowed(false)

    channels.forEach { channel ->
        val item = p5Radio.addItem(channel.program_name, channel.program_number.toFloat()).getItem(channel.program_name)
        with(channel.color) {
            item.setColorActive(color.invoke(r, g, b))
        }
    }
    p5Radio.activate(0)
}
