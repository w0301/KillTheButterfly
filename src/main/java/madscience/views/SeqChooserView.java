package madscience.views;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import madscience.sprites.SeqElixirSprite;

/**
 *
 * @author Richard Kakaš
 */
public class SeqChooserView extends CanvasView {
    private static final Font INFO_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 25);
    private static final Color TEXT_COLOR = Color.GREEN;
    private static final Font TEXT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
    private static final float TEXT_TOP_MARGIN = 50;
    private static final float TEXT_LEFT_MARGIN = 20;

    private static final int MAX_BAD_TRIES = 3;
    private static final float SPRITES_MARGIN = 20;

    private Set<SeqChooserListener> seqListeners = new HashSet<SeqChooserListener>();

    private List<SeqElixirSprite> seqSpritesToView = new ArrayList<SeqElixirSprite>();
    private int hoverIndex = -1;

    private List<SeqElixirSprite> seqSpritesToCheck = null;
    private int nextSeqSprite = 0;
    private int badTriesLeft = MAX_BAD_TRIES;

    private int clickIndex = -1;

    public SeqChooserView(int width, int height) {
        super(width, height);

        SeqElixirSprite redSprite = new SeqElixirSprite(null, SeqElixirSprite.RED_IMG);
        seqSpritesToView.add(redSprite);

        SeqElixirSprite blueSprite = new SeqElixirSprite(null, SeqElixirSprite.BLUE_IMG);
        seqSpritesToView.add(blueSprite);

        SeqElixirSprite greenSprite = new SeqElixirSprite(null, SeqElixirSprite.GREEN_IMG);
        seqSpritesToView.add(greenSprite);

        SeqElixirSprite yellowSprite = new SeqElixirSprite(null, SeqElixirSprite.YELLOW_IMG);
        seqSpritesToView.add(yellowSprite);

        double spritesWidth = -SPRITES_MARGIN;
        double spritesHeight = 0;
        for (SeqElixirSprite sprite : seqSpritesToView) {
            spritesWidth += SPRITES_MARGIN + sprite.getWidth();
            spritesHeight = Math.max(sprite.getHeight(), spritesHeight);
        }

        double x = getWidth() / 2 - spritesWidth / 2;
        double y = getHeight() / 2;
        for (SeqElixirSprite sprite : seqSpritesToView) {
            sprite.setXY(x, y - sprite.getHeight() / 2);
            x += sprite.getWidth() + SPRITES_MARGIN;
        }
    }

    public void addSeqListener(SeqChooserListener l) {
        seqListeners.add(l);
    }

    public void removeSeqListener(SeqChooserListener l) {
        seqListeners.remove(l);
    }

    public void reset(List<SeqElixirSprite> checkList) {
        nextSeqSprite = 0;
        badTriesLeft = MAX_BAD_TRIES;
        seqSpritesToCheck = checkList;
    }

    public void checkClickedElixir() {
        if (clickIndex < 0 || clickIndex >= seqSpritesToView.size() || seqSpritesToCheck == null ||
            nextSeqSprite >= seqSpritesToCheck.size() || badTriesLeft <= 0) return;

        if (seqSpritesToView.get(clickIndex).compareTo(seqSpritesToCheck.get(nextSeqSprite)) == 0) {
            nextSeqSprite++;
            if (nextSeqSprite == seqSpritesToCheck.size()) {
                for (SeqChooserListener l : seqListeners) l.seqChooserEnded(this, true);
            }
        }
        else {
            badTriesLeft--;
            if (badTriesLeft == 0) {
                for (SeqChooserListener l : seqListeners) l.seqChooserEnded(this, false);
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        if (!isVisible()) return;

        g.clearRect(0, 0, getWidth(), getHeight());

        g.setFont(INFO_FONT);
        g.setColor(TEXT_COLOR);
        String badTriesStr = "Bad tries left: " + badTriesLeft;
        Rectangle2D bounds = TEXT_FONT.getStringBounds(badTriesStr, g.getFontRenderContext());
        g.drawString(badTriesStr, TEXT_LEFT_MARGIN, (float) bounds.getHeight() + TEXT_TOP_MARGIN);

        int elixirsLeft = 0;
        if (seqSpritesToCheck != null) elixirsLeft = seqSpritesToCheck.size() - nextSeqSprite;

        g.drawString("Elixirs left: " + elixirsLeft, TEXT_LEFT_MARGIN, TEXT_TOP_MARGIN + (float) (2*bounds.getHeight()));

        g.setFont(TEXT_FONT);
        g.drawString("Click on elixirs in the same order as they appeard on the wall:", 2*TEXT_LEFT_MARGIN, 3*TEXT_TOP_MARGIN + (float) (2*bounds.getHeight()));

        int i = 0;
        for (SeqElixirSprite sprite : seqSpritesToView) {
            if (hoverIndex == i) {
                if (clickIndex == hoverIndex) g.setColor(Color.RED);
                else g.setColor(Color.YELLOW);

                Stroke oldStroke = g.getStroke();
                int stroke = 2;
                g.setStroke(new BasicStroke(stroke));
                g.draw(new Rectangle2D.Double(sprite.getX() - stroke / 2, sprite.getY() - stroke / 2,
                                              sprite.getWidth() + stroke / 2, sprite.getHeight() + stroke / 2));
                g.setStroke(oldStroke);
            }
            sprite.draw(g);
            i++;
        }
    }

    @Override
    public void processMouse(int mouseX, int mouseY, Set<Integer> buttons) {
        if (!isVisible()) return;

        int i = 0;
        hoverIndex = -1;
        for (SeqElixirSprite sprite : seqSpritesToView) {
            if (sprite.contains(mouseX, mouseY)) hoverIndex = i;
            i++;
        }

        if (buttons.contains(MouseEvent.BUTTON1)) {
            clickIndex = hoverIndex;
        }
        else if (clickIndex != -1 && hoverIndex == clickIndex) {
            checkClickedElixir();
            clickIndex = -1;
        }
        else clickIndex = -1;
    }

}
