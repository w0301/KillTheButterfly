package madscience.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 *
 * @author Richard Kakaš
 */
public class MenuView extends CanvasView {
    public static final BufferedImage MAIN_MENU_IMG;
    public static final BufferedImage MAIN_MENU_IMG2;
    public static final BufferedImage GAME_OVER_IMG;
    public static final BufferedImage GAME_WON_IMG;

    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 25);
    private static final Color TITLE_COLOR = Color.RED;

    private static final Font ITEM_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 20);
    private static final Color ITEM_COLOR = Color.GRAY;
    private static final Color ITEM_SELECTED_COLOR = Color.YELLOW;
    private static final Color ITEM_CLICKED_COLOR = Color.RED;

    static {
        BufferedImage mainMenuImg = null;
        BufferedImage mainMenu2Img = null;
        BufferedImage gameOverImg = null;
        BufferedImage gameWonImg = null;
        try {
            mainMenuImg = ImageIO.read(MenuView.class.getResourceAsStream("/menu/main_menu.png"));
            mainMenu2Img = ImageIO.read(MenuView.class.getResourceAsStream("/menu/main_menu2.png"));
            gameOverImg = ImageIO.read(MenuView.class.getResourceAsStream("/menu/game_over.png"));
            gameWonImg = ImageIO.read(MenuView.class.getResourceAsStream("/menu/game_won.png"));
        }
        catch (IOException ex) { }
        finally {
            MAIN_MENU_IMG = mainMenuImg;
            MAIN_MENU_IMG2 = mainMenu2Img;
            GAME_OVER_IMG = gameOverImg;
            GAME_WON_IMG = gameWonImg;
        }
    }

    public static interface Action {
        public void doAction(MenuView sender);
    }

    public static class Item {
        public String text;
        public Action action;
        public Rectangle2D bounds = null;

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

    private int hoverIndex = -1;
    private int clickIndex = -1;

    private int currentItem = 0;
    private int lastItem = -1;
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

    public Item getLastItem() {
        if (lastItem != -1) return items.get(lastItem);
        return null;
    }

    @Override
    public void setVisible(boolean val) {
        if (val) {
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
            items.get(i).bounds = ITEM_FONT.getStringBounds(items.get(i).text, g.getFontRenderContext());
            menuHeight += items.get(i).bounds.getHeight();
            //menuWidth = Math.max(menuWidth, bounds.getWidth());
        }

        double menuStartY = getHeight() / 2 - menuHeight / 2;

        g.setFont(TITLE_FONT);
        g.setColor(TITLE_COLOR);
        g.drawString(title, getWidth() / 2 - (float) titleBounds.getWidth() / 2,
                            (float) menuStartY);

        for (int i = 0; i < items.size(); i++) {
            Rectangle2D bounds = items.get(i).bounds;
            menuStartY += bounds.getHeight();

            g.setFont(ITEM_FONT);
            double rectYAdd = bounds.getHeight() - 5;
            bounds.setRect(getWidth() / 2 - bounds.getWidth() / 2, menuStartY - rectYAdd, bounds.getWidth() + 1, bounds.getHeight());

            if (hoverIndex == i || (hoverIndex == -1 && currentItem == i)) {
                if (clickIndex == currentItem) g.setColor(ITEM_CLICKED_COLOR);
                else g.setColor(ITEM_SELECTED_COLOR);
                g.draw(items.get(i).bounds);
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
                lastItem = currentItem;
                items.get(currentItem).action.doAction(this);
                canDoAction = false;
            }
        }
        else canDoAction = true;
    }

    @Override
    public void processMouse(int mouseX, int mouseY, Set<Integer> buttons) {
        if (!isVisible()) return;

        int i = 0;
        hoverIndex = -1;
        for (Item item : items) {
            if (item.bounds != null && item.bounds.contains(mouseX, mouseY) && item.isSelectable())
                currentItem = hoverIndex = i;
            i++;
        }

        if (buttons.contains(MouseEvent.BUTTON1)) {
            clickIndex = hoverIndex;
        }
        else if (clickIndex != -1 && hoverIndex == clickIndex) {
            if (items.get(clickIndex).isSelectable()) {
                lastItem = clickIndex;
                items.get(clickIndex).action.doAction(this);
            }
            clickIndex = -1;
        }
        else clickIndex = -1;
    }

}
