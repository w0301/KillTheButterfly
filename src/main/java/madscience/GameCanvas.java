package madscience;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.event.MouseInputListener;
import madscience.views.CanvasView;
import madscience.views.GameView;
import madscience.views.GameViewListener;
import madscience.views.IndicatorView;
import madscience.views.MenuView;
import madscience.views.SeqChooserListener;
import madscience.views.SeqChooserView;
import madscience.views.SlideshowListener;
import madscience.views.SlideshowView;

/**
 *
 * @author Richard Kakaš
 */
public final class GameCanvas extends Canvas implements Runnable, ComponentListener, KeyListener, MouseInputListener, GameViewListener, SeqChooserListener {
    public static final int MAX_LEVEL = 7;
    private static final int INDICATOR_HEIGHT = 25;
    private static final Runnable BACKGROUND_SOUND;

    private static Thread backgroundSoundThread = null;
    private static boolean backgroundSoundRunning = false;
    private static boolean backgroundSoundPaused = false;

    static {
        BACKGROUND_SOUND = new Runnable() {
            @Override
            public void run() {
                byte buffer[] = new byte[10000];
                try {
                    while (backgroundSoundRunning) {
                        URL soundFile = GameCanvas.class.getResource("/sounds/background.wav");
                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                        AudioFormat audioFormat = audioInputStream.getFormat();
                        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                        sourceDataLine.open(audioFormat);
                        sourceDataLine.start();

                        while (backgroundSoundRunning) {
                            while (backgroundSoundPaused) Thread.sleep(1);
                            int read = audioInputStream.read(buffer, 0, buffer.length);
                            if (read > 0) sourceDataLine.write(buffer, 0, read);
                            else break;
                        }
                        sourceDataLine.drain();
                        sourceDataLine.close();
                    }
                }
                catch (Exception e) { }
            }
        };
    }

    private static final int GAME_VIEW_ID = 1;
    private static final int INDICATOR_VIEW_ID = 2;
    private static final int NEW_GAME_MENU_VIEW_ID = 3;
    private static final int LEVEL_CLEARED_MENU_VIEW_ID = 4;
    private static final int GAME_LOST_MENU_VIEW_ID = 5;
    private static final int SEQUENCE_CHOOSER_VIEW_ID = 6;
    private static final int CHOOSER_LOST_MENU_VIEW_ID = 7;
    private static final int GAME_WON_MENU_VIEW_ID = 8;
    private static final int HIGHSCORE_TABLE_VIEW_ID = 9;
    private static final int GAME_PAUSED_MENU_VIEW_ID = 10;
    private static final int SLIDESHOW_VIEW_ID = 11;

    private BufferStrategy buffer;

    private Thread loopThread;
    private final Object loopLock = new Object();
    private boolean loopRunning = false;

    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private int lastMouseX = 0, lastMouseY = 0;
    private Set<Integer> pressedMouseButtons = new HashSet<Integer>();

    private Map<Integer, CanvasView> views = new ConcurrentHashMap<Integer, CanvasView>();
    private GameView game = null;

    private int nextGameLevel = 1;
    private int lastPlayerScore = 0;

