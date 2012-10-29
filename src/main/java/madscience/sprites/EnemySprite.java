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
        DEFAULT_IMG_1 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = DEFAULT_IMG_1.createGraphics();
        g.setColor(Color.BLUE);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_1.getWidth() - 1, DEFAULT_IMG_1.getHeight() - 1));

        DEFAULT_IMG_2 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        g = DEFAULT_IMG_2.createGraphics();
        g.setColor(Color.GREEN);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_2.getWidth() - 1, DEFAULT_IMG_2.getHeight() - 1));

        DEFAULT_IMG_3 = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        g = DEFAULT_IMG_3.createGraphics();
        g.setColor(Color.PINK);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG_3.getWidth() - 1, DEFAULT_IMG_3.getHeight() - 1));
    }

    int lives;
    double descendingTime = 0;
    double descendedX = 0, descendedY = 0;
    double oscillationTime = 0;

    public EnemySprite(Game game, BufferedImage image, int lives) {
        super(game, image);
        this.lives = lives;
    }

    public EnemySprite(Game game, int lives) {
        this(game, DEFAULT_IMG_1, lives);
    }

    public void setDescendingTime(double descendingTime) {
        this.descendingTime = descendingTime;
    }

    @Override
    public void update(double sec) {
        double beforeX = x, beforeY = y;
        super.update(sec);

        if (descendingTime > 0) descendingTime -= sec * 1000;
        else {
            if (oscillationTime == 0) {
                descendedX = beforeX;
                descendedY = beforeY;
            }
            double ampl = Math.min(descendedX, game.getCanvasWidth() - descendedX - getWidth());
            double period = (ampl / Math.abs(getSpeedY())) * 4;
            x = ampl * Math.sin(oscillationTime * 2 * Math.PI / period) + descendedX;
            y = descendedY;
            oscillationTime += sec;
        }

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER) ||
            borders.contains(Game.Border.BOTTOM_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(getSpeedX(), -getSpeedY());
            oscillationTime = 0;
        }
        if (borders.contains(Game.Border.LEFT_BORDER) ||
            borders.contains(Game.Border.RIGHT_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(-getSpeedX(), getSpeedY());
            oscillationTime = 0;
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner().getClass() != getClass())
            lives--;
        if (lives <= 0) game.removeSprite(this);
    }

}
