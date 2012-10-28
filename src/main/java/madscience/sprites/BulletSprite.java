package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class BulletSprite extends MovableSprite {

    public BulletSprite(Game game, double x, double y) {
        super(game, x, y);
    }

    public BulletSprite(Game game) {
        this(game, 0, 0);
    }

    @Override
    public double getWidth() {
        return 5;
    }

    @Override
    public double getHeight() {
        return 10;
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        Game.SpriteIntersection inter = game.getIntersection(this);
        if (inter.hasType(Game.SpriteIntersection.Type.TOP_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.BOTTOM_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.LEFT_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.RIGHT_BORDER)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);

        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