    public GameCanvas() {
        setVisible(false);
        setIgnoreRepaint(true);
        addComponentListener(this);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public CanvasView putView(Integer id, CanvasView view) {
        views.put(id, view);
        return view;
    }

    public CanvasView getView(Integer id) {
        return views.get(id);
    }

    public int getGameWidth() {
        return getWidth();
    }

    public int getGameHeight() {
        return getHeight() - INDICATOR_HEIGHT;
    }

    public void startGame(GameView newGame) {
        synchronized (loopLock) {
            game = (GameView) putView(GAME_VIEW_ID, newGame);
            game.setVisible(true);
            game.setPaused(false);
            game.addGameListener(this);

            IndicatorView indicator = (IndicatorView) putView(INDICATOR_VIEW_ID, new IndicatorView(getWidth(), INDICATOR_HEIGHT, game));
            indicator.setVisible(true);
            indicator.setXY(0, 0);

            game.setXY(0, indicator.getHeight());
        }
    }

    public void startNextGame(boolean reset) {
        synchronized (loopLock) {
            int score = 0;
            if (game == null || reset) {
                nextGameLevel = 1;
                lastPlayerScore = 0;
            }
            else {
                score = game.getPlayerScore();
                lastPlayerScore = score;
            }
            GameView newGame = GameFactory.createGame(getGameWidth(), getGameHeight(), nextGameLevel++);
            newGame.addPlayerScore(score);
            startGame(newGame);
        }
    }

    public void resetCurrentGame() {
        synchronized (loopLock) {
            GameView newGame = GameFactory.createGame(getGameWidth(), getGameHeight(), nextGameLevel);
            newGame.addPlayerScore(lastPlayerScore);
            startGame(newGame);
        }
    }

    protected void update(double sec) {
        synchronized (loopLock) {
            for (CanvasView view : views.values()) {
                if (view == null) continue;
                view.processKeys(pressedKeys);
                view.processMouse(lastMouseX, lastMouseY, pressedMouseButtons);
                view.update(sec);
            }
        }
    }

    protected void draw(Graphics gAbs) {
        Graphics2D g = (Graphics2D) gAbs;

        // drawing canvas views
        synchronized (loopLock) {
            for (CanvasView view : views.values()) {
                if (view == null) continue;
                double viewX = view.getX();
                double viewY = view.getY();
                if (viewX == -1) viewX = getWidth() / 2 - view.getWidth() / 2;
                if (viewY == -1) viewY = getHeight() / 2 - view.getHeight() / 2;
                g.setClip(new Rectangle2D.Double(viewX, viewY, view.getWidth(), view.getHeight()));
                g.translate(viewX, viewY);
                view.draw(g);
                g.translate(-viewX, -viewY);
            }
        }
    }

    public static void startBackgroundSound() {
        backgroundSoundRunning = true;
        if (backgroundSoundThread == null) {
            backgroundSoundThread = new Thread(BACKGROUND_SOUND);
            backgroundSoundThread.start();
        }
    }

    public static void setBackgroundSoundPaused(boolean val) {
        backgroundSoundPaused = val;
    }

    public static void stopBackgroundSound() {
        if (backgroundSoundThread == null) return;
        boolean stopped = false;
        backgroundSoundRunning = false;
        while (!stopped) {
            try {
                backgroundSoundThread.join();
                backgroundSoundThread = null;
                stopped = true;
            }
            catch (InterruptedException e) { }
        }
    }

    @Override
    public void run() {
        final int TARGET_FPS = 60;
        final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

        long lastLoopTime = System.nanoTime();

        while (loopRunning) {
            long now = System.nanoTime();
            double delta = now - lastLoopTime;
            lastLoopTime = now;

            update(delta / 1000000000);

            Graphics g = null;
            try {
                g = buffer.getDrawGraphics();
                draw(g);
            }
            finally {
                if (g != null) g.dispose();
            }
            if (!buffer.contentsLost()) buffer.show();
            Toolkit.getDefaultToolkit().sync();

            try {
                Thread.sleep((lastLoopTime - System.nanoTime() + OPTIMAL_TIME) / 1000000);
            }
            catch(Exception e) { }
        }
    }

    @Override
    public void seqChooserEnded(SeqChooserView sender, boolean won) {
        sender.setVisible(false);

        CanvasView toView;
        if (won) {
            if (nextGameLevel > MAX_LEVEL) toView = getView(GAME_WON_MENU_VIEW_ID);
            else {
                toView = getView(LEVEL_CLEARED_MENU_VIEW_ID);
                if (toView != null && toView instanceof MenuView) {
                    ((MenuView) toView).setTitle("Level " + (nextGameLevel - 1) + " cleared");
                }
            }
        }
        else toView = getView(CHOOSER_LOST_MENU_VIEW_ID);
        if (toView != null) toView.setVisible(true);
    }

    @Override
    public void gameEnded(GameView sender, boolean won) {
        sender.setVisible(false);
        CanvasView indicator = getView(INDICATOR_VIEW_ID);
        if (indicator != null) indicator.setVisible(false);

        CanvasView toView;
        if (won) {
            toView = getView(SEQUENCE_CHOOSER_VIEW_ID);
            if (toView != null && toView instanceof SeqChooserView) {
                ((SeqChooserView) toView).reset(sender.getSeqElixirs());
            }
        }
        else toView = getView(GAME_LOST_MENU_VIEW_ID);
        if (toView != null) toView.setVisible(true);
    }

    @Override
    public void gamePaused(GameView sender) {
        sender.setVisible(false);
        CanvasView toView = getView(GAME_PAUSED_MENU_VIEW_ID);
        if (toView != null && toView instanceof MenuView) {
            ((MenuView) toView).setTitle("Paused - Level " + (nextGameLevel - 1));
            toView.setVisible(true);
        }
    }

    @Override
    public void gameUnpaused(GameView sender) {
        CanvasView toHide = getView(GAME_PAUSED_MENU_VIEW_ID);
        if (toHide != null) toHide.setVisible(false);
        sender.setVisible(true);
    }

    @Override
    public void componentResized(ComponentEvent ce) {
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
        startBackgroundSound();

        final MenuView.Action newGameAction = new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                sender.setVisible(false);
                startNextGame(true);
            }
        };

