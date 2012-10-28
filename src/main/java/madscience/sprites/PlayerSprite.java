package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class PlayerSprite extends MovableSprite {

    public PlayerSprite(Game game, double x, double y) {
        super(game, x, y);
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
        double beforeX = x, beforeY = y;
        super.update(sec);

        Game.SpriteIntersection inter = game.getIntersection(this);
        if (inter.hasType(Game.SpriteIntersection.Type.TOP_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.BOTTOM_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.LEFT_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.RIGHT_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(0, 0);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
