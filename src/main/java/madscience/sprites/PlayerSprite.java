package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public class PlayerSprite extends ShooterSprite {
    public static final int MAX_LIVES = 5;
    public static final SpriteView DEFAULT_VIEW;
    public static final SpriteView DEFAULT_VIEW_1;
    //public static final SpriteView DEFAULT_VIEW_2;

    static {

        Rectangle2D bodyRect = new Rectangle2D.Double(0, 0, 49, 64);
        Rectangle2D legRect = new Rectangle2D.Double(22.5, 65, 5, 30);

        BufferedImage img = new BufferedImage(50, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.draw(bodyRect);
        g.draw(legRect);
        DEFAULT_VIEW = new SpriteView(img, new Rectangle2D[] {bodyRect, legRect});

        img = new BufferedImage(50, 100, BufferedImage.TYPE_INT_ARGB);
        g = img.createGraphics();
        g.setColor(Color.RED);
        g.draw(bodyRect);
        legRect.setRect(12.5, 65, 5, 30);
        g.draw(legRect);
        legRect.setRect(32.5, 65, 5, 30);
        g.draw(legRect);
        DEFAULT_VIEW_1 = new SpriteView(img, new Rectangle2D[] {bodyRect,
                                                new Rectangle2D.Double(12.5, 65, 5, 30),
                                                new Rectangle2D.Double(32.5, 65, 5, 30)});
    }

    private int lives = MAX_LIVES;

    public PlayerSprite(Game game, SpriteView view) {
        super(game, view);
    }

    public PlayerSprite(Game game, BufferedImage image) {
        this(game, new SpriteView(image));
    }

    public PlayerSprite(Game game) {
        this(game, DEFAULT_VIEW);
    }

    public int getLives() {
        return lives;
    }

    public void addLives(int val) {
        lives += val;
        if (lives > MAX_LIVES)
            lives = MAX_LIVES;
    }

    @Override
    public void update(double sec) {
        double beforeX = x, beforeY = y;
        super.update(sec);

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER ) ||
            borders.contains(Game.Border.BOTTOM_BORDER) ||
            borders.contains(Game.Border.LEFT_BORDER) ||
            borders.contains(Game.Border.RIGHT_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(0, 0);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if ( (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() != this) ||
             (sprite instanceof EnemySprite) )
            addLives(-1);
    }

}
