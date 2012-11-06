package madscience.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public class MenuView extends CanvasView {
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    private static final Color TITLE_COLOR = Color.GREEN;
    private static final int TITLE_MARGIN = 5;

    private static final Font ITEM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
    private static final Color ITEM_COLOR = Color.GRAY;
    private static final Color ITEM_SELECTED_COLOR = Color.YELLOW;


    static {

    }

    public static interface Action {
        public void doAction(MenuView sender);
    }

    private static class Item {
        public String text;
        public Action action;

        public Item(String text, Action action) {
            this.text = text;
            this.action = action;
        }

        public boolean isSelectable() {
            return action != null;
        }
    }

    private String title = null;
    private List<Item> items = new ArrayList<Item>();

    private int currentItem = 0;
    private boolean canDoAction = true;
    private boolean canSelectNext = true;
    private boolean canSelectPrev = true;

    public MenuView(int width, int height) {
        super(width, height);
    }

    public MenuView(int width, int height, String title) {
        this(width, height);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int addItem(String text, Action action) {
        items.add(new Item(text, action));
        return items.size() - 1;
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public int getCurrentItem() {
        return currentItem;
    }

    public void selectNextItem() {
        if (currentItem < 0 || currentItem >= items.size()) return;

        for (int i = currentItem + 1; i < items.size(); i++) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }

        for (int i = 0; i < currentItem; i++) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }
    }

    public void selectPrevItem() {
        if (currentItem < 0 || currentItem >= items.size()) return;

        for (int i = 0; i < currentItem; i++) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }

        for (int i = currentItem + 1; i < items.size(); i++) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible()) return;

        Rectangle2D titleBounds = TITLE_FONT.getStringBounds(title, g.getFontRenderContext());

        //double menuWidth = titleBounds.getWidth();
        double menuHeight = titleBounds.getHeight();

        for (int i = 0; i < items.size(); i++) {
            Rectangle2D bounds = ITEM_FONT.getStringBounds(items.get(i).text, g.getFontRenderContext());
            menuHeight += bounds.getHeight();
            //menuWidth = Math.max(menuWidth, bounds.getWidth());
        }

        double menuStartY = getHeight() / 2 - menuHeight / 2;

        g.setFont(TITLE_FONT);
        g.setColor(TITLE_COLOR);
        g.drawString(title, getWidth() / 2 - (float) titleBounds.getWidth() / 2,
                            (float) menuStartY);

        for (int i = 0; i < items.size(); i++) {
            Rectangle2D bounds = ITEM_FONT.getStringBounds(items.get(i).text, g.getFontRenderContext());
            menuStartY += bounds.getHeight();

            g.setFont(ITEM_FONT);
            if (currentItem == i) g.setColor(ITEM_SELECTED_COLOR);
            else g.setColor(ITEM_COLOR);
            g.drawString(items.get(i).text, getWidth() / 2 - (float) bounds.getWidth() / 2,
                                            (float) menuStartY);
        }
    }

    @Override
    public void update(double sec) {
    }

    @Override
    public void processKeys(Set<Integer> keys) {
        if (!isVisible()) return;

        if (keys.contains(KeyEvent.VK_DOWN)) {
            if (canSelectNext) {
                selectNextItem();
                canSelectNext = false;
            }
        }
        else canSelectNext = true;

        if (keys.contains(KeyEvent.VK_UP)) {
            if (canSelectPrev) {
                selectPrevItem();
                canSelectPrev = false;
            }
        }
        else canSelectPrev = true;

        if (keys.contains(KeyEvent.VK_ENTER)) {
            if (canDoAction && items.get(currentItem).isSelectable()) {
                items.get(currentItem).action.doAction(this);
                canDoAction = false;
            }
        }
        else canDoAction = true;
    }

}
