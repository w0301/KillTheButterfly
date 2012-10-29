package madscience;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    // borders constants
    public static final int TOP_BORDER = 0x1;
    public static final int BOTTOM_BORDER = 0x2;
    public static final int LEFT_BORDER = 0x3;
    public static final int RIGHT_BORDER = 0x4;

    public enum Border {
        TOP_BORDER, BOTTOM_BORDER,
        LEFT_BORDER, RIGHT_BORDER
    }

    private Random rand = new Random();
    private int canvasWidth = 0, canvasHeight = 0;

    // game info
    private int playerScore = 0;
    private int playerLives = MAX_PLAYER_LIVES;

    // enemy generations
    private static class EnemyPossibility {
        public double probability;
        public EnemySprite enemy;

        public EnemyPossibility(double probability, EnemySprite enemy) {
            this.probability = probability;
            this.enemy = enemy;
        }
    }

    private int enemiesToGen = 5;
    private double enemiesGenInterval = 2000;
    private double tillEnemyGen = enemiesGenInterval;
    private List<EnemyPossibility> possibleEnemies = new ArrayList<EnemyPossibility>();

    // pixels / second
    private double playerSetSpeed = 175;
    private double playerBulletSpeed = playerSetSpeed + 50;

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
                           canvasHeight - playerSprite.getHeight() * 2);
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

    public EnumSet<Border> getBorders(AbstractSprite sprite) {
        EnumSet<Border> ret = EnumSet.noneOf(Border.class);

        if (sprite.getX() <= 0)
            ret.add(Border.LEFT_BORDER);
        else if (sprite.getX() + sprite.getWidth() >= getCanvasWidth())
            ret.add(Border.RIGHT_BORDER);
        if (sprite.getY() <= 0)
            ret.add(Border.TOP_BORDER);
        else if (sprite.getY() + sprite.getHeight() >= getCanvasHeight())
            ret.add(Border.BOTTOM_BORDER);

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

    public void addPossibleEnemy(double prob, EnemySprite enemy) {
        possibleEnemies.add(new EnemyPossibility(prob, enemy));
        Collections.sort(possibleEnemies, new Comparator<EnemyPossibility>() {
            @Override
            public int compare(EnemyPossibility t, EnemyPossibility t1) {
                return (int) (t.probability - t1.probability + 0.5);
            }

        });
    }

    public void setEnemiesToGen(int val) {
        enemiesToGen = val;
    }

    public void setEnemiesGenInterval(double val) {
        enemiesGenInterval = val;
        tillEnemyGen = val;
    }

    public void update(double sec) {
        // updating current sprites
        for (AbstractSprite sprite : sprites) sprite.update(sec);
        for (int i = 0; i < sprites.size(); i++) {
            for (int j = i + 1; j < sprites.size(); j++) {
                if (sprites.get(i).intersects(sprites.get(j))) {
                    sprites.get(i).performIntersection(sprites.get(j));
                    sprites.get(j).performIntersection(sprites.get(i));
                }
            }
        }

        // adding/removing sprites added/removed by current sprites
        sprites.addAll(spritesToAdd);
        spritesToAdd.clear();
        sprites.removeAll(spritesToRemove);
        spritesToRemove.clear();

        // adding auto generated enemies
        tillEnemyGen -= sec * 1000;
        if (enemiesToGen > 0 && tillEnemyGen <= 0) {
            EnemySprite enemy = null;

            double cumulativeProbability = 0.0;
            double randomProbability = rand.nextDouble();
            for (EnemyPossibility enPos : possibleEnemies) {
                cumulativeProbability += enPos.probability;
                if (randomProbability <= cumulativeProbability) {
                    enemy = (EnemySprite) enPos.enemy.clone();
                    break;
                }
            }
            if (enemy != null) {
                enemy.setXY(rand.nextInt(canvasWidth - (int) enemy.getWidth() - 1) + 1, 1);
                enemy.setDescendingTime(1000 + 400*rand.nextInt(10));
                sprites.add(enemy);
                enemiesToGen--;
                tillEnemyGen = enemiesGenInterval;
            }
        }
    }

    public void draw(Graphics2D g) {
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

}
