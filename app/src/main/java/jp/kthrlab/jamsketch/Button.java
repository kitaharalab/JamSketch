package jp.kthrlab.jamsketch;

import processing.core.PApplet;

public class Button extends Controller {

    public Button(String id, PApplet pApplet) {
        super(id, pApplet);
    }

    @Override
    public void draw() {
        if (overRect()) {
            pApplet.fill(255, 0, 0);
        } else {
            pApplet.fill(0, 0, 0);
        }
        pApplet.rect(positionX, positionY, width, height);
        pApplet.fill(255, 255, 255);
        pApplet.text(label.toUpperCase(), positionX, positionY, width, height);

    }
}
