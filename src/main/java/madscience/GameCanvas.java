package madscience;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.io.InputStream;
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
import madscience.views.CanvasView;
import madscience.views.GameView;
import madscience.views.GameViewListener;
import madscience.views.IndicatorView;
import madscience.views.MenuView;

/**
 *
 * @author Richard Kakaš
 */
public final class GameCanvas extends Canvas implements Runnable, ComponentListener, KeyListener, GameViewListener {
    private static final int INDICATOR_HEIGHT = 25;
    private static final Runnable BACKGROUND_SOUND;

    private static Thread backgroundSoundThread = null;
    private static boolean backgroundSoundRunning = false;

    static {
        BACKGROUND_SOUND = new Runnable() {
            @Override
            public void run() {
                byte buffer[] = new byte[10000];
                try {
                    URL soundFile = GameCanvas.class.getResource("/sounds/background.wav");
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
                    AudioFormat audioFormat = audioInputStream.getFormat();
                    DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);

                    SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                    sourceDataLine.open(audioFormat);
                    sourceDataLine.start();

                    audioInputStream.mark(Integer.MAX_VALUE);
                    while (backgroundSoundRunning) {
                        int read = audioInputStream.read(buffer, 0, buffer.length);
                        if (read > 0) sourceDataLine.write(buffer, 0, read);
                        else audioInputStream.reset();
                    }
                    sourceDataLine.drain();
                    sourceDataLine.close();
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

    private BufferStrategy buffer;

    private Thread loopThread;
    private final Object loopLock = new Object();
    private boolean loopRunning = false;

    private Set<Integer> pressedKeys = new HashSet<Integer>();
    private Map<Integer, CanvasView> views = new ConcurrentHashMap<Integer, CanvasView>();

    private GameView game = null;
    private int nextGameLevel = 1;

    public GameCanvas() {
        setVisible(false);
        setIgnoreRepaint(true);
        addComponentListener(this);
        addKeyListener(this);
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
            startBackgroundSound();
        }
    }

    public void startNextGame(boolean reset) {
        synchronized (loopLock) {
            int score = 0;
            if (game == null || reset) nextGameLevel = 1;
            else score = game.getPlayerScore();
            GameView newGame = GameFactory.createGame(getGameWidth(), getGameHeight(), nextGameLevel++);
            newGame.addPlayerScore(score);
            startGame(newGame);
        }
    }

    public void pauseGame(boolean val) {
        synchronized (loopLock) {
            game.setPaused(val);
            stopBackgroundSound();
        }
    }

    protected void update(double sec) {
        synchronized (loopLock) {
            for (CanvasView view : views.values()) {
                if (view == null) continue;
                view.processKeys(pressedKeys);
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
    public void componentResized(ComponentEvent ce) {
    }

    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
        MenuView mainMenu = (MenuView) putView(NEW_GAME_MENU_VIEW_ID, new MenuView(getWidth(), getHeight(), "Main menu"));
        mainMenu.addItem("New game", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                startNextGame(true);
                sender.setVisible(false);
            }
        });
        mainMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });
        mainMenu.setVisible(true);

        MenuView levelClearedMenu = (MenuView) putView(LEVEL_CLEARED_MENU_VIEW_ID, new MenuView(getWidth(), getHeight(), "Level cleared"));
        levelClearedMenu.addItem("Next level", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                startNextGame(false);
                sender.setVisible(false);
            }
        });
        levelClearedMenu.addItem("New game", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                startNextGame(true);
                sender.setVisible(false);
            }
        });
        levelClearedMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });

        MenuView gameLostMenu = (MenuView) putView(GAME_LOST_MENU_VIEW_ID, new MenuView(getWidth(), getHeight(), "You lose"));
        gameLostMenu.addItem("New game", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                startNextGame(true);
                sender.setVisible(false);
            }
        });
        gameLostMenu.addItem("Exit", new MenuView.Action() {
            @Override
            public void doAction(MenuView sender) {
                System.exit(0);
            }
        });

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
    public void gameEnded(GameView game, boolean won) {
        if (game != null) {
            game.setVisible(false);
            getView(INDICATOR_VIEW_ID).setVisible(false);
        }
        CanvasView menu;
        if (won) menu = getView(LEVEL_CLEARED_MENU_VIEW_ID);
        else menu = getView(GAME_LOST_MENU_VIEW_ID);
        if (menu != null) {
            menu.setVisible(true);
        }
        stopBackgroundSound();
    }

    @Override
    public void gamePaused(GameView game) {
    }

    @Override
    public void gameUnpaused(GameView game) {
    }

}
