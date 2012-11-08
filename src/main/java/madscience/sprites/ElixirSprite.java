package madscience.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumSet;
import javax.imageio.ImageIO;
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
    }

}
