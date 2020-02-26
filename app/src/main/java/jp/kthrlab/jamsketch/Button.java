package jp.kthrlab.jamsketch;

import processing.core.PApplet;
import processing.event.MouseEvent;
import processing.event.TouchEvent;

public class Button extends Controller {

    protected boolean startedInside = false;

    public Button(String id, PApplet pApplet) {
        super(id, pApplet);
    }

    @Override
    public void draw() {
        pApplet.textSize(20);
        pApplet.noStroke();
        if (overRect()) {
            pApplet.fill(0, 0, 255);
        } else {
            pApplet.fill(0, 0, 0);
        }
        pApplet.rect(positionX, positionY, width, height);
        pApplet.fill(255, 255, 255);
        pApplet.textAlign(PApplet.CENTER, PApplet.CENTER);
        pApplet.text(label, positionX, positionY, width, height);

    }

    @Override
    public void mouseEvent(MouseEvent event) {
        switch(event.getAction()) {
            case 1:
//                this.mousePressed(event);
                if (overRect()) {
                    startedInside = true;
                }
                break;
            case 2:
//                this.mouseReleased(event);
                if (startedInside && overRect()) {
                    pApplet.method(id);
                }
                startedInside = false;
                break;
            case 3:
//                this.mouseClicked(event);
                break;
            case 4:
//                this.mouseDragged(event);
                break;
            case 5:
//                this.mouseMoved(event);
                break;
            case 6:
//                this.mouseEntered(event);
                break;
            case 7:
//                this.mouseExited(event);
        }

    }

//    @Override
    public void touchEvent(TouchEvent event) {
        switch(event.getAction()) {
            case 1:
//                this.touchStarted(event);
                if (overRect()) {
                    startedInside = true;
                }
                break;
            case 2:
//                this.touchEnded(event);
                if (startedInside && overRect()) {
                    pApplet.method(id);
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
