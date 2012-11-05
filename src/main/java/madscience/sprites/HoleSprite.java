package madscience.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class HoleSprite extends ElixirSprite {
    public static final BufferedImage HOLE_IMG;

    static {
        BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
        try {
            img = ImageIO.read(ElixirSprite.class.getResourceAsStream("/elixirs/hole.png"));
        }
        catch (IOException ex) { }
        finally {
            HOLE_IMG = img;
        }
    }


    public HoleSprite(GameView game, SpriteView view) {
        super(game, view);
    }

    public HoleSprite(GameView game, BufferedImage image) {
        this(game, new SpriteView(image));
    }

    public HoleSprite(GameView game) {
        this(game, HOLE_IMG);
    }

    @Override
    public void performElixir() {
        game.getPlayerSprite().removeLife();
    }

    @Override
    public void performElixirShooted() {
    }

}
