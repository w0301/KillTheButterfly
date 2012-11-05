package madscience.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class BossSprite extends EnemySprite {
    public static final BufferedImage DEFAULT_IMG;

    boolean oscillationStarted = false;

    static {
        DEFAULT_IMG = new BufferedImage(70, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = DEFAULT_IMG.createGraphics();
        g.setColor(Color.ORANGE);
        g.draw(new Rectangle2D.Double(0, 0, DEFAULT_IMG.getWidth() - 1, DEFAULT_IMG.getHeight() - 1));
    }

    public BossSprite(GameView game, BufferedImage image, int lives) {
        super(game, image, lives);
    }

    public BossSprite(GameView game, int lives) {
        this(game, DEFAULT_IMG, lives);
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
