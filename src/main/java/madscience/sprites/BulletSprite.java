package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class BulletSprite extends MovableSprite {
    public static final BufferedImage DEFAULT_IMG;

    static {
        DEFAULT_IMG = new BufferedImage(10, 5, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = DEFAULT_IMG.createGraphics();
        g.setColor(Color.YELLOW);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG.getWidth() - 1, DEFAULT_IMG.getHeight() - 1));
    }

    AbstractSprite owner;

    public BulletSprite(Game game, AbstractSprite owner, BufferedImage image) {
        super(game, image);
        this.owner = owner;
    }

    public BulletSprite(Game game, AbstractSprite owner) {
        this(game, owner, DEFAULT_IMG);
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

}
