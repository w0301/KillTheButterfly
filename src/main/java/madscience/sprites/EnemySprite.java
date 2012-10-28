package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class EnemySprite extends ShooterSprite {

    int lives;
    int descendingTime;
    long lastUpdateTime = -1;

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
        double beforeX = x, beforeY = y;
        super.update(sec);

        if (lastUpdateTime != -1) {
            descendingTime -= (now - lastUpdateTime);
        }
        if (descendingTime <= 0) setSpeedXY(0, 0);

        Game.SpriteIntersection inter = game.getIntersection(this);
        if (inter.hasType(Game.SpriteIntersection.Type.TOP_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.BOTTOM_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(getSpeedX(), -getSpeedY());
        }
        if (inter.hasType(Game.SpriteIntersection.Type.LEFT_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.RIGHT_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(-getSpeedX(), getSpeedY());
        }

        if (inter.hasType(Game.SpriteIntersection.Type.OTHER_SPRITE)) {
            for (AbstractSprite sprite : inter.getOtherSprites()) {
                if (sprite instanceof BulletSprite &&
                    ((BulletSprite) sprite).getOwner() != this) {
                    lives--;
                    //game.removeSprite(sprite);
                }
            }
        }

        if (lives <= 0) game.removeSprite(this);
        lastUpdateTime = now;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
