package madscience;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import madscience.sprites.AbstractSprite;
import madscience.sprites.EnemySprite;
import madscience.sprites.PlayerSprite;
import madscience.sprites.ShooterSprite;

/**
 *
 * @author Richard Kakaš
 */
public final class Game {
    public static final int MAX_PLAYER_LIVES = 3;

    public static class SpriteIntersection {
        public enum Type {
            TOP_BORDER, BOTTOM_BORDER,
            LEFT_BORDER, RIGHT_BORDER,
            OTHER_SPRITE
        }

        private List<Type> types = new ArrayList<Type>();
        private List<AbstractSprite> otherSprites = new ArrayList<AbstractSprite>();

        public SpriteIntersection() {

        }

        public void addType(Type t) {
            if (!types.contains(t)) types.add(t);
        }

        public boolean hasType(Type t) {
            return types.contains(t);
        }

        public List<AbstractSprite> getOtherSprites() {
            return otherSprites;
        }

        public void addOtherSprite(AbstractSprite otherSprite) {
            otherSprites.add(otherSprite);
        }

    }

    private Random rand = new Random();
    private int canvasWidth = 0, canvasHeight = 0;

    // game info
    private int playerScore = 0;
    private int playerLives = MAX_PLAYER_LIVES;

    // enemy generations
    private int enemiesToGen = 5;
    private int enemiesGenInterval = 2000;
    private long lastEnemyGenTime = 0;

    // pixels / second
    private double playerSetSpeed = 100;
    private double playerBulletSpeed = 150;

    // sprites
    private List<AbstractSprite> sprites;
    private PlayerSprite playerSprite;

    private List<AbstractSprite> spritesToAdd;
    private List<AbstractSprite> spritesToRemove;

    public Game(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;

        playerSprite = new PlayerSprite(this);
        playerSprite.setXY(canvasWidth / 2 - playerSprite.getWidth() / 2,
                           canvasHeight / 2 - playerSprite.getHeight() / 2);
        playerSprite.addGun(new ShooterSprite.Gun(playerSprite.getWidth() / 2, 0,
                                                  0, -playerBulletSpeed));

        sprites = new LinkedList<AbstractSprite>();
        sprites.add(playerSprite);

        spritesToAdd = new ArrayList<AbstractSprite>();
        spritesToRemove = new ArrayList<AbstractSprite>();
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getPlayerScore() {
        return playerScore;
    }

    public void addPlayerScore(int score) {
        playerScore += score;
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void addPlayerLives(int lives) {
        playerLives += lives;
        if (playerLives > MAX_PLAYER_LIVES)
            playerLives = MAX_PLAYER_LIVES;
    }

    public PlayerSprite getPlayerSprite() {
        return playerSprite;
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
                ret.addOtherSprite(otherSprite);
            }
        }

        return ret;
    }

    public boolean isLost() {
        return playerLives <= -1;
    }

    public boolean isWon() {
        return enemiesToGen == 0 && sprites.size() == 1;
    }

    public void addSprite(AbstractSprite sprite) {
        spritesToAdd.add(sprite);
    }

    public void removeSprite(AbstractSprite sprite) {
        spritesToRemove.add(sprite);
    }

    public void update(double sec) {
        long now = System.currentTimeMillis();

        // updating current sprites
        for (AbstractSprite sprite : sprites) sprite.update(sec);

        // adding/removing sprites added/removed by current sprites
        sprites.addAll(spritesToAdd);
        spritesToAdd.clear();

        sprites.removeAll(spritesToRemove);
        spritesToRemove.clear();

        // adding auto generated sprites
        if (enemiesToGen > 0 && (now - lastEnemyGenTime) >= enemiesGenInterval) {
            EnemySprite enemy = new EnemySprite(this, 5, 1000);
            enemy.addGun(new ShooterSprite.Gun(enemy.getWidth() / 2, enemy.getHeight(), 0, 150));
            //enemy.setShooting(1000);
            enemy.setSpeedXY(0, 70);
            enemy.setXY(rand.nextInt(canvasWidth - (int) enemy.getWidth() - 1) + 1, 1);

            sprites.add(enemy);
            enemiesToGen--;
            lastEnemyGenTime = now;
        }
    }

    public void draw(Graphics2D g) {
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

}
