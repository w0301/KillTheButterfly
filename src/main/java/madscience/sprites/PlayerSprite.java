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
public class PlayerSprite extends ShooterSprite {

    private double lastX, lastY;

    public PlayerSprite(Game game, double x, double y) {
        super(game, x, y);
    }

    public PlayerSprite(Game game) {
        this(game, 0, 0);
    }

    @Override
    public double getWidth() {
        return 15;
    }

    @Override
    public double getHeight() {
        return 30;
    }

    @Override
    public void update(double sec) {
        lastX = x;
        lastY = y;
        super.update(sec);

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER ) ||
            borders.contains(Game.Border.BOTTOM_BORDER) ||
            borders.contains(Game.Border.LEFT_BORDER) ||
            borders.contains(Game.Border.RIGHT_BORDER)) {
            x = lastX;
            y = lastY;
            setSpeedXY(0, 0);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() != this)
            game.addPlayerLives(-1);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
