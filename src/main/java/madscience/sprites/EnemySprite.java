package madscience.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class EnemySprite extends ShooterSprite {
    public static final BufferedImage ENEMY_IMG_1;
    public static final BufferedImage ENEMY_IMG_2;
    public static final BufferedImage ENEMY_IMG_3;
    private static final URL SHOOTED_SOUND;
    private static final URL DEAD_SOUND;

    static {
        BufferedImage enemy1Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage enemy2Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage enemy3Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        try {
            enemy1Img = ImageIO.read(EnemySprite.class.getResourceAsStream("/enemies/enemy1.png"));
            enemy2Img = ImageIO.read(EnemySprite.class.getResourceAsStream("/enemies/enemy2.png"));
            enemy3Img = ImageIO.read(EnemySprite.class.getResourceAsStream("/enemies/enemy3.png"));
        }
        catch (IOException ex) { }
        finally {
            ENEMY_IMG_1 = enemy1Img;
            ENEMY_IMG_2 = enemy2Img;
            ENEMY_IMG_3 = enemy3Img;
        }

        SHOOTED_SOUND = EnemySprite.class.getResource("/sounds/enemy_shot.wav");
        DEAD_SOUND = EnemySprite.class.getResource("/sounds/enemy_dead.wav");
    }

    int lives;
    int startLives;
    double oscillationCenter = 0;
    double oscillationTime = 0;
    double oscillationAmpl = 30;
    double oscillationPeriod = 2;

    protected void playShootedSound() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(SHOOTED_SOUND));
            clip.start();
        }
        catch (LineUnavailableException e) { }
        catch (UnsupportedAudioFileException e) { }
        catch (IOException e) { }
    }

    protected void playDeadSound() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(DEAD_SOUND));
            clip.start();
        }
        catch (LineUnavailableException e) { }
        catch (UnsupportedAudioFileException e) { }
        catch (IOException e) { }
    }

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
        boolean lifeRemoved = false;
        if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() == game.getPlayerSprite()) {
            lives--;
            lifeRemoved = true;
        }

        if (lives <= 0 || sprite instanceof PlayerSprite) {
            game.removeSprite(this);
            game.addPlayerScore(startLives * 5);
            playDeadSound();
        }
        else if (lifeRemoved) playShootedSound();
    }

}
