package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public abstract class AbstractSprite implements Cloneable {

    public static class SpriteView {
        public final BufferedImage image;
        public final Rectangle2D[] rects;

        public SpriteView(BufferedImage image, Rectangle2D[] rects) {
            this.image = image;
            if (rects == null) {
                this.rects = new Rectangle[] {new Rectangle(0, 0, image.getWidth(), image.getHeight())};
            }
            else this.rects = rects;
        }

        public SpriteView(BufferedImage image) {
            this(image, null);
        }

        public int getWidth() {
            return image.getWidth();
        }

        public int getHeight() {
            return image.getHeight();
        }
    }

    protected final GameView game;
    protected double x = 0, y = 0;

    protected SpriteView currView;

    protected SpriteView defaultView;

    protected List<SpriteView> animViews = null;
    protected int nextAnimationFrame = 0;
    protected double animationInterval = -1;
    protected double tillAnimationFlip = -1;

    public AbstractSprite(GameView game, SpriteView view) {
        this.game = game;
        this.defaultView = this.currView = view;
    }

    public AbstractSprite(GameView game, BufferedImage image) {
        this(game, new SpriteView(image, null));
    }

    public void setDefaultView(SpriteView view) {
        defaultView = view;
    }

    public void addAnimationView(SpriteView view) {
        if (animViews == null) animViews = new ArrayList<SpriteView>();
        animViews.add(view);
    }

    public boolean isAnimationRunning() {
        return animationInterval != -1;
    }

    public void runAnimation(double interval) {
        if (animViews != null && !animViews.isEmpty()) {
            nextAnimationFrame = 0;
            animationInterval = tillAnimationFlip = interval;
        }
    }

    public void stopAnimation() {
        animationInterval = -1;
        currView = defaultView;
    }

    public void clearAnimation() {
        stopAnimation();
        if (animViews != null) animViews.clear();
    }

    @Override
    public AbstractSprite clone() {
        Object ret = null;
        try {
            ret = super.clone();
        }
        catch(CloneNotSupportedException e) { }
        return (AbstractSprite) ret;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getWidth() {
        return currView.getWidth();
    }

    public double getHeight() {
        return currView.getHeight();
    }

    public boolean intersects(AbstractSprite sprite) {
        for (int i = 0; i < currView.rects.length; i++) {
            for (int j = 0; j < sprite.currView.rects.length; j++) {
                Rectangle2D rect1 = currView.rects[i];
                Rectangle2D rect2 = sprite.currView.rects[j];
                rect1 = new Rectangle2D.Double(x + rect1.getX(), y + rect1.getY(),
                                               rect1.getWidth(), rect1.getHeight());
                rect2 = new Rectangle2D.Double(sprite.x + rect2.getX(), sprite.y + rect2.getY(),
                                               rect2.getWidth(), rect2.getHeight());
                if (rect1.intersects(rect2)) return true;
            }
        }
        return false;
    }

    public void update(double sec) {
        if (isAnimationRunning()) {
            tillAnimationFlip -= sec * 1000;
            if (tillAnimationFlip <= 0) {
                currView = animViews.get(nextAnimationFrame++);
                if (nextAnimationFrame >= animViews.size()) nextAnimationFrame = 0;
                tillAnimationFlip = animationInterval;
            }
        }
    }

    public abstract void performIntersection(AbstractSprite sprite);
    public abstract void performAdded();
    public abstract void performRemoved();

    public void draw(Graphics2D g) {
        AffineTransform af = new AffineTransform();
        af.translate(x, y);
        g.drawImage(currView.image, af, null);
    }

}
