import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.filewrappers.*
import static jp.crestmuse.cmx.misc.ChordSymbol2.*
				     //import static Config.*
import static JamSketch.CFG

class MelodyData {
  def curve1 // y coord per pixel
  def curve2 // note number per beat
  def curve4 // 平滑化後
  def RHYTHMS = [[1, 0, 0, 0, 0, 0], [1, 0, 0, 1, 0, 0],
		 [1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 1],
		 [1, 0, 1, 1, 1, 1], [1, 1, 1, 1, 1, 1]];
  // The size of each list has to be DIVISION
  def scc
  def target_part
  def model
  def rmodel
  def expgen
  def width
  def pianoroll
  def dis
  def result
  def piano = 100
  def x_s = piano
  def x_f = 192
  def keep_sec_s //保持
  def keep_sec_f 
  def keep_result
  def keep_r
  int times

  def vel1 
  def vel2 

  def p
  


  MelodyData(filename, width, cmxcontrol, pianoroll) {
    this.width = width
    this.pianoroll = pianoroll
    curve1 = [null] * width
	curve4 = [0] * width
	dis = [null] * width
	p = [null] * width
    scc = cmxcontrol.readSMFAsSCC(filename)
    scc.repeat(CFG.INITIAL_BLANK_MEASURES * CFG.BEATS_PER_MEASURE *
	       scc.division,
	       (CFG.INITIAL_BLANK_MEASURES + CFG.NUM_OF_MEASURES) *
	       CFG.BEATS_PER_MEASURE * scc.division, CFG.REPEAT_TIMES - 1)
    target_part = scc.getFirstPartWithChannel(1)
    model = new MelodyModel(cmxcontrol, new SCCGenerator())
	rmodel = new RhythmModel()
    if (CFG.EXPRESSION) {
      expgen = new ExpressionGenerator()
      expgen.start(scc.getFirstPartWithChannel(1),
     		 getFullChordProgression(), CFG.BEATS_PER_MEASURE)
    }
  }

  
  def smoothing() {
	System.out.println("こっから")

	def a = 50; //何回ずらすか
	def x = a + 1;
	def y = curve1.size;
	def i,j,k,l;
	def bre
	def sec_s = 0
    def sec_f = 0
	//def finish = 0
	def disb = 0
	def disa = 0

	double[][] coord = new double[x][y]

	bre = [0] * width

	for (i = 0;i < y;i++){
	if(curve1[i] != null){
		coord[0][i] = curve1[i]
		//finish = i
	}else{
		coord[0][i] = 0
	}
	}

	for (i = 1;i < x;i++){
		for(j=0;j < y;j++){
		if(j-1>0){
		coord[i][j] = coord[i-1][j-1];
		}else{
			coord[i][j] = 0
		}
		}
	}

	//平滑化後
	/*
	for (i = 0; i < x ; i++){
		for( j = 0; j < curve4.size ; j++){
		if(coord[i][j] != 0){
		curve4[j] += coord[i][j];
		bre[j]++
		}
		}
    }
	*/
	for (j = 0; j < curve4.size ; j++){
		for( i = 0; i < x ; i++){
		if(coord[i][j] != 0){
		curve4[j] += coord[i][j];
		bre[j]++
		}
		}
    }
	


	for (i = 0;i < curve4.size; i++){
	if(bre[i] != 0)
	curve4[i] = curve4[i] / bre[i]
    }

	
	for (i = a;i < curve4.size; i++){    //平滑化するのにずらした分戻した
	curve4[i-a] = curve4[i]
    }
	
	//平滑化後-前

	for (i = 0;i < curve4.size-1;i++){
		if(curve4[i] != 0 && coord[0][i] != 0){
		disb = coord[0][i] - coord[0][i+1]
		disa = curve4[i] - curve4[i+1]
		dis[i] = disa - disb
		}
	}
	
	//ギザギザ度
	def deg
	def z = 92 //1小節分の座標の数
	def cou
	def stan1 = 6.5
	def stan2 = 30 //基準値
	def start
	def s = 0
	def f = z
	def keep = 0

	times = width/z 
	//times = (finish-100)/z + 1
	//System.out.println("times"+times)

	deg = [0] * times
	keep_sec_s = [null] * times
	keep_sec_f = [null] * times
	keep_result = [null] * times
	keep_r = [0] * times
	k = 0
	l = 0
	for(i=0 ; i<times ;i++){
		deg[i] = 0
		for(j=s;j< f ;j++){
		if(dis[j] != null){
			deg[i] += dis[j] * dis[j]
			}
		}

		if(deg[i] != 0)
		deg[i] = deg[i] / a
		//System.out.println("値>>"+i+";"+deg[i])
		s += z
		f += z

		x_f += z
			if(deg[i] == 0){
			keep_r[i] = 0
			result = 0
			//sec_s++
			}else if(deg[i] <= stan1){
			keep_r[i] = 1
				result = 1                 //nomal
			}else if(stan2 <= deg[i]){
				keep_r[i] = 3
				result = 3                 //slow
			}else{
			keep_r[i] = 2
				result = 2                 //speed
				}

		/*
		//System.out.println("result>>"+result)
		if(result != keep){
			x_s += z * (sec_s -1)
			x_f = x_s + z
			if(keep != 0){
			//System.out.println("finishsecton>>" + sec_f )
			keep_sec_f[l] = sec_f 
			System.out.println(">>finishsecton>>" +  keep_sec_f[l])
			l++
			}
			if(result == 1){
				System.out.println("startsection>>" + sec_s )
				//System.out.println("start>>" + x_s )
				//System.out.println("finish>>" + x_f )
				System.out.println("speed")
				keep_sec_s[k] = sec_s
				keep_result[k] = result
				System.out.println(">>startsecton>>" +  keep_sec_s[k])
				k++
			}else if(result == 2){
				System.out.println("startsection>>" + sec_s )
				//System.out.println("start>>" + x_s )
				//System.out.println("finish>>" + x_f )
				System.out.println("slow")
				keep_sec_s[k] = sec_s
				keep_result[k] = result
				System.out.println(">>startsecton>>" +  keep_sec_s[k])
				k++
			}
			sec_s = sec_f + 1
		}
		sec_s++
		sec_f++
		keep = result
		*/
		
	
	} 
	//System.out.println("finishsecton>>" + sec_f )
	/*
	keep_sec_f[l] = sec_f
	System.out.println(">>finishsecton>>" +  keep_sec_f[l])
	*/

 }
 

