package madscience.sprites;

import java.awt.image.BufferedImage;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public abstract class MovableSprite extends AbstractSprite {
    protected double speedX = 0, speedY = 0;

    public MovableSprite(Game game, BufferedImage image) {
        super(game, image);
    }

    public double getSpeedX() {
        return speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedXY(double speedX, double speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    @Override
    public void update(double sec) {
        // no collision detection here!!! => implement it in subclass
        x += speedX * sec;
        y += speedY * sec;
    }

}
