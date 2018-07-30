class JamSketchSlave extends JamSketch implements TargetMover {

  void setup() {
    background(255)
    smooth()
    size(1200, 700)
    showMidiOutChooser()

    ctrl = Class.forName(CFG.MOTION_CONTROLLER).newInstance()
    ctrl.setTargetMover(this)
    ctrl.init()
    ctrl.start()

    initData()
  }

  void draw() {
    super.draw()

    int m1 = x2measure(mouseX)
    int m0 = x2measure(pmouseX)
    if (0 <= m0) {
      if (pmouseX < mouseX) {
        (pmouseX..mouseX).each { i ->
          melodyData.curve1[i] = mouseY
        }
      }
    }
  }

}