  def resetCurve() {
    curve1 = [null] * width
	curve4 = [0] * width
	dis = [null] * width
	p = [null] * width
    updateCurve('all')
  }
  def updateCurve(measure) {
    def curve3 = curve1.subList(100, curve4.size()).collect {
      it == null ? null : pianoroll.y2notenum(it)
    }
    curve2 = shortenCurve(curve3)
    if (measure == 'all') {
      (0..<CFG.NUM_OF_MEASURES).each {
	setDataToMusicRepresentation(curve2, model.mr, it)
	//	model.updateMusicRepresentation(it)
      }
    } else {
      setDataToMusicRepresentation(curve2, model.mr, measure as int)
      //      model.updateMusicRepresentation(measure as int)
    }
  }

  def setDataToMusicRepresentation(curve, mr, measure) {
    println("++++++++++++++++++++++++++++++++++++" + measure + "++++")
    //    def rhythm = decideRhythm(curve2, 0.25)
    def curve1 = curve[(CFG.DIVISION*measure)..<(CFG.DIVISION*(measure+1))]
    //    def similar_curve =  searchSimilarCurve(curve1)
    //    if (similar_curve != null) {
    //      copy_melody(similar_curve, measure, mr)
    //    } else {
    //curve_pool[measure] = curve1
    def rhythm1
	double rhythmDensity
	def k

    if (CFG.RHYTHM_ENGINE == "v1") {
      rhythm1 = decideRhythm(curve1, 0.25)
    } else {
	
//	  for(k = 0;k < times;k++){
	    //println("k=" + k + " KEEP_R=" + keep_r)
	  	  if(keep_r[measure] == 1){
	  rhythmDensity = 4
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }else if(keep_r[measure] == 2){
	  rhythmDensity = 12
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }else{
	  rhythmDensity = 2
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }
	  //println("RHYTHM_DENSITY: " + rhythmDensity)
	  //println("rhythm1: " + rhythm1)
	  }
	/*
	  if(result == 1){
	  rhythmDensity = 12
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }else if(result == 2){
	  rhythmDensity = 4
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }else{
	  rhythmDensity = 6
      rhythm1 = rmodel.decideRhythm(curve1, rhythmDensity)
	  }
	  */
	  
 //   }
    //    println("RHYTHM" + rhythm1)
    (0..<CFG.DIVISION).each { i ->
      def e = mr.getMusicElement("curve", measure, i)
      e.suspendUpdate()
      if (curve1[i] == null) {
	e.setRest(true)
      } else if (rhythm1[i] == 0) {
	e.setRest(false)
	e.setTiedFromPrevious(true)
	println("Tied")
      } else {
	e.setRest(false)
	e.setTiedFromPrevious(false)
	e.setEvidence(curve1[i])
      }
    }
    model.updateMusicRepresentation(measure)
    //    }
  }

