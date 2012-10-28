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
public class BulletSprite extends MovableSprite {

    AbstractSprite owner;

    public BulletSprite(Game game, double x, double y, AbstractSprite owner) {
        super(game, x, y);
        this.owner = owner;
    }

    public BulletSprite(Game game, AbstractSprite owner) {
        this(game, 0, 0, owner);
    }

    @Override
    public double getWidth() {
        return 5;
    }

    @Override
    public double getHeight() {
        return 10;
    }

    public AbstractSprite getOwner() {
        return owner;
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER) ||
            borders.contains(Game.Border.BOTTOM_BORDER) ||
            borders.contains(Game.Border.LEFT_BORDER) ||
            borders.contains(Game.Border.RIGHT_BORDER)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite.getClass() != owner.getClass() || sprite instanceof BulletSprite)
            game.removeSprite(this);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);

        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
