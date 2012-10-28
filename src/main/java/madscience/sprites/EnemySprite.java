package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class EnemySprite extends ShooterSprite {

    int lives;
    int descendingTime;
    long lastUpdateTime = -1;
    double lastX, lastY;

    public EnemySprite(Game game, double x, double y, int lives, int descendingTime) {
        super(game, x, y);
        this.lives = lives;
        this.descendingTime = descendingTime;
    }

    public EnemySprite(Game game, int lives, int descendingTime) {
        this(game, 0, 0, lives, descendingTime);
    }

    @Override
    public double getWidth() {
        return 25;
    }

    @Override
    public double getHeight() {
        return 25;
    }

    @Override
    public void update(double sec) {
        long now = System.currentTimeMillis();
        if (lastUpdateTime != -1) {
            descendingTime -= (now - lastUpdateTime);
        }
        if (descendingTime <= 0) setSpeedXY(0, 0);

        double beforeX = x, beforeY = y;
        super.update(sec);

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER) ||
            borders.contains(Game.Border.BOTTOM_BORDER)) {
            x = lastX;
            y = lastY;
            setSpeedXY(getSpeedX(), -getSpeedY());
        }
        if (borders.contains(Game.Border.LEFT_BORDER) ||
            borders.contains(Game.Border.RIGHT_BORDER)) {
            x = lastX;
            y = lastY;
            setSpeedXY(-getSpeedX(), getSpeedY());
        }

        lastUpdateTime = now;
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() != this)
            lives--;
        if (lives <= 0) game.removeSprite(this);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