        final MenuView mainMenu = new MenuView(getWidth(), getHeight(), "Main menu");
        final MenuView highScoreMenu = new MenuView(getWidth(), getHeight(), "High scores");
        final SlideshowView newGameSlideshow = new SlideshowView(getWidth(), getHeight(), SlideshowView.SLIDESHOW_IMGS, SlideshowView.SLIDESHOW_TIMES);

        newGameSlideshow.addSlideshowListener(new SlideshowListener() {
            @Override
            public void slideshowEnded(SlideshowView sender) {
                sender.setVisible(false);
                mainMenu.setVisible(true);
            }
        });
        putView(SLIDESHOW_VIEW_ID, newGameSlideshow);

        mainMenu.addItem("New game", newGameAction);
        mainMenu.addItem("High scores", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                sender.setVisible(false);
                highScoreMenu.setVisible(true);
            }
        });
        mainMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(NEW_GAME_MENU_VIEW_ID, mainMenu);

        final MenuView.Action viewMainMenuAction = new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                sender.setVisible(false);
                mainMenu.setVisible(true);
            }
        };

        HighScoreTable.getInstance().setMenuView(highScoreMenu);
        highScoreMenu.addItem("<-- Back", viewMainMenuAction);
        putView(HIGHSCORE_TABLE_VIEW_ID, highScoreMenu);

        final MenuView levelClearedMenu = new MenuView(getWidth(), getHeight());
        levelClearedMenu.addItem("Next level", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                startNextGame(false);
                sender.setVisible(false);
            }
        });
        levelClearedMenu.addItem("Restart game", newGameAction);
        levelClearedMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(LEVEL_CLEARED_MENU_VIEW_ID, levelClearedMenu);

        final MenuView.Action toHighScoreAction = new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                HighScoreTable.getInstance().showAddEntryDialog(GameCanvas.this, game.getPlayerScore(), nextGameLevel - 1);
                sender.setVisible(false);
                mainMenu.setVisible(true);
            }
        };

        final MenuView gameLostMenu = new MenuView(getWidth(), getHeight(), "You lose");
        gameLostMenu.addItem("Add to high scores", toHighScoreAction);
        gameLostMenu.addItem("Restart game", newGameAction);
        gameLostMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(GAME_LOST_MENU_VIEW_ID, gameLostMenu);

        final MenuView chooserLostMenu = new MenuView(getWidth(), getHeight(), "Bad sequence");
        chooserLostMenu.addItem("Retry level", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                resetCurrentGame();
                sender.setVisible(false);
            }
        });
        chooserLostMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(CHOOSER_LOST_MENU_VIEW_ID, chooserLostMenu);

        final MenuView gameWonMenu = new MenuView(getWidth(), getHeight(), "Game won");
        gameWonMenu.addItem("Add to high scores", toHighScoreAction);
        gameWonMenu.addItem("Restart game", newGameAction);
        gameWonMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(GAME_WON_MENU_VIEW_ID, gameWonMenu);

        final MenuView gamePausedMenu = new MenuView(getWidth(), getHeight());
        gamePausedMenu.addItem("Unpause game", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                if (game == null) return;
                synchronized (loopLock) {
                    sender.setVisible(false);
                    game.setPaused(false);
                    game.setVisible(true);
                }
            }
        });
        gamePausedMenu.addItem("Restart game", newGameAction);
        gamePausedMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        putView(GAME_PAUSED_MENU_VIEW_ID, gamePausedMenu);

        final SeqChooserView seqChooser = new SeqChooserView(getWidth(), getHeight());
        seqChooser.addSeqListener(this);
        putView(SEQUENCE_CHOOSER_VIEW_ID, seqChooser);

        newGameSlideshow.setVisible(true);
        //seqChooser.setVisible(true);

        createBufferStrategy(2);
        buffer = getBufferStrategy();

        if (loopThread == null || loopThread.getState() == Thread.State.TERMINATED) {
            loopThread = new Thread(this);
            loopRunning = true;
            loopThread.start();
        }
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
        if (loopThread == null) return;
        loopRunning = false;
        boolean joined = false;
        while (!joined) {
            try {
                loopThread.join();
                joined = true;
            }
            catch (InterruptedException ex) { }
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        synchronized (loopLock) {
            pressedKeys.add(ke.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        synchronized (loopLock) {
            pressedKeys.remove(ke.getKeyCode());
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        synchronized (loopLock) {
            pressedMouseButtons.add(me.getButton());
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        synchronized (loopLock) {
            pressedMouseButtons.remove(me.getButton());
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
        mouseMoved(me);
    }

    @Override
    public void mouseMoved(MouseEvent me) {
        synchronized (loopLock) {
            lastMouseX = me.getX();
            lastMouseY = me.getY();
        }
    }

}
