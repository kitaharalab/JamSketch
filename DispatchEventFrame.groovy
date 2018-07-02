import javax.swing.*
import java.awt.*
import java.awt.event.*

public class DispatchEventFrame extends JFrame implements MouseListener, MouseWheelListener, MouseMotionListener, KeyListener, FocusListener {

    def target

	public DispatchEventFrame(title) {
		super(title)
	}

    void init() {
        setUndecorated(true)
        setOpacity(0.4)

        addMouseListener(this)
        addMouseWheelListener(this)
        addMouseMotionListener(this)
        addKeyListener(this)
        addFocusListener(this)
    }

    public void setTarget(target) {
        this.target = target
    }
 	
    // MouseListener
    void mouseClicked(MouseEvent e) {
        target.dispatchEvent(e)
    }
    void mouseEntered(MouseEvent e) {
        target.dispatchEvent(e)
    }
    void mouseExited(MouseEvent e) {
        target.dispatchEvent(e)
    }
    void mousePressed(MouseEvent e) {
        target.dispatchEvent(e)
    }
    void mouseReleased(MouseEvent e) {
        target.dispatchEvent(e)
    }

    // MouseWheelListener
	void mouseWheelMoved(MouseWheelEvent e) {
        target.dispatchEvent(e)
    }

    // MouseMotionListener
    void mouseDragged(MouseEvent e) {
        target.dispatchEvent(e)
    }
    void mouseMoved(MouseEvent e) {
        target.dispatchEvent(e)
    }

    // KeyListener
    void keyTyped(KeyEvent e) {
        target.dispatchEvent(e)
    }

    void keyPressed(KeyEvent e) {
        target.dispatchEvent(e)
    }

    void keyReleased(KeyEvent e) {
        target.dispatchEvent(e)
    }

    // FocusListener
    void focusGained(FocusEvent e) {
        target.dispatchEvent(e)
    }

    void focusLost(FocusEvent e) {
        target.dispatchEvent(e)
    }
}