   /* 
  def updateCurve(measure) {
    def curve3 = curve4.subList(100, curve4.size()).collect {
      it == null ? null : pianoroll.y2notenum(it)
    }
    curve2 = shortenCurve(curve3)
    setDataToMusicRepresentation(curve2, model.mr)
    if (measure == 'all') {
      (0..<CFG.NUM_OF_MEASURES).each {
	model.updateMusicRepresentation(it)
      }
    } else {
      model.updateMusicRepresentation(measure as int)
    }
  }

  def setDataToMusicRepresentation(curve, mr) {
  double rhythmDensity = 4
      def rhythm = rmodel.decideRhythm(curve1, rhythmDensity)
    //def rhythm = decideRhythm(curve2, 0.25)
    (0..<(CFG.NUM_OF_MEASURES * CFG.DIVISION)).each { i ->
      def e = mr.getMusicElement("curve", i / CFG.DIVISION as int,
				 i % CFG.DIVISION as int)
      e.suspendUpdate()
      if (curve[i] == null) {
	e.setRest(true)
      } else if (rhythm[i] == 0) {
	e.setRest(false)
	e.setTiedFromPrevious(true)
      } else {
	e.setRest(false)
	e.setTiedFromPrevious(false)
	e.setEvidence(curve[i])
      }
    }
  }
  */

  def shortenCurve(curve) {
    def newcurve = []
    (0..<(CFG.NUM_OF_MEASURES * CFG.DIVISION)).each { i ->
      int from = i / (CFG.NUM_OF_MEASURES * CFG.DIVISION) * curve.size()
      int thru = (i+1) / (CFG.NUM_OF_MEASURES * CFG.DIVISION) * curve.size()
      def curve4 = curve[from..<thru].findAll{it != null}
      if (curve4.size() > 0)
	newcurve.add(curve4.sum() / curve4.size())
        else
	newcurve.add(null)
    }
    newcurve
  }
    
