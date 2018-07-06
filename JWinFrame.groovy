import javax.swing.*
import java.awt.*
import java.awt.event.*
import jwinpointer.JWinPointerReader
import jwinpointer.JWinPointerReader.PointerEventListener

public class JWinFrame extends DispatchEventFrame implements PointerEventListener {

    def targetListener
    private static final int EVENT_TYPE_DRAG = 1
	private static final int EVENT_TYPE_HOVER = 2
	private static final int EVENT_TYPE_DOWN = 3
	private static final int EVENT_TYPE_UP = 4
	private static final int EVENT_TYPE_BUTTON_DOWN = 5
	private static final int EVENT_TYPE_BUTTON_UP = 6
	private static final int EVENT_TYPE_IN_RANGE = 7
	private static final int EVENT_TYPE_OUT_OF_RANGE = 8


	public JWinFrame(title) {
		super(title)
	}

    public void init() {
        super.init()

        Rectangle targetRect = target.getBounds()
        Rectangle targetFrameRect = target.frame.getBounds()
        Insets targetFrameInsets = target.frame.getInsets()
        int frameWidth = targetRect.width
        int frameHeight = targetRect.height
        int frameX = targetFrameRect.x + targetFrameInsets.left
        int frameY = targetFrameRect.y + targetFrameInsets.top

        setSize(frameWidth, frameHeight)
        setBounds(frameX, frameY, frameWidth, frameHeight)
        setVisible(true)

        def jwin = new JWinPointerReader(this.getTitle())
        println(this.getTitle())
        jwin.addPointerEventListener(this)

    }

    public void setTargetPointerEventListener(targetListener) {
        this.targetListener = targetListener
    }
 	
    // PointerEventListener
    public void pointerXYEvent(int deviceType, int pointerID, int eventType, boolean inverted, int x, int y, int pressure) {
    
        println("pointerXYEvent x=${x}, y=${y}, pressure=${pressure} eventType=${eventType} inverted=${inverted}")

        switch(eventType) {
        case EVENT_TYPE_DRAG:
            break

        case EVENT_TYPE_HOVER:
            break

        case EVENT_TYPE_DOWN:
            break

        case EVENT_TYPE_UP:
            break

        case EVENT_TYPE_BUTTON_DOWN:
            break

        case EVENT_TYPE_BUTTON_UP:
            break

        case EVENT_TYPE_IN_RANGE:
            break

        case EVENT_TYPE_OUT_OF_RANGE:
            break

        default:
            break
        }

//        targetListener.pointerXYEvent(deviceType, pointerID, eventType, inverted, x, y, pressure)

    }

    public void pointerButtonEvent(int deviceType, int pointerID, int eventType, boolean inverted, int buttonIndex) {
        println("pointerButtonEvent")
//        targetListener.pointerButtonEvent(deviceType, pointerID, eventType, inverted, buttonIndex)
    }

    public void pointerEvent(int deviceType, int pointerID, int eventType, boolean inverted) {
        println("pointerEvent")
//        targetListener.pointerEvent(deviceType, pointerID, eventType, inverted)
    }

}
