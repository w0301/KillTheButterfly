package madscience.views;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public abstract class CanvasView {
    private final int width, height;
    private boolean visible = false;
    private Set<CanvasViewListener> viewListeners = new HashSet<CanvasViewListener>();

    public CanvasView(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            for (CanvasViewListener l : viewListeners) l.canvasViewShown(this);
        }
        else {
            for (CanvasViewListener l : viewListeners) l.canvasViewHidden(this);
        }
    }

    public void addViewListener(CanvasViewListener l) {
        viewListeners.add(l);
    }

    public void removeViewListener(CanvasViewListener l) {
        viewListeners.remove(l);
    }

    public abstract void draw(Graphics2D g);
    public abstract void update(double sec);
    public abstract void processKeys(Set<Integer> keys);

}
