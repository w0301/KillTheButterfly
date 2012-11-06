package madscience.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import madscience.sprites.PlayerSprite;

/**
 *
 * @author Richard Kakaš
 */
public class IndicatorView extends CanvasView {
    private static final double LIFE_INDICATOR_WIDTH = 275;
    private static final double LIFE_INDICATOR_MARGIN = 6;

    private final GameView game;

    public IndicatorView(int width, int height, GameView game) {
        super(width, height);
        this.game = game;
    }

    @Override
    public void draw(Graphics2D g) {
        //if (!isVisible()) return;

        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // life indicator for player
        double onePlayerLifeWidth = LIFE_INDICATOR_WIDTH / PlayerSprite.MAX_LIVES;
        int currPlayerLives = (game == null) ? 0 : game.getPlayerSprite().getLives();
        g.setColor(Color.RED);
        g.fill(new Rectangle2D.Double(LIFE_INDICATOR_MARGIN, LIFE_INDICATOR_MARGIN,
                                      onePlayerLifeWidth * currPlayerLives,
                                      getHeight() - 2*LIFE_INDICATOR_MARGIN));

        // life indicator for boss
        int bossMaxLives = (game == null) ? 1 : game.getBossMaxLives();
        if (bossMaxLives == 0) bossMaxLives = 1;
        double oneBossLifeWidth = LIFE_INDICATOR_WIDTH / bossMaxLives;
        int currBossLives = (game == null || game.getBossSprite() == null) ? 0 :
                                    game.getBossSprite().getLives();
        g.setColor(Color.RED);
        g.fill(new Rectangle2D.Double(getWidth() - oneBossLifeWidth * currBossLives - 2*LIFE_INDICATOR_MARGIN,
                                      LIFE_INDICATOR_MARGIN,
                                      oneBossLifeWidth * currBossLives,
                                      getHeight() - 2*LIFE_INDICATOR_MARGIN));

        // score indicator
        String scoreStr = Integer.toString((game == null) ? 0 : game.getPlayerScore());
        g.setColor(Color.WHITE);
        g.setFont(new Font(null, 0, getHeight()));
        FontMetrics fontMetrics = g.getFontMetrics();
        g.drawString(scoreStr, getWidth() / 2.0f - fontMetrics.stringWidth(scoreStr) / 2,
                               9.0f*(getHeight()) / 10.0f);
    }

    @Override
    public void update(double sec) {
    }

    @Override
    public void processKeys(Set<Integer> keys) {
    }

}
