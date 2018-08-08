class JamSketchSlave extends JamSketch implements TargetMover {

  void setup() {
    background(255)
    smooth()
    size(1200, 700)
    showMidiOutChooser()

    motionController = Class.forName(args[0]).newInstance()
    motionController.setTargetMover(this)
    motionController.init()
    motionController.start()

    initData()

    // add WindowListener (windowClosing) which calls exit();
    super.setupExternalMessages()
  }

  void setTargetXY(double x, double y) {
      int measure = getCurrentMeasure()
      double beat = getCurrentBeat()
      if (measure >= 0) {
        double myX = beat2x(measure + 1, beat)
        melodyData.curve1[myX as int] = y
        println("melodyData.curve1[${myX as int}]=${y}")
        fillCurve1(myX as int)
    }
  }

  private void fillCurve1(currentValueIndex) {
    if (currentValueIndex > 0) {
      def pIndex = getPreviousValueIndex(currentValueIndex)
      if (pIndex >= 0 && (currentValueIndex - pIndex) > 1) {
        def diff = (melodyData.curve1[currentValueIndex] - melodyData.curve1[pIndex]) / (currentValueIndex - pIndex)
        for (i in currentValueIndex - 1 .. pIndex + 1) {
          melodyData.curve1[i] = melodyData.curve1[i + 1] - diff
        }
      }
    }
  }

  private int getPreviousValueIndex(currentValueIndex) {
    for (i in currentValueIndex - 1 .. 0) {
      if (melodyData.curve1[i] != null) return i
      if (i == 0) return -1
    }
  }

}