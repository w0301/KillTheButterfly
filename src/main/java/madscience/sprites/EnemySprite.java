package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;
import javax.imageio.ImageIO;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class EnemySprite extends ShooterSprite {
    public static final BufferedImage ENEMY_IMG_1;
    public static final BufferedImage ENEMY_IMG_2;
    public static final BufferedImage ENEMY_IMG_3;

    static {
        BufferedImage enemy1Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage enemy2Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage enemy3Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        try {
            enemy1Img = ImageIO.read(ElixirSprite.class.getResourceAsStream("/enemies/enemy1.png"));
            enemy2Img = ImageIO.read(ElixirSprite.class.getResourceAsStream("/enemies/enemy2.png"));
            enemy3Img = ImageIO.read(ElixirSprite.class.getResourceAsStream("/enemies/enemy3.png"));
        }
        catch (IOException ex) { }
        finally {
            ENEMY_IMG_1 = enemy1Img;
            ENEMY_IMG_2 = enemy2Img;
            ENEMY_IMG_3 = enemy3Img;
        }
    }

    int lives;
    int startLives;
    double oscillationCenter = 0;
    double oscillationTime = 0;
    double oscillationAmpl = 30;
    double oscillationPeriod = 2;

    public EnemySprite(GameView game, SpriteView view, int lives) {
        super(game, view);
        this.lives = this.startLives = lives;
    }

    public EnemySprite(GameView game, BufferedImage image, int lives) {
        this(game, new SpriteView(image), lives);
    }

    public EnemySprite(GameView game, int lives) {
        this(game, ENEMY_IMG_1, lives);
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

    public double oscillationFunction(double param, double period) {
        //if (param < oscillationPeriod / 2) return 2*param / oscillationPeriod;
        //else return -(2 * param) / (oscillationPeriod + 2) + oscillationPeriod / (oscillationPeriod + 2);
        double omega = (2 * Math.PI / period);
        //return omega * (2 * Math.asin(Math.cos(omega * param))) / Math.PI;
        //return omega * Math.cos(omega * param);
        return Math.signum(Math.sin(omega * param));
    }

    @Override
    public void update(double sec) {
        if (canOscillate()) {
            speedY = (oscillationAmpl / (oscillationPeriod / 4)) * oscillationFunction(oscillationTime, oscillationPeriod);

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