  def decideRhythm(curve, th) {
    def r1 = [1]
	(0..<(curve.size()-1)).each { i ->
      if (curve[i+1] == null) {
	r1.add(0)
      } else if (curve[i] == null && curve[i+1] != null) {
	r1.add(1)
      } else if (Math.abs(curve[i+1] - curve[i]) >= th) {
	r1.add(1)
      } else {
	r1.add(0)
      }
    }
    def r2 = []
    (0..<(CFG.NUM_OF_MEASURES*2)).each { i ->
      def r1sub = r1[(CFG.DIVISION/2*i as int)..<(CFG.DIVISION/2*(i+1) as int)]
      def diffs = []
      diffs = RHYTHMS.collect { r ->
	abs(sub(r, r1sub)).sum()
      }
      def index = argmin(diffs)
      r2.add(RHYTHMS[index])
    }
    r2.sum()
  }

  def sub(x, y) {
    def z = []
    x.indices.each{ i ->
      z.add(x[i] - y[i])
    }
    z
  }
    
  def abs(x) {
    x.collect{ Math.abs(it) }
  }
    
  def argmin(x) {
    def minvalue = x[0]
    def argmin = 0
    x.indices.each { i ->
      if (x[i] < minvalue) {
	minvalue = x[i]
	argmin = i
      }
    }
    argmin
  }

  def getFullChordProgression() {
    [NON_CHORD] * CFG.INITIAL_BLANK_MEASURES + CFG.chordprog * CFG.REPEAT_TIMES
  }   

  class SCCGenerator implements MusicCalculator {
    void updated(int measure, int tick, String layer,
		 MusicRepresentation mr) {
      def sccdiv = scc.getDivision()
      def firstMeasure = pianoroll.getDataModel().getFirstMeasure()
      def e = mr.getMusicElement(layer, measure, tick)
      if (!e.rest() && !e.tiedFromPrevious()) {
	int notenum = getNoteNum(e.getMostLikely(),
				 curve2[measure * CFG.DIVISION + tick])
	int duration = e.duration() * sccdiv /
	(CFG.DIVISION / CFG.BEATS_PER_MEASURE)
	int onset = ((firstMeasure + measure) * CFG.DIVISION + tick) * sccdiv /
	(CFG.DIVISION / CFG.BEATS_PER_MEASURE)
	if (onset > pianoroll.getTickPosition()) {
	  def oldnotes =
	    SCCUtils.getNotesBetween(target_part, onset,
				     onset+duration, sccdiv, true, true)
	    //data.target_part.getNotesBetween2(onset, onset+duration)
	  target_part.remove(oldnotes)
	  
	  int x = pianoroll.beat2x(measure, tick / CFG.DIVISION) as int

	  def pressdata = p[(x-4)..(x+4)]
	  def sum = 0
	  def count = 0
	  for (i in 0..(pressdata.size()-1)) {
	    if (pressdata[i] != null) {
		  sum += pressdata[i]
		  count++
		}
	   }
	   def pressavg
	   if (count > 0) {
	     pressavg = sum / count  
		} else {
			pressavg = 0
		}
	  //println(x + ":" + pressavg)
	  if(pressavg > 3000){
	  	  vel1 = 127
		  vel2 = 127
	  }else if(pressavg > 2000){
		  vel1 = 80
		  vel2 = 80
	  }else if(pressavg >1000){
		  vel1 = 50
		  vel2 = 50
	  }else if(pressavg == 0){
	      vel1 = 100
		  vel2 = 100
	  }else{
		  	  vel1 = 30
			  vel2 = 30
		  }
	
	  target_part.addNoteElement(onset, onset+duration, notenum,
					  vel1, vel2)
	}
      }

      if (CFG.EXPRESSION) {
	def fromTick = (firstMeasure + measure) * CFG.BEATS_PER_MEASURE *
	  CFG.DIVISION
	def thruTick = fromTick + CFG.BEATS_PER_MEASURE * CFG.DIVISION
	expgen.execute(fromTick, thruTick, CFG.DIVISION)
      }
    }

    def getNoteNum(notename, neighbor) {
      def best = 0
      for (int i in 0..11) {
	def notenum = i * 12 + notename
	if (Math.abs(notenum - neighbor) < Math.abs(best - neighbor))
	  best = notenum
      }
      best
    }
  }
}
