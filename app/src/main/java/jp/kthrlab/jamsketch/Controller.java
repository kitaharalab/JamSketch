package jp.kthrlab.jamsketch;

import processing.core.PApplet;
import processing.event.MouseEvent;
import processing.event.TouchEvent;

public abstract class Controller {
    protected PApplet pApplet;
    protected int width;
    protected int height;
    protected float positionX;
    protected float positionY;
    protected  String label;
    protected  String id;
    protected float labelWidth = 0;
    protected float labelHeight = 0;

    public Controller (String id, PApplet pApplet) {
        this.pApplet = pApplet;
        this.id = id;
    }

    public Controller setPosition(float positionX, float positionY){
        this.positionX = positionX;
        this.positionY = positionY;
        return this;
    };

    public Controller setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Controller setLabel(String label) {
        this.label = label;
        return this;
    }

    public Controller setLabelSize(float labelWidth, float labelHeight) {
        this.labelWidth = labelWidth;
        this.labelHeight = labelHeight;
        return this;
    }

    public boolean overRect() {
//        if(pApplet.mouseX >= positionX && pApplet.mouseX <= positionX + width &&
//        pApplet.mouseY >= positionY && pApplet.mouseY <= positionY  + height) return true;
//
//        for (TouchEvent.Pointer touch : pApplet.touches) {
//            System.out.println(touch.x + ", " + touch.y);
//            if (touch.x >= positionX && touch.x <= positionX + width &&
//                    touch.y >= positionY && touch.y <= positionY + height) return true;
//        }
//        return false;
        return overRect(positionX, positionY, width, height);
    }

    public  boolean overRect(int margin) {
        return overRect(positionX - margin, positionY + margin, width + (margin * 2), height + (margin * 2));
    }

    public boolean overRect(float positionX, float positionY, int width, int height) {
        if(pApplet.mouseX >= positionX && pApplet.mouseX <= positionX + width &&
                pApplet.mouseY >= positionY && pApplet.mouseY <= positionY  + height) return true;

        for (TouchEvent.Pointer touch : pApplet.touches) {
            System.out.println(touch.x + ", " + touch.y);
            if (touch.x >= positionX && touch.x <= positionX + width &&
                    touch.y >= positionY && touch.y <= positionY + height) return true;
        }
        return false;
    }


    public boolean pOverRect() {
        for (TouchEvent.Pointer touch : pApplet.touches) {
            if (touch.x >= positionX && touch.x <= positionX + width &&
                    touch.y >= positionY && touch.y <= positionY + height) return true;
        }
        return false;
    }

    public abstract void draw();
//    public abstract void touchEvent(TouchEvent event);
    public abstract void mouseEvent(MouseEvent event);
//    public abstract void touchStarted(TouchEvent event);
//    public abstract void touchEnded(TouchEvent event);

}
