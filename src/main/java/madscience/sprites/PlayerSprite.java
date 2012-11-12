package madscience.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import madscience.GameCanvas;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class PlayerSprite extends ShooterSprite {
    public static final int MAX_LIVES = 5;
    public static final SpriteView PLAYER_VIEW_1;
    public static final SpriteView PLAYER_VIEW_2;
    public static final SpriteView PLAYER_VIEW_3;
    public static final SpriteView PLAYER_SHIELDED_VIEW_1;
    public static final SpriteView PLAYER_SHIELDED_VIEW_2;
    public static final SpriteView PLAYER_SHIELDED_VIEW_3;
    private static final URL SHOOTED_SOUND;

    static {
        BufferedImage player1Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage player2Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage player3Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage playerShielded1Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage playerShielded2Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage playerShielded3Img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        try {
            player1Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player1.png"));
            player2Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player2.png"));
            player3Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player3.png"));
            playerShielded1Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player_shielded1.png"));
            playerShielded2Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player_shielded2.png"));
            playerShielded3Img = ImageIO.read(PlayerSprite.class.getResourceAsStream("/player/player_shielded3.png"));
        }
        catch (IOException ex) { }
        finally {
            PLAYER_VIEW_1 = new SpriteView(player1Img);
            PLAYER_VIEW_2 = new SpriteView(player2Img);
            PLAYER_VIEW_3 = new SpriteView(player3Img);
            PLAYER_SHIELDED_VIEW_1 = new SpriteView(playerShielded1Img);
            PLAYER_SHIELDED_VIEW_2 = new SpriteView(playerShielded2Img);
            PLAYER_SHIELDED_VIEW_3 = new SpriteView(playerShielded3Img);
        }

        SHOOTED_SOUND = EnemySprite.class.getResource("/sounds/player_shot.wav");
    }

    private int lives = MAX_LIVES;
    private boolean shield = false;

    protected void playShootedSound() {
        if(GameCanvas.isSoundEffectsPaused()) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(SHOOTED_SOUND));
            clip.start();
        }
        catch (LineUnavailableException e) { }
        catch (UnsupportedAudioFileException e) { }
        catch (IOException e) { }
    }

    public PlayerSprite(GameView game, SpriteView view) {
        super(game, view);
    }

    public PlayerSprite(GameView game, BufferedImage image) {
        this(game, new SpriteView(image));
    }

    public PlayerSprite(GameView game) {
        this(game, PLAYER_VIEW_1);
    }

    public int getLives() {
        return lives;
    }

    public boolean hasShield() {
        return shield;
    }

    public void setShield(boolean val) {
        boolean oldVal = shield;
        shield = val;
        if (oldVal != val) game.refreshPlayerView();
    }

    public void addLife() {
        lives += 1;
        if (lives > MAX_LIVES)
            lives = MAX_LIVES;
    }

    public void removeLife() {
        lives -= 1;
    }

    @Override
    public void update(double sec) {
        double beforeX = x, beforeY = y;
        super.update(sec);

        EnumSet<GameView.Border> borders = game.getBorders(this);
        if (borders.contains(GameView.Border.TOP_BORDER ) ||
            borders.contains(GameView.Border.BOTTOM_BORDER) ||
            borders.contains(GameView.Border.LEFT_BORDER) ||
            borders.contains(GameView.Border.RIGHT_BORDER)) {
            x = beforeX;
            y = beforeY;
            setSpeedXY(0, 0);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if ( (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() != this) ||
             (sprite instanceof EnemySprite) || (sprite instanceof HoleSprite) ) {
            if (hasShield() && !(sprite instanceof HoleSprite)) {
                setShield(false);
                ElixirSprite.playShieldShootedSound();
            }
            else {
                removeLife();
                playShootedSound();
            }
        }
    }

}
