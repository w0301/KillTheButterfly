package madscience.sprites;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public abstract class AbstractSprite implements Cloneable {
    protected final Game game;
    protected final BufferedImage image;
    protected double x = 0, y = 0;

    public AbstractSprite(Game game, BufferedImage image) {
        this.game = game;
        this.image = image;
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
        return image.getWidth();
    }

    public double getHeight() {
        return image.getHeight();
    }

    public boolean containsPoint(double px, double py) {
        return px >= x && px <= x + getWidth() &&
               py >= y && py <= y + getHeight();
    }

    public boolean intersects(AbstractSprite sprite) {
        return containsPoint(sprite.x, sprite.y) ||
               containsPoint(sprite.x + sprite.getWidth(), sprite.y) ||
               containsPoint(sprite.x + sprite.getWidth(), sprite.y + sprite.getHeight()) ||
               containsPoint(sprite.x, sprite.y + sprite.getHeight());
    }

    public abstract void performAdded();
    public abstract void performRemoved();

    public abstract void update(double sec);
    public abstract void performIntersection(AbstractSprite sprite);

    public void draw(Graphics2D g) {
        AffineTransform af = new AffineTransform();
        af.translate(x, y);
        g.drawImage(image, af, null);
    }

}
