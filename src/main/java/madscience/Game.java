package madscience;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import madscience.sprites.AbstractSprite;

/**
 *
 * @author Richard Kakaš
 */
public final class Game {

    public static class SpriteIntersection {
        public enum Type {
            TOP_BORDER, BOTTOM_BORDER,
            LEFT_BORDER, RIGHT_BORDER,
            OTHER_SPRITE
        }

        private List<Type> types = new ArrayList<Type>();
        private AbstractSprite otherSprite = null;

        public SpriteIntersection() {

        }

        public void addType(Type t) {
            types.add(t);
        }

        public boolean hasType(Type t) {
            return types.contains(t);
        }

        public AbstractSprite getOtherSprite() {
            return otherSprite;
        }

        public void setOtherSprite(AbstractSprite otherSprite) {
            this.otherSprite = otherSprite;
        }

    }

    private List<AbstractSprite> sprites;
    private int canvasWidth = 0, canvasHeight = 0;

    public Game() {
        sprites = new LinkedList<AbstractSprite>();
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public SpriteIntersection getIntersection(AbstractSprite sprite) {
        SpriteIntersection ret = new SpriteIntersection();

        if (sprite.getX() <= 0)
            ret.addType(SpriteIntersection.Type.LEFT_BORDER);
        else if (sprite.getX() + sprite.getWidth() >= getCanvasWidth())
            ret.addType(SpriteIntersection.Type.RIGHT_BORDER);
        if (sprite.getY() <= 0)
            ret.addType(SpriteIntersection.Type.TOP_BORDER);
        else if (sprite.getY() + sprite.getHeight() >= getCanvasHeight())
            ret.addType(SpriteIntersection.Type.BOTTOM_BORDER);

        for (AbstractSprite otherSprite : sprites) {
            if (sprite == otherSprite) continue;
            if (sprite.intersects(otherSprite)) {
                ret.addType(SpriteIntersection.Type.OTHER_SPRITE);
                ret.setOtherSprite(otherSprite);
                break;
            }
        }

        return ret;
    }

    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public void update(double sec) {
        for (AbstractSprite sprite : sprites) sprite.update(sec);

    }

    public void draw(Graphics2D g) {
        for (AbstractSprite sprite : sprites) sprite.draw(g);

    }

}
