package madscience.views;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
import javax.imageio.ImageIO;
import madscience.sprites.AbstractSprite;
import madscience.sprites.BossSprite;
import madscience.sprites.BulletSprite;
import madscience.sprites.ElixirSprite;
import madscience.sprites.EnemySprite;
import madscience.sprites.PlayerSprite;
import madscience.sprites.SeqElixirSprite;
import madscience.sprites.ShooterSprite;

/**
 *
 * @author Richard Kakaš
 */
public final class GameView extends CanvasView {
    private static final double DEFAULT_ENDED__DELAY = 500;
    private static final BufferedImage BACKGROUND_BLOCK_IMG;
    private static final Random RAND = new Random();

    static {
        BufferedImage bgBlockImg = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bgBlockImg.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bgBlockImg.getWidth(), bgBlockImg.getHeight());
        g.setColor(Color.GRAY);
        g.drawRect(3, 3, bgBlockImg.getWidth() - 3, bgBlockImg.getHeight() - 3);

        try {
            bgBlockImg = ImageIO.read(ElixirSprite.class.getResourceAsStream("/background/background_block.png"));
        }
        catch (IOException ex) { }
        finally {
            BACKGROUND_BLOCK_IMG = bgBlockImg;
        }
    }

    // borders constants
    public enum Border {
        TOP_BORDER, TOP_BORDER_CROSSED,
        BOTTOM_BORDER, BOTTOM_BORDER_CROSSED,
        LEFT_BORDER, LEFT_BORDER_CROSSED,
        RIGHT_BORDER, RIGHT_BORDER_CROSSED
    }

    private Set<GameViewListener> gameListeners = new HashSet<GameViewListener>();

    private double backgroundOffset = 0;
    private double wallHeight = 100;

    // game info
    private int playerScore = 0;
    private boolean paused = false;
    private boolean canKeyPause = false;

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

    private int lastElixirsCount = 0;
    private double elixirsGenInterval = 5000;
    private double tillElixirGen = elixirsGenInterval;
    private List<SpritePossibility> possibleElixirs = new ArrayList<SpritePossibility>();

    private List<SeqElixirSprite> seqElixirsToAdd = new LinkedList<SeqElixirSprite>();
    private int nextSeqElixir = 0;
    private int seqElixirAfterEnemies = 0;
    private int nextSeqElixirAfterEnemies = 0;

    // pixels / second
    private double gameSpeed = 100;
    private double origGameSpeed = gameSpeed;
    private double playerSetSpeed = 275;
    private double playerBulletSpeed = 200;

    private double speedRatio = 1;
    private double tillUnsetSpeedRatio = -1;
    private double endedDelay = DEFAULT_ENDED__DELAY;

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
        double randomProbability = RAND.nextDouble();
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
        BufferedImage bgImg = new BufferedImage(BACKGROUND_BLOCK_IMG.getWidth() * bgXCount,
                                                BACKGROUND_BLOCK_IMG.getHeight() * bgYCount, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bgImg.createGraphics();
        g.setColor(new Color(200, 200, 225));
        g.fill(new Rectangle2D.Double(0, 0, bgImg.getWidth(), wallHeight));
        for (int y = 0; y < bgYCount; y++) {
            for (int x = 0; x < bgXCount; x++) {
                g.drawImage(BACKGROUND_BLOCK_IMG, null, x * BACKGROUND_BLOCK_IMG.getWidth(),
                                                        (int) wallHeight + y * BACKGROUND_BLOCK_IMG.getHeight());
            }
        }
        setBackground(bgImg);

        // sprites logics
        playerSprite = new PlayerSprite(this);
        playerSprite.setXY(playerSprite.getWidth() * 0.5,
                           getHeight() / 2 - playerSprite.getHeight() / 2);
        playerSprite.addGun(new ShooterSprite.Gun(playerSprite.getWidth(), playerSprite.getHeight() / 2,
                                                  playerBulletSpeed, 0, BulletSprite.PLAYER_BULLET_IMG));
        playerSprite.setShootingInterval(playerShootingInterval);
        refreshPlayerView();

        sprites = new LinkedList<AbstractSprite>();
        sprites.add(playerSprite);

        spritesToAdd = new ArrayList<AbstractSprite>();
        spritesToRemove = new ArrayList<AbstractSprite>();
    }

    @Override
    public void setVisible(boolean val) {
        if (val != isVisible()) {
            canKeyPause = false;
        }
        super.setVisible(val);
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
        if (!playerSprite.hasShield()) playerSprite.setDefaultView(PlayerSprite.PLAYER_VIEW_1);
        else playerSprite.setDefaultView(PlayerSprite.PLAYER_SHIELDED_VIEW_1);
        playerSprite.clearAnimation();
        if (gameSpeed != 0) {
            if (!playerSprite.hasShield()) {
                playerSprite.addAnimationView(PlayerSprite.PLAYER_VIEW_2);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_VIEW_1);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_VIEW_3);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_VIEW_1);
            }
            else {
                playerSprite.addAnimationView(PlayerSprite.PLAYER_SHIELDED_VIEW_2);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_SHIELDED_VIEW_1);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_SHIELDED_VIEW_3);
                playerSprite.addAnimationView(PlayerSprite.PLAYER_SHIELDED_VIEW_1);
            }
            playerSprite.runAnimation(playerAnimInterval);
        }
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

    public void createSeqElixirs(int count) {
        for (int i = 0; i < count; i++) {
            SeqElixirSprite sprite;
            int num = RAND.nextInt(4);
            switch (num) {
                case 0:
                    sprite = new SeqElixirSprite(this, SeqElixirSprite.RED_IMG);
                    break;
                case 1:
                    sprite = new SeqElixirSprite(this, SeqElixirSprite.BLUE_IMG);
                    break;
                case 2:
                    sprite = new SeqElixirSprite(this, SeqElixirSprite.GREEN_IMG);
                    break;
                default:
                    sprite = new SeqElixirSprite(this, SeqElixirSprite.YELLOW_IMG);
                    break;
            }
            seqElixirsToAdd.add(sprite);
        }
        nextSeqElixir = 0;
        seqElixirAfterEnemies = enemiesToGen / count;
        nextSeqElixirAfterEnemies = seqElixirAfterEnemies;
    }

    public List<SeqElixirSprite> getSeqElixirs() {
        return seqElixirsToAdd;
    }

    @Override
    public void update(double sec) {
        if (isPaused() || !isVisible()) return;
        if (isEnded()) {
            endedDelay -= sec * 1000;
            if (endedDelay <= 0) {
                for (GameViewListener l : gameListeners)
                    l.gameEnded(this, isWon());
            }
            return;
        }

        // updating background
        backgroundOffset -= origGameSpeed * sec * speedRatio;
        if (Math.abs(backgroundOffset) >= BACKGROUND_BLOCK_IMG.getWidth())
            backgroundOffset = 0;

        // updating current sprites
        if (tillUnsetSpeedRatio > 0) tillUnsetSpeedRatio -= sec * 1000;
        else if (speedRatio != 1) {
            speedRatio = 1;
            setGameSpeed(origGameSpeed);
        }

        lastEnemiesCount = 0;
        lastElixirsCount = 0;
        for (AbstractSprite sprite : sprites) {
            sprite.update(sec * speedRatio);
            if (sprite instanceof EnemySprite) lastEnemiesCount++;
            if (sprite instanceof ElixirSprite || sprite instanceof SeqElixirSprite) lastElixirsCount++;
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
            int toGen = RAND.nextInt(maxEnemiesToGen - minEnemiesToGen + 1) + minEnemiesToGen;
            toGen = Math.min(toGen, enemiesToGen);
            for (int i = 0; i < toGen; i++) {
                AbstractSprite sprite = pickPossibleSprite(possibleEnemies);

                if (sprite != null && sprite instanceof EnemySprite) {
                    EnemySprite enemy = (EnemySprite) sprite;
                    int minY = Math.max(0, (int) wallHeight - (int) enemy.getHeight());
                    enemy.setXY(getWidth(), RAND.nextInt(getHeight() - (int) enemy.getHeight() - minY) + minY - 1);
                    enemy.setSpeedXY(-origGameSpeed, 0);

                    addSprite(enemy);
                }
            }
            enemiesToGen -= toGen;
            nextSeqElixirAfterEnemies -= toGen;
            tillEnemyGen = enemiesGenInterval;
        }
        else if (enemiesToGen == 0 && tillEnemyGen <= 0 && bossSprite != null && !bossAdded && lastEnemiesCount == 0 && lastElixirsCount == 0) {
            bossSprite.setXY(getWidth(), getHeight() - bossSprite.getHeight() - 1);
            bossSprite.setOscillation(getHeight() - bossSprite.getHeight(), 15);
            bossSprite.setSpeedXY(-origGameSpeed, 0);

            addSprite(bossSprite);
            bossAdded = true;
        }

        // generating elixirs for sequence
        if (nextSeqElixirAfterEnemies <= 0 && nextSeqElixir < seqElixirsToAdd.size()) {
            SeqElixirSprite seqElixir = (SeqElixirSprite) seqElixirsToAdd.get(nextSeqElixir++).clone();
            seqElixir.setXY(getWidth(), wallHeight / 2 - seqElixir.getHeight() / 2);
            seqElixir.setSpeedXY(-origGameSpeed, 0);

            addSprite(seqElixir);
            nextSeqElixirAfterEnemies = seqElixirAfterEnemies - Math.abs(nextSeqElixirAfterEnemies);
        }

        // generation elixirs
        tillElixirGen -= sec * 1000;
        if (tillElixirGen <= 0 && !bossAdded && lastEnemiesCount > 0) {
            AbstractSprite sprite = pickPossibleSprite(possibleElixirs);

            if (sprite != null && sprite instanceof ElixirSprite) {
                ElixirSprite elixir = (ElixirSprite) sprite;
                int minY = (int) wallHeight;
                elixir.setXY(getWidth(), RAND.nextInt(getHeight() - (int) elixir.getHeight() - minY) + minY - 1);
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
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible()) return;

        // drawing background
        AffineTransform bgAf = new AffineTransform();
        bgAf.translate(backgroundOffset, 0);
        g.drawImage(getBackground(), bgAf, null);

        // drawing sprite
        for (AbstractSprite sprite : sprites) sprite.draw(g);
    }

    @Override
    public void processKeys(Set<Integer> keys) {
        if (isEnded() || !isVisible()) return;

        if (keys.contains(KeyEvent.VK_ESCAPE) || keys.contains(KeyEvent.VK_PAUSE)) {
            if (canKeyPause) {
                setPaused(!isPaused());
                canKeyPause = false;
            }
        }
        else canKeyPause = true;

        if (isPaused()) return;

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
