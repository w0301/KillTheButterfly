package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import madscience.views.GameView;

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

    public BulletSprite(GameView game, AbstractSprite owner, SpriteView view) {
        super(game, view);
        this.owner = owner;
    }

    public BulletSprite(GameView game, AbstractSprite owner, BufferedImage image) {
        super(game, new SpriteView(image));
        this.owner = owner;
    }

    public BulletSprite(GameView game, AbstractSprite owner) {
        this(game, owner, DEFAULT_IMG);
    }

    public AbstractSprite getOwner() {
        return owner;
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        EnumSet<GameView.Border> borders = game.getBorders(this);
        if (borders.contains(GameView.Border.TOP_BORDER) ||
            borders.contains(GameView.Border.BOTTOM_BORDER) ||
            borders.contains(GameView.Border.LEFT_BORDER) ||
            borders.contains(GameView.Border.RIGHT_BORDER)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if ( (sprite.getClass() != owner.getClass() || sprite instanceof BulletSprite) &&
            !(sprite instanceof HoleSprite) && !(getOwner() instanceof EnemySprite && sprite instanceof ElixirSprite) )
            game.removeSprite(this);
    }

    @Override
    public void performAdded() {
    }

    @Override
    public void performRemoved() {
    }

}
