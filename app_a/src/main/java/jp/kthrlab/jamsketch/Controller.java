package jp.kthrlab.jamsketch;

import processing.core.PApplet;
import processing.event.TouchEvent;

public abstract class Controller {
    protected PApplet pApplet;
    protected int width;
    protected int height;
    protected float positionX;
    protected float positionY;
    protected  String label;
    protected  String id;

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

    public boolean overRect() {
        for (TouchEvent.Pointer touch : pApplet.touches) {
            if (touch.x >= positionX && touch.x <= positionX + width &&
                    touch.y >= positionY && touch.y <= positionY + height) return true;
        }
        return false;
    }

    public abstract void draw();

}
