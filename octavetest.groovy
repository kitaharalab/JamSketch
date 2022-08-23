import dk.ange.octave.*
import dk.ange.octave.io.*
import dk.ange.octave.type.*


OctaveEngineFactory oef = new OctaveEngineFactory();
oef.setOctaveProgram(new File("C:\\Octave\\Octave-4.4.1\\bin\\octave.bat"))
//oef.setOctaveProgram(new File("'C:\\Program Files\\GNU Octave\\Octave-6.2.0\\octave.vbs'")
println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
OctaveDouble od = oe.get(OctaveDouble.class, "dOut");
println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDx")
double jd = od.get(1,1);
println(jd)