import java.io.*;

public interface MotionController {
    void setTargetMover(TargetMover tm);
    void init() throws IOException;
    void start();
}
