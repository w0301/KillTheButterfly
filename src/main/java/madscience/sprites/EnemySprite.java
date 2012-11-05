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
    int startLives;
    double oscillationCenter = 0;
    double oscillationTime = 0;
    double oscillationAmpl = 50;
    double oscillationPeriod = 2;

    public EnemySprite(GameView game, SpriteView view, int lives) {
        super(game, view);
        this.lives = this.startLives = lives;
    }

    public EnemySprite(GameView game, BufferedImage image, int lives) {
        this(game, new SpriteView(image), lives);
    }

    public EnemySprite(GameView game, int lives) {
        this(game, DEFAULT_IMG_1, lives);
    }

    public int getLives() {
        return lives;
    }

    public double getOscillationAmplitude() {
        return oscillationAmpl;
    }

    public void setOscillationAmplitude(double ampl) {
        oscillationAmpl = ampl;
    }

    public double getOscillationPeriod() {
        return oscillationPeriod;
    }

    public void setOscillationPeriod(double period) {
        oscillationPeriod = period;
        oscillationTime = 0;
    }

    public void setOscillation(double ampl, double period) {
        setOscillationAmplitude(ampl);
        setOscillationPeriod(period);
    }

    public boolean canOscillate() {
        return true;
    }

    public double oscillationFunction(double param) {
        //if (param < oscillationPeriod / 2) return 2*param / oscillationPeriod;
        //else return -(2 * param) / (oscillationPeriod + 2) + oscillationPeriod / (oscillationPeriod + 2);
        //double omega = (2 * Math.PI / oscillationPeriod);
        //return omega * (2 * Math.asin(Math.cos(omega * param))) / Math.PI;
        //return omega * Math.cos(omega * param);
        return Math.signum(Math.sin((2*Math.PI * param) / oscillationPeriod));
    }

    @Override
    public void update(double sec) {
        if (canOscillate()) {
            speedY = (oscillationAmpl / (oscillationPeriod / 2)) * oscillationFunction(oscillationTime);

            oscillationTime += sec;
            if (oscillationTime >= oscillationPeriod)
                oscillationTime -= ((int) (oscillationTime / oscillationPeriod)) * oscillationPeriod;
        }

        double beforeX = x, beforeY = y;
        super.update(sec);

        EnumSet<GameView.Border> borders = game.getBorders(this);
        if (borders.contains(GameView.Border.TOP_BORDER) ||
            borders.contains(GameView.Border.BOTTOM_BORDER)) {
            x = beforeX;
            y = beforeY;
            if (canOscillate()) {
                if (oscillationTime <= oscillationPeriod / 4)
                    oscillationTime = oscillationPeriod / 4;
                else if (oscillationTime <= 2*oscillationPeriod / 4)
                    oscillationTime = 2*oscillationPeriod / 4;
                else if (oscillationTime <= 3*oscillationPeriod / 4)
                    oscillationTime = 3*oscillationPeriod / 4;
                else if (oscillationTime <= oscillationPeriod)
                    oscillationTime = 0;
            }
        }
        if (borders.contains(GameView.Border.LEFT_BORDER_CROSSED)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() == game.getPlayerSprite())
            lives--;
        if (lives <= 0 || sprite instanceof PlayerSprite) {
            game.removeSprite(this);
            game.addPlayerScore(startLives * 5);
        }
    }

}
