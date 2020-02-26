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
        addController(button, "mouseEvent");
        return button;
    }

    public Checkbox addCheckbox(String id) {
        Checkbox checkbox = new Checkbox(id, pApplet);
        addController(checkbox, "mouseEvent");
        return  checkbox;
    }

    private void addController(Controller controller, String methodName) {
        controllers.add(controller);
        pApplet.registerMethod(methodName, controller);
    }

    public void draw() {
        controllers.forEach(controller -> controller.draw());
    }
}
