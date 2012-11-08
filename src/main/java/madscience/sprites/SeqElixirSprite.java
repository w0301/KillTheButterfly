package madscience.sprites;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import madscience.views.GameView;

/**
 *
 * @author Richard Kakaš
 */
public class SeqElixirSprite extends ElixirSprite implements Comparable<SeqElixirSprite> {
    public static final BufferedImage RED_IMG;
    public static final BufferedImage BLUE_IMG;
    public static final BufferedImage GREEN_IMG;
    public static final BufferedImage YELLOW_IMG;

    static {
        BufferedImage redImg = new BufferedImage(75, 75, BufferedImage.TYPE_INT_ARGB);
        BufferedImage blueImg = new BufferedImage(75, 75, BufferedImage.TYPE_INT_ARGB);
        BufferedImage greenImg = new BufferedImage(75, 75, BufferedImage.TYPE_INT_ARGB);
        BufferedImage yellowImg = new BufferedImage(75, 75, BufferedImage.TYPE_INT_ARGB);
        try {
            redImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/seq_elixirs/red.png"));
            blueImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/seq_elixirs/blue.png"));
            greenImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/seq_elixirs/green.png"));
            yellowImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/seq_elixirs/yellow.png"));
        }
        catch (IOException e) { }
        finally {
            RED_IMG = redImg;
            BLUE_IMG = blueImg;
            GREEN_IMG = greenImg;
            YELLOW_IMG = yellowImg;
        }
    }

    public SeqElixirSprite(GameView game, SpriteView view) {
        super(game, view);
    }

    public SeqElixirSprite(GameView game, BufferedImage image) {
        this(game, new SpriteView(image));
    }

    public SeqElixirSprite(GameView game) {
        this(game, RED_IMG);
    }

    @Override
    public void performIntersection(AbstractSprite sprite) {
    }

    @Override
    public void performAdded() {
    }

    @Override
    public void performRemoved() {
    }

    @Override
    public void performElixir() {
    }

    @Override
    public void performElixirShooted() {
    }    

    @Override
    public int compareTo(SeqElixirSprite t) {
        if (currView.image == t.currView.image) return 0;
        return -1;
    }

}
