package madscience;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
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

    // borders constants
    public enum Border {
        TOP_BORDER, TOP_BORDER_CROSSED,
        BOTTOM_BORDER, BOTTOM_BORDER_CROSSED,
        LEFT_BORDER, LEFT_BORDER_CROSSED,
        RIGHT_BORDER, RIGHT_BORDER_CROSSED
    }

    private Random rand = new Random();
    private int canvasWidth = 0, canvasHeight = 0;

    // game info
    private int playerScore = 0;

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
    private double gameSpeed = 100;
    private double playerSetSpeed = 175;
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
        playerSprite.setXY(playerSprite.getWidth() * 0.5,
                           canvasHeight / 2 - playerSprite.getHeight() / 2);
        playerSprite.addGun(new ShooterSprite.Gun(playerSprite.getWidth(), playerSprite.getHeight() / 2,
                                                  playerBulletSpeed, 0));
        playerSprite.setShootingInterval(200);

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

    public void setPlayerShooting(boolean val) {
        playerSprite.setShooting(val);
    }

    public void togglePlayerShooting() {
        if (playerSprite.isShooting()) playerSprite.setShooting(false);
        else playerSprite.setShooting(true);
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

        if (sprite.getX() + sprite.getWidth() <= 0)
            ret.add(Border.LEFT_BORDER_CROSSED);
        else if (sprite.getX() >= getCanvasWidth())
            ret.add(Border.RIGHT_BORDER_CROSSED);
        if (sprite.getY() + sprite.getHeight() <= 0)
            ret.add(Border.TOP_BORDER_CROSSED);
        else if (sprite.getY() >= getCanvasHeight())
            ret.add(Border.BOTTOM_BORDER_CROSSED);

        return ret;
    }

    public boolean isLost() {
        return playerSprite.getLives() <= -1;
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
                if (sprites.get(i).intersects(sprites.get(j)) ||
                    sprites.get(j).intersects(sprites.get(i))) {
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
                enemy.setXY(canvasWidth - enemy.getWidth() - 1,
                            rand.nextInt(canvasHeight - (int) enemy.getHeight() - 1) + 1); // 30
                enemy.setSpeedXY(-gameSpeed, 0);

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
