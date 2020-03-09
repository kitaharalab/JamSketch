import controlP5.ControlP5
import groovy.json.JsonOutput
import jp.crestmuse.cmx.processing.gui.SimplePianoRoll

import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class JamSketch extends SimplePianoRoll {

  GuideData guideData
  MelodyData2 melodyData
  boolean nowDrawing = false
  String username = ""

  
  static def CFG

  void setup() {
    super.setup()
    size(1200, 700)
    showMidiOutChooser()
    def p5ctrl = new ControlP5(this)
    p5ctrl.addButton("startMusic").
    setLabel("Start / Stop").setPosition(20, 645).
      setSize(120, 40)
    p5ctrl.addButton("resetMusic").
    setLabel("Reset").setPosition(160, 645).setSize(120, 40)
    p5ctrl.addButton("loadCurve").
    setLabel("Load").setPosition(300, 645).setSize(120, 40)

    if (CFG.MOTION_CONTROLLER != null) {
      CFG.MOTION_CONTROLLER.each { mCtrl ->
        JamSketch.main("JamSketchSlave", [mCtrl] as String[])
      }
    }

    initData()

    // add WindowListener (windowClosing) which calls exit();
  }

  void initData() {
    if (CFG.SHOW_GUIDE) guideData = new GuideData(CFG.MIDFILENAME, width - 100, this)
    melodyData = new MelodyData2(CFG.MIDFILENAME, width, this, this, CFG)
    smfread(melodyData.scc.getMIDISequence())
    def part =
      melodyData.scc.getFirstPartWithChannel(CFG.CHANNEL_ACC)
    setDataModel(
      part.getPianoRollDataModel(
	    CFG.INITIAL_BLANK_MEASURES,
            CFG.INITIAL_BLANK_MEASURES + CFG.NUM_OF_MEASURES
      ))
  }

  void draw() {
    super.draw()    
    if (guideData != null)
      drawGuideCurve()
    drawCurve()
    if (getCurrentMeasure() == CFG.NUM_OF_MEASURES - 1)
      processLastMeasure()
    melodyData.engine.setFirstMeasure(getDataModel().
      getFirstMeasure())
    enhanceCursor()
    drawProgress()
  }

  void drawCurve() {
    strokeWeight(3)
    stroke(0, 0, 255)
    (0..<(melodyData.curve1.size()-1)).each { i ->
      if (melodyData.curve1[i] != null &&
          melodyData.curve1[i+1] != null) {
        line(i, melodyData.curve1[i] as int, i+1,
             melodyData.curve1[i+1] as int)
      }
    }    
  }

  void drawGuideCurve() {
    def xFrom = 100
    strokeWeight(3)
    stroke(224, 224, 224)
    (0..<(guideData.curveGuideView.size()-1)).each { i ->
      if (guideData.curveGuideView[i] != null &&
      guideData.curveGuideView[i+1] != null) {
        line(i+xFrom, guideData.curveGuideView[i] as int,
             i+1+xFrom, guideData.curveGuideView[i+1] as int)
      }
    }

  }

  void storeCursorPosition() {
    if ((!CFG.ON_DRAG_ONLY || nowDrawing) &&
         isInside(mouseX, mouseY)) {
      int m1 = x2measure(mouseX)
      int m0 = x2measure(pmouseX)
      if (0 <= m0) {  
        if (pmouseX < mouseX) {
          (pmouseX..mouseX).each { i ->
            melodyData.curve1[i] = mouseY
          }
	  melodyData.updateCurve(pmouseX, mouseX)
        }
//        if (m1 > m0) {
//          melodyData.updateCurve(m0 % CFG.NUM_OF_MEASURES)
//        }
      }
    }
  }

  void processLastMeasure() {
    makeLog("melody")
    if (CFG.MELODY_RESETING) {
      getDataModel().shiftMeasure(CFG.NUM_OF_MEASURES)
      melodyData.resetCurve()
      if (guideData != null) guideData.shiftCurve()
    }
  }

  void enhanceCursor() {
    if (CFG.CURSOR_ENHANCED) {
      fill(255, 0, 0)
      ellipse(mouseX, mouseY, 10, 10)
    }
  }

  void drawProgress() {
    if (isNowPlaying()) {
      def dataModel = getDataModel()
      int m = getCurrentMeasure() +
              dataModel.getFirstMeasure() -
              CFG.INITIAL_BLANK_MEASURES + 1
      int mtotal = dataModel.getMeasureNum() *
                   CFG.REPEAT_TIMES
      textSize(32)
      fill(0, 0, 0)
      text(m + " / " + mtotal, 460, 675)    
    }
  }
  
  void stop() {
    super.stop()
    //featext.stop()
  }

  void startMusic() {
    if (isNowPlaying()) {
      stopMusic()
      makeLog("stop")
    } else {
      playMusic()
      makeLog("play")
    }
  }

  void resetMusic() {
    initData()
    setTickPosition(0)
    getDataModel().setFirstMeasure(CFG.INITIAL_BLANK_MEASURES)
    makeLog("reset")
  }

  void makeLog(action) {
    def logname = "output_" + (new Date()).toString().replace(" ", "_").replace(":", "-")
    if (action == "melody") {
//      def midname = "${CFG.LOG_DIR}/${logname}_melody.mid"
//      melodyData.scc.toWrapper().toMIDIXML().writefileAsSMF(midname)
//      println("saved as ${midname}")
//      def sccname = "${CFG.LOG_DIR}/${logname}_melody.sccxml"
//      melodyData.scc.toWrapper().writefile(sccname)
//      println("saved as ${sccname}")
      def jsonname = "${CFG.LOG_DIR}/${logname}_curve.json"
      saveStrings(jsonname, [JsonOutput.toJson(melodyData.curve1)] as String[])
      println("saved as ${jsonname}")
      def pngname = "${CFG.LOG_DIR}/${logname}_screenshot.png"
      save(pngname)
      println("saved as ${pngname}")
    } else {
      def txtname = "${CFG.LOG_DIR}/${logname}_${action}.txt"
      saveStrings(txtname, [action] as String[])
      println("saved as ${txtname}")
    }
  }

  void loadCurve() {
    def filter = new FileNameExtensionFilter(".json or .txt", "json", "txt")
    def chooser = new JFileChooser(currentDirectory: new File("."),
				   fileFilter: filter)
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      if (chooser.selectedFile.name.endsWith(".json")) {
      	melodyData.curve1 = json.parseText(chooser.selectedFile.text)
      } else if (chooser.selectedFile.name.endsWith(".txt")) {
        println("Reading ${chooser.selectedFile.absolutePath}")
        def table = loadTable(chooser.selectedFile.absolutePath, "csv")
        melodyData.curve1 = [null] * width
        int n = table.getRowCount()
        int m = melodyData.curve1.size() - 100
        for (int i in 100..<(melodyData.curve1.size()-1)) {
          int from = (i-100) * n / m
          int thru = ((i+1)-100) * n / m - 1
          melodyData.curve1[i] =
            (from..thru).collect{notenum2y(table.getFloat(it, 0))}.sum() /
            (from..thru).size()
        }
      }
    } else {
      println("File is not supported")
      return
    }
    melodyData.updateCurve('all')
  }

  void mousePressed() {
    nowDrawing = true
  }
  
  void mouseReleased() {
    nowDrawing = false
    if (isInside(mouseX, mouseY)) {
      println(x2measure(mouseX))
      println(CFG.NUM_OF_MEASURES)
      if (!melodyData.engine.automaticUpdate()) {
        melodyData.engine.outlineUpdated(
	   x2measure(mouseX) % CFG.NUM_OF_MEASURES,
           CFG.DIVISION - 1)
      }
    }
  }

  void mouseDragged() {
    storeCursorPosition()
  }

  void keyReleased() {
    if (key == ' ') {
      if (isNowPlaying()) {
      	stopMusic()
      } else {
        setTickPosition(0)
        getDataModel().setFirstMeasure(CFG.INITIAL_BLANK_MEASURES)
        playMusic()
      }
    } else if (key == 'b') {
      setNoteVisible(!isNoteVisible());
      println("Visible=${isVisible()}")
//    } else if (key == 'u') {
//      melodyData.updateCurve('all')
    }
  }

  public void exit() {
    println("exit() called.")
    super.exit()
    if (CFG.MOTION_CONTROLLER.any{mCtrl == "RfcommServer"}) RfcommServer.close()
  }

}
JamSketch.CFG = evaluate(new File("./config.txt"))
JamSketch.start("JamSketch")
// JamSketch.main("JamSketch", ["--external"] as String[])
  
