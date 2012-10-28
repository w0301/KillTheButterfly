package madscience.sprites;

import java.awt.Graphics2D;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public abstract class AbstractSprite {
    protected Game game;
    protected double x, y;


    public AbstractSprite(Game game, double x, double y) {
        this.game = game;
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public abstract double getWidth();
    public abstract double getHeight();

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

    public abstract void update(double sec);
    public abstract void draw(Graphics2D g);

}
