package madscience.views;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public abstract class CanvasView {
    private double x = -1, y = -1;
    private final int width, height;
    private boolean visible = false;
    private Set<CanvasViewListener> viewListeners = new HashSet<CanvasViewListener>();
    private BufferedImage backgroundImg = null;

    public CanvasView(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
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

    public BufferedImage getBackground() {
        return backgroundImg;
    }

    public void setBackground(BufferedImage backgroundImg) {
        this.backgroundImg = backgroundImg;
    }

    public void draw(Graphics2D g) {
        if (!isVisible()) return;
        if (backgroundImg != null) g.drawImage(backgroundImg, null, 0, 0);
        else g.clearRect(0, 0, getWidth(), getHeight());
    }

    public void update(double sec) {
    }

    public void processKeys(Set<Integer> keys) {
    }

    public void processMouse(int mouseX, int mouseY, Set<Integer> buttons) {
    }

}
