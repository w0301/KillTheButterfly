package madscience.sprites;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class BossSprite extends EnemySprite {
    public static final SpriteView DEFAULT_VIEW;

    private static final Random RAND = new Random();
    private boolean oscillationStarted = false;

    static {
        BufferedImage bossImg = new BufferedImage(70, 100, BufferedImage.TYPE_INT_ARGB);
        try {
            bossImg = ImageIO.read(BossSprite.class.getResourceAsStream("/enemies/boss.png"));
        }
        catch (IOException ex) { }
        finally {
            DEFAULT_VIEW = new SpriteView(bossImg, new Rectangle2D[] { new Rectangle2D.Double(20, 0, bossImg.getWidth() - 20, bossImg.getHeight()),
                                                                       new Rectangle2D.Double(0, 47, 20, 110) });
        }
    }

    @Override
    protected void playShootedSound() {
    }

    public BossSprite(GameView game, SpriteView image, int lives) {
        super(game, image, lives);
    }

    public BossSprite(GameView game, int lives) {
        this(game, DEFAULT_VIEW, lives);
    }

    @Override
    public synchronized boolean shoot() {
        boolean retVal = super.shoot();
        if (retVal) {
            int min = (int) (shootingInterval / 4);
            shootInTime = RAND.nextInt(((int) shootingInterval) - min) + min;
        }
        return retVal;
    }

    @Override
    public boolean canOscillate() {
        return x <= game.getWidth() - getWidth() * 1.5;
    }

    @Override
    public void update(double sec) {
        if (canOscillate() && !oscillationStarted) {
            setSpeedXY(0, 0);
            game.setGameSpeed(0);
            oscillationStarted = true;
        }
        super.update(sec);
    }

    @Override
    public void performRemoved() {
        super.performRemoved();
        game.setBossSprite(null);
    }

}
