package madscience.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public class MenuView extends CanvasView {
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    private static final Color TITLE_COLOR = Color.GREEN;

    private static final Font ITEM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
    private static final Color ITEM_COLOR = Color.GRAY;
    private static final Color ITEM_SELECTED_COLOR = Color.YELLOW;

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
    private boolean canDoAction = false;
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

    public int addItem(int index, String text, Action action) {
        items.add(index, new Item(text, action));
        return index;
    }

    public void removeItem(int index) {
        items.remove(index);
    }

    public void removeAllNotSelectableItems() {
        List<Item> newItems = new ArrayList<Item>();
        for (Item item : items) {
            if (item.isSelectable()) newItems.add(item);
        }
        items = newItems;
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

        for (int i = currentItem - 1; i >= 0; i--) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }

        for (int i = items.size() - 1; i > currentItem; i--) {
            if (items.get(i).isSelectable()) {
                currentItem = i;
                return;
            }
        }
    }

    public void resetKeys() {
        canDoAction = true;
        canSelectNext = true;
        canSelectPrev = true;
    }

    @Override
    public void setVisible(boolean val) {
        if (isVisible() != val) {
            currentItem = 0;
            if (!items.get(currentItem).isSelectable()) selectNextItem();
            canDoAction = false;
        }
        super.setVisible(val);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

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
            double rectYAdd = bounds.getHeight() - 5;
            bounds.setRect(getWidth() / 2 - bounds.getWidth() / 2, menuStartY - rectYAdd, bounds.getWidth() + 1, bounds.getHeight());

            if (currentItem == i) {
                g.setColor(ITEM_SELECTED_COLOR);
                g.draw(bounds);
            }
            else g.setColor(ITEM_COLOR);
            g.drawString(items.get(i).text, (float) bounds.getX(), (float) (bounds.getY() + rectYAdd));
        }
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
