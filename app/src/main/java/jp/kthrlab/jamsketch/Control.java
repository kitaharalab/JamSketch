package jp.kthrlab.jamsketch;

import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class Control {
    private PApplet pApplet;
    private List<Controller> controllers = new ArrayList<Controller>();

    public Control(PApplet pApplet) {
        this.pApplet = pApplet;
        pApplet.registerMethod("draw", this);
    }

    public Button addButton(String id) {
//        pApplet.registerMethod(id, pApplet);
        Button button = new Button(id, pApplet);
        addController(button);
        return button;
    }

    private void addController(Controller controller) {
        controllers.add(controller);
        pApplet.registerMethod("mouseEvent", controller);
//        pApplet.registerMethod("touchEvent", controller);
    }

    public void draw() {
        controllers.forEach(controller -> controller.draw());
    }
}
