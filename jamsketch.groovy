import groovy.transform.*
import jp.crestmuse.cmx.filewrappers.*
import jp.crestmuse.cmx.processing.*
import jp.crestmuse.cmx.processing.gui.*
import groovy.json.*
import jp.crestmuse.cmx.misc.*
import groovy.json.*
import controlP5.*
import javax.swing.*
import javax.swing.filechooser.*

class JamSketch extends SimplePianoRoll implements TargetMover {

  MelodyData melodyData
  boolean nowDrawing = false
  String username = ""
  static def CFG
  def motionController

  void setup() {
    super.setup()
    size(1200, 700)
    showMidiOutChooser()
    def p5ctrl = new ControlP5(this)
    p5ctrl.addButton("startMusic").
    setLabel("Start / Stop").setPosition(20, 645).setSize(120, 40)
    p5ctrl.addButton("resetMusic").
    setLabel("Reset").setPosition(160, 645).setSize(120, 40)
    p5ctrl.addButton("loadCurve").
    setLabel("Load").setPosition(300, 645).setSize(120, 40)

    if (CFG.MOTION_CONTROLLER != null) {
      CFG.MOTION_CONTROLLER.each { mCtrl ->
        if (mCtrl == "RfcommServer") {
            JamSketch.main("JamSketchSlave", [mCtrl] as String[])
        } else {
          motionController = Class.forName(mCtrl).newInstance()
          motionController.setTargetMover(this)
          motionController.init()
          motionController.start()
        }
      }
    }

    initData()
  }

  void initData() {
    melodyData = new MelodyData(CFG.MIDFILENAME, width, this, this)
    println(melodyData.getFullChordProgression())
    smfread(melodyData.scc.getMIDISequence())
    def part = melodyData.scc.getFirstPartWithChannel(1)
    setDataModel(
      part.getPianoRollDataModel(
	    CFG.INITIAL_BLANK_MEASURES, CFG.INITIAL_BLANK_MEASURES + CFG.NUM_OF_MEASURES
      ))
  }

  void draw() {
    super.draw()    
    strokeWeight(3)
    stroke(0, 0, 255)
    drawCurve()

    if (getCurrentMeasure() == CFG.NUM_OF_MEASURES - 1) {
      makeLog("melody")
      if (CFG.MELODY_RESETING) {
        getDataModel().shiftMeasure(CFG.NUM_OF_MEASURES)
        melodyData.resetCurve()
      }
    }

    if (!(motionController in RfcommServer)) {
      if ((!CFG.ON_DRAG_ONLY || nowDrawing) && isInside(mouseX, mouseY)) {
        int m1 = x2measure(mouseX)
        int m0 = x2measure(pmouseX)
        if (0 <= m0) {  
          if (pmouseX < mouseX) {
            (pmouseX..mouseX).each { i ->
              melodyData.curve1[i] = mouseY
            }
          }
          if (m1 > m0) {
            melodyData.updateCurve(m0 % CFG.NUM_OF_MEASURES)
          }
        }
      }
    }

    if (isNowPlaying()) {
      def dataModel = getDataModel()
      int m = getCurrentMeasure() + dataModel.getFirstMeasure() -
              CFG.INITIAL_BLANK_MEASURES + 1
      int mtotal = dataModel.getMeasureNum() * CFG.REPEAT_TIMES
      textSize(32)
      fill(0, 0, 0)
      text(m + " / " + mtotal, 460, 675)
    }

    if (CFG.FORCED_PROGRESS) {
      mouseX = beat2x(getCurrentMeasure()+1, getCurrentBeat());
    }

    if (CFG.CURSOR_ENHANCED) {
      fill(255, 0, 0)
      ellipse(mouseX, mouseY, 10, 10)
    }
  }

  void drawCurve() {
    (0..<(melodyData.curve1.size()-1)).each { i ->
      //println("${melodyData.curve1[i]}, ${melodyData.curve1[i+1]}")
      if (melodyData.curve1[i] != null && melodyData.curve1[i+1] != null) {
        line(i, melodyData.curve1[i] as int, i+1, melodyData.curve1[i+1] as int)
      }
    }    
  }
  
  void stop() {
    super.stop()
    featext.stop()
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
      def midname = "${CFG.LOG_DIR}/${logname}_melody.mid"
      melodyData.scc.toWrapper().toMIDIXML().writefileAsSMF(midname)
      println("saved as ${midname}")
      def sccname = "${CFG.LOG_DIR}/${logname}_melody.sccxml"
      melodyData.scc.toWrapper().writefile(sccname)
      println("saved as ${sccname}")
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
      melodyData.updateCurve(x2measure(mouseX) % CFG.NUM_OF_MEASURES)
    }
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
    } else if (key == 'u') {
      melodyData.updateCurve('all')
    }
  }

  int height() {
    height
  }

  int width() {
    width
  }

  void sendEvent(int event) {
    if (event == TargetMover.ONSET) {
       mousePressed()
    } else if (event == TargetMover.OFFSET) {
       mouseReleased()
    }
  }

  void setTargetXY(double x, double y) {
    println("(${x}, ${y})")
    mouseX = x
    mouseY = y
  }

}
JamSketch.CFG = evaluate(new File("./config.txt"))
JamSketch.start("JamSketch")

  
