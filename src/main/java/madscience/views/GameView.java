package madscience.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import madscience.sprites.AbstractSprite;
import madscience.sprites.BossSprite;
import madscience.sprites.ElixirSprite;
import madscience.sprites.EnemySprite;
import madscience.sprites.PlayerSprite;
import madscience.sprites.ShooterSprite;

/*
 * TODO:
 *  - Menu for game canvas
 */

/**
 *
 * @author Richard Kakaš
 */
public final class GameView extends CanvasView {
    private static final BufferedImage BACKGROUND_BLOCK_IMG;

    static {
        BACKGROUND_BLOCK_IMG = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = BACKGROUND_BLOCK_IMG.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, BACKGROUND_BLOCK_IMG.getWidth(), BACKGROUND_BLOCK_IMG.getHeight());
        g.setColor(Color.GRAY);
        g.drawRect(3, 3, BACKGROUND_BLOCK_IMG.getWidth() - 3, BACKGROUND_BLOCK_IMG.getHeight() - 3);
    }

    // borders constants
    public enum Border {
        TOP_BORDER, TOP_BORDER_CROSSED,
        BOTTOM_BORDER, BOTTOM_BORDER_CROSSED,
        LEFT_BORDER, LEFT_BORDER_CROSSED,
        RIGHT_BORDER, RIGHT_BORDER_CROSSED
    }

    private Set<GameViewListener> gameListeners = new HashSet<GameViewListener>();
    private static Random rand = new Random();

    private BufferedImage backgroundImg;
    private double backgroundOffset = 0;

    // game info
    private int playerScore = 0;
    private boolean paused = false;

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
    private int minEnemiesToGen = 1, maxEnemiesToGen = 1;
    private int lastEnemiesCount = 0;
    private double enemiesGenInterval = 2000;
    private double tillEnemyGen = enemiesGenInterval;
    private List<SpritePossibility> possibleEnemies = new ArrayList<SpritePossibility>();

    private double elixirsGenInterval = 5000;
    private double tillElixirGen = elixirsGenInterval;
    private List<SpritePossibility> possibleElixirs = new ArrayList<SpritePossibility>();

    // pixels / second
    private double gameSpeed = 100;
    private double origGameSpeed = gameSpeed;
    private double playerSetSpeed = 175;
    private double playerBulletSpeed = 150;

    private double speedRatio = 1;
    private double tillUnsetSpeedRatio = -1;

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

    public GameView(int width, int height) {
        super(width, height);

        // background
        int bgXCount = (int) (((double) getWidth()) / ((double) BACKGROUND_BLOCK_IMG.getWidth()) + 1.5);
        int bgYCount = (int) (((double) getHeight()) / ((double) BACKGROUND_BLOCK_IMG.getHeight()) + 0.5);
        backgroundImg = new BufferedImage(BACKGROUND_BLOCK_IMG.getWidth() * bgXCount,
                                          BACKGROUND_BLOCK_IMG.getHeight() * bgYCount, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = backgroundImg.createGraphics();
        for (int y = 0; y < bgYCount; y++) {
            for (int x = 0; x < bgXCount; x++) {
                g.drawImage(BACKGROUND_BLOCK_IMG, null, x * BACKGROUND_BLOCK_IMG.getWidth(),
                                                        y * BACKGROUND_BLOCK_IMG.getHeight());
            }
        }

        // sprites logics
        playerSprite = new PlayerSprite(this);
        playerSprite.setXY(playerSprite.getWidth() * 1.25,
                           getHeight() / 2 - playerSprite.getHeight() / 2);
        playerSprite.addGun(new ShooterSprite.Gun(playerSprite.getWidth(), playerSprite.getHeight() / 2,
                                                  playerBulletSpeed, 0));
        playerSprite.setShootingInterval(playerShootingInterval);
        refreshPlayerView();

        sprites = new LinkedList<AbstractSprite>();
        sprites.add(playerSprite);

        spritesToAdd = new ArrayList<AbstractSprite>();
        spritesToRemove = new ArrayList<AbstractSprite>();
    }

    public void addGameListener(GameViewListener l) {
        gameListeners.add(l);
    }

    public void removeGameListener(GameViewListener l) {
        gameListeners.remove(l);
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            for (GameViewListener l : gameListeners) l.gamePaused(this);
        }
        else {
            for (GameViewListener l : gameListeners) l.gameUnpaused(this);
        }
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
        this.gameSpeed = this.origGameSpeed = gameSpeed;
        refreshPlayerView();
    }

    public double getSpeedRatio() {
        return speedRatio;
    }

    public void setSpeedRatio(double speedRatio, double time) {
        this.speedRatio = speedRatio;
        tillUnsetSpeedRatio = time;
        gameSpeed = origGameSpeed * speedRatio;
        refreshPlayerView();
    }

    public PlayerSprite getPlayerSprite() {
        return playerSprite;
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

    public double getPlayerSetSpeed() {
        return playerSetSpeed;
    }

    public void setPlayerSetSpeed(double playerSetSpeed) {
        this.playerSetSpeed = playerSetSpeed;
    }

    public void setPlayerMoving(boolean top, boolean down) {
        if (top == down) playerSprite.setSpeedXY(0, 0);
        else playerSprite.setSpeedXY(0, top ? -playerSetSpeed : playerSetSpeed);
    }

    public void setPlayerShooting(boolean val) {
        playerSprite.setShooting(val);
    }

    public int getBossMaxLives() {
        return bossMaxLives;
    }

    public BossSprite getBossSprite() {
        return bossSprite;
    }

    public void setBossSprite(BossSprite sprite) {
        bossSprite = sprite;
        if (sprite != null) bossMaxLives = sprite.getLives();
    }

    public EnumSet<Border> getBorders(AbstractSprite sprite) {
        EnumSet<Border> ret = EnumSet.noneOf(Border.class);

        if (sprite.getX() <= 0)
            ret.add(Border.LEFT_BORDER);
        else if (sprite.getX() + sprite.getWidth() >= getWidth())
            ret.add(Border.RIGHT_BORDER);
        if (sprite.getY() <= 0)
            ret.add(Border.TOP_BORDER);
        else if (sprite.getY() + sprite.getHeight() >= getHeight())
            ret.add(Border.BOTTOM_BORDER);

        if (sprite.getX() + sprite.getWidth() <= 0)
            ret.add(Border.LEFT_BORDER_CROSSED);
        else if (sprite.getX() >= getWidth())
            ret.add(Border.RIGHT_BORDER_CROSSED);
        if (sprite.getY() + sprite.getHeight() <= 0)
            ret.add(Border.TOP_BORDER_CROSSED);
        else if (sprite.getY() >= getHeight())
            ret.add(Border.BOTTOM_BORDER_CROSSED);

        return ret;
    }

    public boolean isLost() {
        return playerSprite.getLives() <= 0;
    }

    public boolean isWon() {
        return enemiesToGen == 0 && bossSprite == null && lastEnemiesCount == 0;
    }

    public boolean isEnded() {
        return isLost() || isWon();
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

    public void setEnemyGeneration(int count, double interval, int min, int max) {
        enemiesToGen = count;
        enemiesGenInterval = tillEnemyGen = interval;
        minEnemiesToGen = min;
        maxEnemiesToGen = max;
    }

    public void addPossibleElixir(double prob, ElixirSprite elixir) {
        addPossibleSprite(possibleElixirs, new SpritePossibility(prob, elixir));
    }

    public void setElixirGeneration(double interval) {
        elixirsGenInterval = tillElixirGen = interval;
    }

    @Override
    public void update(double sec) {
        if (isPaused() || isEnded() || !isVisible()) return;

        // updating background
        backgroundOffset -= getGameSpeed() * sec * speedRatio;
        if (Math.abs(backgroundOffset) >= BACKGROUND_BLOCK_IMG.getWidth())
        backgroundOffset = 0;

        // updating current sprites
        lastEnemiesCount = 0;
        if (tillUnsetSpeedRatio > 0) tillUnsetSpeedRatio -= sec * 1000;
        else if (speedRatio != 1) {
            speedRatio = 1;
            setGameSpeed(origGameSpeed);
        }
        for (AbstractSprite sprite : sprites) {
            sprite.update(sec * speedRatio);
            if (sprite instanceof EnemySprite) lastEnemiesCount++;
        }

        ListIterator<AbstractSprite> iter1 = sprites.listIterator();
        while (iter1.hasNext()) {
            AbstractSprite sprite1 = iter1.next();
            ListIterator<AbstractSprite> iter2 = sprites.listIterator(iter1.nextIndex());
            while (iter2.hasNext()) {
                AbstractSprite sprite2 = iter2.next();
                if (sprite1.intersects(sprite2) || sprite2.intersects(sprite1)) {
                    sprite1.performIntersection(sprite2);
                    sprite2.performIntersection(sprite1);
                }
            }
        }

        // adding auto generated enemies
        tillEnemyGen -= sec * 1000;
        if (enemiesToGen > 0 && tillEnemyGen <= 0) {
            int toGen = rand.nextInt(maxEnemiesToGen - minEnemiesToGen + 1) + minEnemiesToGen;
            toGen = Math.min(toGen, enemiesToGen);
            for (int i = 0; i < toGen; i++) {
                AbstractSprite sprite = pickPossibleSprite(possibleEnemies);

                if (sprite != null && sprite instanceof EnemySprite) {
                    EnemySprite enemy = (EnemySprite) sprite;
                    enemy.setXY(getWidth() - enemy.getWidth() - 1,
                                rand.nextInt(getHeight() - (int) enemy.getHeight() - 1) + 1);
                    enemy.setSpeedXY(-origGameSpeed, 0);

                    addSprite(enemy);
                }
            }
            enemiesToGen -= toGen;
            tillEnemyGen = enemiesGenInterval;
        }
        else if (enemiesToGen == 0 && tillEnemyGen <= 0 && bossSprite != null && !bossAdded && lastEnemiesCount == 0) {
            bossSprite.setXY(getWidth(), getHeight() / 2 - bossSprite.getHeight());
            bossSprite.setOscillation(bossSprite.getHeight() * 1.5, 5);
            bossSprite.setSpeedXY(-origGameSpeed, 0);

            addSprite(bossSprite);
            bossAdded = true;
        }

        // generation elixirs
        tillElixirGen -= sec * 1000;
        if (tillElixirGen <= 0 && !bossAdded) {
            AbstractSprite sprite = pickPossibleSprite(possibleElixirs);

            if (sprite != null && sprite instanceof ElixirSprite) {
                ElixirSprite elixir = (ElixirSprite) sprite;
                elixir.setXY(getWidth() - elixir.getWidth() - 1,
                             rand.nextInt(getHeight() - (int) elixir.getHeight() - 1) + 1);
                elixir.setSpeedXY(-origGameSpeed, 0);

                addSprite(elixir);
                tillElixirGen = elixirsGenInterval;
            }
        }

        // adding/removing sprites added/removed
        for (AbstractSprite sprite : spritesToAdd) {
            if (sprite instanceof ElixirSprite) sprites.add(0, sprite);
            else sprites.add(sprite);
            sprite.performAdded();
        }
        spritesToAdd.clear();

        for (AbstractSprite sprite : spritesToRemove) {
            sprites.remove(sprite);
            sprite.performRemoved();
        }
        spritesToRemove.clear();

        if (isEnded()) {
            for (GameViewListener l : gameListeners)
                l.gameEnded(this, isWon());
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible()) return;

        // drawing background
        AffineTransform bgAf = new AffineTransform();
        bgAf.translate(backgroundOffset, 0);
        g.drawImage(backgroundImg, bgAf, null);

        // drawing sprite
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

    @Override
    public void processKeys(Set<Integer> keys) {
        if (isPaused() || isEnded() || !isVisible()) return;

        boolean shoot = false, up = false, down = false;
        for (Integer key : keys) {
            switch (key) {
                case KeyEvent.VK_UP:
                    up = true;
                    break;
                case KeyEvent.VK_DOWN:
                    down = true;
                    break;
                case KeyEvent.VK_SPACE:
                    shoot = true;
                    break;
            }
        }
        setPlayerMoving(up, down);
        setPlayerShooting(shoot);
    }

}
