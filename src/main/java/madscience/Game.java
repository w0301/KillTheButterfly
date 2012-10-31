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
import madscience.sprites.BossSprite;
import madscience.sprites.ElixirSprite;
import madscience.sprites.EnemySprite;
import madscience.sprites.PlayerSprite;
import madscience.sprites.ShooterSprite;

/*
 * TODO:
 *  - moving background
 *  - Menu for game canvas
 */

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

    private static Random rand = new Random();
    private int canvasWidth = 0, canvasHeight = 0;

    // game info
    private int playerScore = 0;

    // enemy and other sprites generations
    private static class SpritePossibility {
        public double probability;
        public AbstractSprite sprite;

        public SpritePossibility(double probability, AbstractSprite sprite) {
            this.probability = probability;
            this.sprite = sprite;
        }
    }

    private int enemiesToGen = 5;
    private int lastEnemiesCount = 0;
    private double enemiesGenInterval = 2000;
    private double tillEnemyGen = enemiesGenInterval;
    private List<SpritePossibility> possibleEnemies = new ArrayList<SpritePossibility>();

    private double elixirsGenInterval = 5000;
    private double tillElixirGen = elixirsGenInterval;
    private List<SpritePossibility> possibleElixirs = new ArrayList<SpritePossibility>();

    // pixels / second
    private double gameSpeed = 100;
    private double playerSetSpeed = 175;
    private double playerBulletSpeed = 150;

    // player options
    private double playerAnimInterval = 100;
    private long playerShootingInterval = 250;

    // sprites
    private List<AbstractSprite> sprites;
    private PlayerSprite playerSprite;

    private boolean bossAdded = false;
    private BossSprite bossSprite = null;
    private int bossMaxLives = 0;

    private List<AbstractSprite> spritesToAdd;
    private List<AbstractSprite> spritesToRemove;

    private static void addPossibleSprite(List<SpritePossibility> list, SpritePossibility spr) {
        list.add(spr);
        Collections.sort(list, new Comparator<SpritePossibility>() {
            @Override
            public int compare(SpritePossibility t, SpritePossibility t1) {
                return (int) (t.probability - t1.probability + 0.5);
            }

        });
    }

    private static AbstractSprite pickPossibleSprite(List<SpritePossibility> list) {
        double cumulativeProbability = 0.0;
        double randomProbability = rand.nextDouble();
        for (SpritePossibility spritePos : list) {
            cumulativeProbability += spritePos.probability;
            if (randomProbability <= cumulativeProbability) {
                return spritePos.sprite.clone();
            }
        }
        return null;
    }

    public Game(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;

        playerSprite = new PlayerSprite(this);
        playerSprite.setXY(playerSprite.getWidth() * 1.25,
                           canvasHeight / 2 - playerSprite.getHeight() / 2);
        playerSprite.addGun(new ShooterSprite.Gun(playerSprite.getWidth(), playerSprite.getHeight() / 2,
                                                  playerBulletSpeed, 0));
        playerSprite.setShootingInterval(playerShootingInterval);
        refreshPlayerView();

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

    public double getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(double gameSpeed) {
        this.gameSpeed = gameSpeed;
        refreshPlayerView();
    }

    public PlayerSprite getPlayerSprite() {
        return playerSprite;
    }

    public double getPlayerSetSpeed() {
        return playerSetSpeed;
    }

    public void refreshPlayerView() {
        playerSprite.setDefaultView(PlayerSprite.DEFAULT_VIEW);
        if (gameSpeed != 0) {
            playerSprite.addAnimationView(PlayerSprite.DEFAULT_VIEW);
            playerSprite.addAnimationView(PlayerSprite.DEFAULT_VIEW_1);
            playerSprite.runAnimation(playerAnimInterval);
        }
        else playerSprite.clearAnimation();
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

    public BossSprite getBossSprite() {
        return bossSprite;
    }

    public int getBossMaxLives() {
        return bossMaxLives;
    }

    public void setBossSprite(BossSprite sprite) {
        bossSprite = sprite;
        if (sprite != null) bossMaxLives = sprite.getLives();
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
        return playerSprite.getLives() <= 0;
    }

    public boolean isWon() {
        return enemiesToGen == 0 && bossSprite == null && lastEnemiesCount == 0;
    }

    public void addSprite(AbstractSprite sprite) {
        spritesToAdd.add(sprite);
    }

    public void removeSprite(AbstractSprite sprite) {
        spritesToRemove.add(sprite);
    }

    public void addPossibleEnemy(double prob, EnemySprite enemy) {
        addPossibleSprite(possibleEnemies, new SpritePossibility(prob, enemy));
    }

    public void setEnemyGeneration(int count, double interval) {
        enemiesToGen = count;
        enemiesGenInterval = tillEnemyGen = interval;
    }

    public void setEnemiesToGen(int val) {
        enemiesToGen = val;
    }

    public void setEnemiesGenInterval(double val) {
        enemiesGenInterval = val;
        tillEnemyGen = val;
    }

    public void addPossibleElixir(double prob, ElixirSprite elixir) {
        addPossibleSprite(possibleElixirs, new SpritePossibility(prob, elixir));
    }

    public void setElixirGeneration(double interval) {
        elixirsGenInterval = tillElixirGen = interval;
    }

    public void update(double sec) {
        // updating current sprites
        lastEnemiesCount = 0;
        for (AbstractSprite sprite : sprites) {
            sprite.update(sec);
            if (sprite instanceof EnemySprite) lastEnemiesCount++;
        }

        for (int i = 0; i < sprites.size(); i++) {
            for (int j = i + 1; j < sprites.size(); j++) {
                if (sprites.get(i).intersects(sprites.get(j)) ||
                    sprites.get(j).intersects(sprites.get(i))) {
                    sprites.get(i).performIntersection(sprites.get(j));
                    sprites.get(j).performIntersection(sprites.get(i));
                }
            }
        }

        // adding auto generated enemies
        tillEnemyGen -= sec * 1000;
        if (enemiesToGen > 0 && tillEnemyGen <= 0) {
            AbstractSprite sprite = pickPossibleSprite(possibleEnemies);

            if (sprite != null && sprite instanceof EnemySprite) {
                EnemySprite enemy = (EnemySprite) sprite;
                enemy.setXY(canvasWidth - enemy.getWidth() - 1,
                            rand.nextInt(canvasHeight - (int) enemy.getHeight() - 1) + 1);
                enemy.setSpeedXY(-gameSpeed, 0);

                addSprite(enemy);
                enemiesToGen--;
                tillEnemyGen = enemiesGenInterval;
            }
        }
        else if (enemiesToGen == 0 && tillEnemyGen <= 0 && bossSprite != null && !bossAdded && lastEnemiesCount == 0) {
            bossSprite.setXY(canvasWidth, canvasHeight / 2 - bossSprite.getHeight() / 2);
            bossSprite.setOscillation(canvasHeight / 2 - bossSprite.getHeight() / 2, 20);
            bossSprite.setSpeedXY(-gameSpeed, 0);

            addSprite(bossSprite);
            bossAdded = true;
            setGameSpeed(0);
        }

        // generation elixirs
        tillElixirGen -= sec * 1000;
        if (tillElixirGen <= 0) {
            AbstractSprite sprite = pickPossibleSprite(possibleElixirs);

            if (sprite != null && sprite instanceof ElixirSprite) {
                ElixirSprite elixir = (ElixirSprite) sprite;
                elixir.setXY(canvasWidth - elixir.getWidth() - 1,
                             rand.nextInt(canvasHeight - (int) elixir.getHeight() - 1) + 1);
                elixir.setSpeedXY(-gameSpeed, 0);

                addSprite(elixir);
                tillElixirGen = elixirsGenInterval;
            }
        }

        // adding/removing sprites added/removed
        for (AbstractSprite sprite : spritesToAdd) {
            sprites.add(sprite);
            sprite.performAdded();
        }
        spritesToAdd.clear();

        for (AbstractSprite sprite : spritesToRemove) {
            sprites.remove(sprite);
            sprite.performRemoved();
        }
        spritesToRemove.clear();
    }

    public void draw(Graphics2D g) {
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

}
