package madscience.sprites;

import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public abstract class MovableSprite extends AbstractSprite {
    protected double speedX = 0, speedY = 0;

    public MovableSprite(GameView game, SpriteView view) {
        super(game, view);
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
        super.update(sec);

        // no collision detection here!!! => implement it in subclass
        x += speedX * sec;
        y += speedY * sec;
    }

}
