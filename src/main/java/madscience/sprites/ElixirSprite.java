package madscience.sprites;

import java.awt.image.BufferedImage;
import madscience.Game;

/**
 *
 * @author Richard Kakaš
 */
public abstract class ElixirSprite extends MovableSprite {

    public ElixirSprite(Game game, SpriteView view) {
        super(game, view);
    }

    public ElixirSprite(Game game, BufferedImage image) {
        super(game, new SpriteView(image));
    }

    @Override
    public void performAdded() {
    }

    @Override
    public void performRemoved() {
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
        if (sprite == game.getPlayerSprite()) {
            performElixir();
            game.removeSprite(this);
        }
    }

    public abstract void performElixir();

}
