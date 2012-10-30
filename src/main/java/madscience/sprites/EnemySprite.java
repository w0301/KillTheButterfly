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
public class EnemySprite extends ShooterSprite {
    public static final BufferedImage DEFAULT_IMG_1;
    public static final BufferedImage DEFAULT_IMG_2;
    public static final BufferedImage DEFAULT_IMG_3;

    static {
        DEFAULT_IMG_1 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = DEFAULT_IMG_1.createGraphics();
        g.setColor(Color.BLUE);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_1.getWidth() - 1, DEFAULT_IMG_1.getHeight() - 1));

        DEFAULT_IMG_2 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        g = DEFAULT_IMG_2.createGraphics();
        g.setColor(Color.GREEN);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_2.getWidth() - 1, DEFAULT_IMG_2.getHeight() - 1));

        DEFAULT_IMG_3 = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        g = DEFAULT_IMG_3.createGraphics();
        g.setColor(Color.PINK);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_3.getWidth() - 1, DEFAULT_IMG_3.getHeight() - 1));
    }

    int lives;
    double oscillationCenter = 0;
    double oscillationTime = -1;

    public EnemySprite(Game game, BufferedImage image, int lives) {
        super(game, image);
        this.lives = lives;
    }

    public EnemySprite(Game game, int lives) {
        this(game, DEFAULT_IMG_1, lives);
    }

    @Override
    public void update(double sec) {
        double beforeX = x, beforeY = y;
        super.update(sec);

        double ampl = 50;
        double period = 3;
        if (oscillationTime == -1) oscillationCenter = y;
        if (oscillationTime >= period || oscillationTime == -1) {
           oscillationTime = 0;
        }
        y = ampl * Math.sin(oscillationTime * 2 * Math.PI / period) + oscillationCenter;
        oscillationTime += sec;

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER)) {
            x = beforeX;
            y = beforeY;
            //oscillationTime += period / 4;
        }
        else if (borders.contains(Game.Border.BOTTOM_BORDER)) {
            x = beforeX;
            y = beforeY;
            //oscillationTime = (3 / 4) * period;
        }

        if (borders.contains(Game.Border.LEFT_BORDER_CROSSED)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() == game.getPlayerSprite())
            lives--;
        if (lives <= 0 || sprite instanceof PlayerSprite) game.removeSprite(this);
    }

}
