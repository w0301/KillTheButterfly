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
public class PlayerSprite extends ShooterSprite {
    public static final int MAX_LIVES = 3;
    public static final BufferedImage DEFAULT_IMG;

    static {
        DEFAULT_IMG = new BufferedImage(50, 75, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = DEFAULT_IMG.createGraphics();
        g.setColor(Color.RED);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG.getWidth() - 1, DEFAULT_IMG.getHeight() - 1));
    }

    private int lives = MAX_LIVES;

    public PlayerSprite(Game game, BufferedImage image) {
        super(game, image);
    }

    public PlayerSprite(Game game) {
        this(game, DEFAULT_IMG);
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
