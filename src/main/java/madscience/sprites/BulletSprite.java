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

        Game.SpriteIntersection inter = game.getIntersection(this);
        if (inter.hasType(Game.SpriteIntersection.Type.TOP_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.BOTTOM_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.LEFT_BORDER) ||
            inter.hasType(Game.SpriteIntersection.Type.RIGHT_BORDER)) {
            game.removeSprite(this);
        }
        else if (inter.hasType(Game.SpriteIntersection.Type.OTHER_SPRITE)) {
            for (AbstractSprite sprite : inter.getOtherSprites()) {
                if (sprite.getClass() != owner.getClass() || sprite instanceof BulletSprite)
                    game.removeSprite(this);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.YELLOW);

        g.draw(new Rectangle2D.Double(x, y, getWidth(), getHeight()));
    }

}
