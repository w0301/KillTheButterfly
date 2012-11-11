package madscience.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import madscience.sprites.PlayerSprite;

/**
 *
 * @author Richard Kakaš
 */
public class IndicatorView extends CanvasView {
    private static final double LIFE_INDICATOR_WIDTH = 275;
    private static final double LIFE_INDICATOR_MARGIN = 6;
    private static final Font TEXT_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    private static final Font TEXT_FONT2 = new Font(Font.SANS_SERIF, Font.BOLD, 15);

    private final GameView game;

    public IndicatorView(int width, int height, GameView game) {
        super(width, height);
        this.game = game;
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible()) return;

        double borderPos = 3;
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // life indicator for player
        double onePlayerLifeWidth = LIFE_INDICATOR_WIDTH / PlayerSprite.MAX_LIVES;
        int currPlayerLives = (game == null) ? 0 : game.getPlayerSprite().getLives();

        g.setColor(Color.GREEN);
        Rectangle2D.Double playerLifeRect = new Rectangle2D.Double(LIFE_INDICATOR_MARGIN - borderPos, LIFE_INDICATOR_MARGIN - borderPos,
                                                                   onePlayerLifeWidth * PlayerSprite.MAX_LIVES + 2*borderPos,
                                                                   getHeight() - 2*LIFE_INDICATOR_MARGIN + 2*borderPos);
        g.draw(playerLifeRect);

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

        g.setColor(Color.GREEN);
        Rectangle2D.Double bossLifeRect = new Rectangle2D.Double(getWidth() - oneBossLifeWidth * bossMaxLives - LIFE_INDICATOR_MARGIN - borderPos,
                                                                 LIFE_INDICATOR_MARGIN - borderPos,
                                                                 oneBossLifeWidth * bossMaxLives + 2*borderPos,
                                                                 getHeight() - 2*LIFE_INDICATOR_MARGIN + 2*borderPos);
        g.draw(bossLifeRect);

        g.setColor(Color.RED);
        g.fill(new Rectangle2D.Double(getWidth() - oneBossLifeWidth * currBossLives - LIFE_INDICATOR_MARGIN,
                                      LIFE_INDICATOR_MARGIN, oneBossLifeWidth * currBossLives,
                                      getHeight() - 2*LIFE_INDICATOR_MARGIN));

        // score indicator and other text
        g.setColor(Color.WHITE);
        g.setFont(TEXT_FONT);

        String scoreStr = Integer.toString((game == null) ? 0 : game.getPlayerScore());
        Rectangle2D scoreStrBounds = TEXT_FONT.getStringBounds(scoreStr, g.getFontRenderContext());
        g.drawString(scoreStr, getWidth() / 2 - (float) scoreStrBounds.getWidth() / 2,
                               9*getHeight() / 10);

        g.setFont(TEXT_FONT2);

        String playerLivesStr = currPlayerLives + "/" + PlayerSprite.MAX_LIVES;
        Rectangle2D playerLivesStrBounds = TEXT_FONT2.getStringBounds(playerLivesStr, g.getFontRenderContext());
        g.drawString(playerLivesStr, (float) playerLifeRect.getX() + (float) playerLifeRect.getWidth() / 2 - (float) playerLivesStrBounds.getWidth() / 2,
                                     (float) playerLifeRect.getY() + 9*((float) playerLivesStrBounds.getHeight()) / 10);

        String bossLivesStr = currBossLives + "/" + bossMaxLives;
        Rectangle2D bossLivesStrBounds = TEXT_FONT2.getStringBounds(bossLivesStr, g.getFontRenderContext());
        g.drawString(bossLivesStr, (float) bossLifeRect.getX() + (float) bossLifeRect.getWidth() / 2 - (float) bossLivesStrBounds.getWidth() / 2,
                                   (float) bossLifeRect.getY() + 9*((float) bossLivesStrBounds.getHeight()) / 10);

    }

}
