package madscience;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import madscience.sprites.AbstractSprite;
import madscience.sprites.PlayerSprite;

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
    private PlayerSprite playerSprite;

    private int canvasWidth = 0, canvasHeight = 0;
    private long lastUpdate = 0;

    // pixels / second
    private double playerSetSpeed = 100;

    public Game(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;

        playerSprite = new PlayerSprite(this, canvasWidth / 2, canvasHeight / 2);

        sprites = new LinkedList<AbstractSprite>();
        sprites.add(playerSprite);
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public double getPlayerSetSpeed() {
        return playerSetSpeed;
    }

    public void setPlayerSetSpeed(double playerSetSpeed) {
        this.playerSetSpeed = playerSetSpeed;
    }

    public double getPlayerSpriteSpeedX() {
        return playerSprite.getSpeedX();
    }

    public double getPlayerSpriteSpeedY() {
        return playerSprite.getSpeedY();
    }

    public void setPlayerSpriteSpeedXY(double x, double y) {
        playerSprite.setSpeedXY(x, y);
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

    public void update(double sec) {
        long now = System.currentTimeMillis();
        for (AbstractSprite sprite : sprites) sprite.update(sec);


        lastUpdate = now;
    }

    public void draw(Graphics2D g) {
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

}
