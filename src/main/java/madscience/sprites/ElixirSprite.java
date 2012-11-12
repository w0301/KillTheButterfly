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
public abstract class ElixirSprite extends MovableSprite {
    public static final BufferedImage LIFE_IMG;
    public static final BufferedImage SHIELD_IMG;
    public static final BufferedImage SLOWDOWN_IMG;
    public static final BufferedImage FASTFORWARD_IMG;
    private static final URL ELIXIR_SHOOTED_SOUND;
    private static final URL SHIELD_SHOOTED_SOUND;

    static {
        BufferedImage lifeImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage shieldImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage slowdownImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        BufferedImage fastforwardImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        try {
            lifeImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/elixirs/life.png"));
            shieldImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/elixirs/shield.png"));
            slowdownImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/elixirs/slowdown.png"));
            fastforwardImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/elixirs/fastforward.png"));
        }
        catch (IOException ex) { }
        finally {
            LIFE_IMG = lifeImg;
            SHIELD_IMG = shieldImg;
            SLOWDOWN_IMG = slowdownImg;
            FASTFORWARD_IMG = fastforwardImg;
        }

        ELIXIR_SHOOTED_SOUND = EnemySprite.class.getResource("/sounds/glass_break.wav");
        SHIELD_SHOOTED_SOUND = EnemySprite.class.getResource("/sounds/shield_shot.wav");
    }

    protected void playElixirShootedSound() {
        if(GameCanvas.isSoundEffectsPaused()) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(ELIXIR_SHOOTED_SOUND));
            clip.start();
        }
        catch (LineUnavailableException e) { }
        catch (UnsupportedAudioFileException e) { }
        catch (IOException e) { }
    }

    public static void playShieldShootedSound() {
        if(GameCanvas.isSoundEffectsPaused()) return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(SHIELD_SHOOTED_SOUND));
            clip.start();
        }
        catch (LineUnavailableException e) { }
        catch (UnsupportedAudioFileException e) { }
        catch (IOException e) { }
    }

    public ElixirSprite(GameView game, SpriteView view) {
        super(game, view);
    }

    public ElixirSprite(GameView game, BufferedImage image) {
        this(game, new SpriteView(image));
    }

    @Override
    public void performAdded() {
    }

    @Override
    public void performRemoved() {
    }

    @Override
    public void update(double sec) {
        super.update(sec);

        EnumSet<GameView.Border> borders = game.getBorders(this);
        if (borders.contains(GameView.Border.LEFT_BORDER_CROSSED)) {
            game.removeSprite(this);
        }
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite == game.getPlayerSprite()) {
            performElixir();
            game.removeSprite(this);
        }
        else if (sprite instanceof BulletSprite && ((BulletSprite) sprite).getOwner() == game.getPlayerSprite()) {
            performElixirShooted();
        }
    }

    public abstract void performElixir();

    public void performElixirShooted() {
        HoleSprite toAdd = new HoleSprite(game);
        toAdd.setXY(x, y);
        toAdd.setSpeedXY(speedX, speedY);
        game.addSprite(toAdd);
        game.removeSprite(this);
        playElixirShootedSound();
    }

}
