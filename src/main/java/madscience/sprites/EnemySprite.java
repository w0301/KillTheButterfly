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
    double oscillationAmpl = 30;
    double oscillationPeriod = 3;

    public EnemySprite(Game game, SpriteView view, int lives) {
        super(game, view);
        this.lives = lives;
    }

    public EnemySprite(Game game, BufferedImage image, int lives) {
        this(game, new SpriteView(image), lives);
    }

    public EnemySprite(Game game, int lives) {
        this(game, DEFAULT_IMG_1, lives);
    }

    public void setOscillationAmplitude(double ampl) {
        oscillationAmpl = ampl;
    }

    public void setOscillationPeriod(double period) {
        oscillationPeriod = period;
        oscillationTime = -1;
    }

    public void setOscillation(double ampl, double period) {
        setOscillationAmplitude(ampl);
        setOscillationPeriod(period);
    }

    public boolean canOscillate() {
        return true;
    }

    public double oscillationFunction() {
        double parM1 = 1 / (oscillationPeriod / 2);
        return (2 * Math.asin(Math.sin(parM1 * Math.PI * oscillationTime))) / Math.PI;
        // return Math.sin(oscillationTime * 2 * Math.PI / oscillationPeriod);
    }

    @Override
    public void update(double sec) {
        double beforeX = x, beforeY = y;
        super.update(sec);

        if (canOscillate()) {
            if (oscillationTime == -1) {
                oscillationTime = 0;
                oscillationCenter = y;
            }
            y = oscillationCenter + oscillationAmpl * oscillationFunction();

            oscillationTime += sec;
            if (oscillationTime >= oscillationPeriod) oscillationTime = 0;
        }

        EnumSet<Game.Border> borders = game.getBorders(this);
        if (borders.contains(Game.Border.TOP_BORDER) ||
            borders.contains(Game.Border.BOTTOM_BORDER)) {
            x = beforeX;
            y = beforeY;
            if (oscillationTime <= oscillationPeriod / 4)
                oscillationTime = oscillationPeriod / 4;
            else if (oscillationTime <= 2*oscillationPeriod / 4)
                oscillationTime = 2*oscillationPeriod / 4;
            else if (oscillationTime <= 3*oscillationPeriod / 4)
                oscillationTime = 3*oscillationPeriod / 4;
            else if (oscillationTime <= oscillationPeriod)
                oscillationTime = 0;
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
