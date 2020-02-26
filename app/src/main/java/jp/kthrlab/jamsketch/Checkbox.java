package jp.kthrlab.jamsketch;

import processing.core.PApplet;
import processing.event.MouseEvent;

public class Checkbox extends Button {

    private boolean checked = false;
    private float textSize  =20;


    public Checkbox(String id, PApplet pApplet) {
        super(id, pApplet);
    }

    public boolean checked() {
        return checked;
    }

    @Override
    public void draw() {

        // draw checkbox
        pApplet.textSize(textSize);
        if (checked) {
            pApplet.noStroke();
            pApplet.fill(0, 0, 255);
        } else {
            pApplet.strokeWeight(3);
            pApplet.stroke(0, 0, 0);
            pApplet.fill(255, 255, 255);
        }
        pApplet.rect(positionX, positionY, width, height);

        // draw label
        pApplet.fill(0, 0, 0);
        pApplet.textAlign(PApplet.LEFT, PApplet.CENTER);
        pApplet.text(label,
                positionX + width + 7,
                (labelHeight > 0) ?  positionY + height/2 - labelHeight/2 : positionY,
                (labelWidth > 0) ? labelWidth : pApplet.textWidth(label),
                (labelHeight > 0) ? labelHeight : textSize);

    }

    @Override
    public void mouseEvent(MouseEvent event) {
        switch(event.getAction()) {
            case 1:
//                this.touchStarted(event);
                if (overRect(10)) {
                    startedInside = true;
                }
                break;
            case 2:
//                this.touchEnded(event);
                if (startedInside && overRect(10)) {
                    checked = !checked;
                }
                startedInside = false;
                break;
            case 3:
//                this.touchCancelled(event);
                break;
            case 4:
//                this.touchMoved(event);
        }

    }


}
