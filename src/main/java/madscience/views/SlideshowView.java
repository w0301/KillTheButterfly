package madscience.views;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import madscience.sprites.EnemySprite;

/**
 *
 * @author Richard Kakaš
 */
public class SlideshowView extends CanvasView {
    public static final BufferedImage[] SLIDESHOW_IMGS;
    public static final double[] SLIDESHOW_TIMES;
    public static final double DEFAULT_TIME = 2000;

    private static final Font SKIP_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    private static final double SKIP_MARGIN = 30;

    static {
        int count = 7;
        SLIDESHOW_IMGS = new BufferedImage[count];
        for (int i = 1; i <= count; i++) {
            SLIDESHOW_IMGS[i - 1] = null;
            try {
                SLIDESHOW_IMGS[i - 1] = ImageIO.read(EnemySprite.class.getResourceAsStream("/slideshow/slide" + i + ".png"));
            }
            catch (IOException e) { }
        }
        SLIDESHOW_TIMES = new double[] { 7000, 1200, 1200, 1200, 1200, 1200, 1200 };
    }

    private static class Page {
        public BufferedImage image;
        public double time;

        public Page(BufferedImage image, double time) {
            this.image = image;
            this.time = time;
        }

    }

    private Set<SlideshowListener> slideshowListeners = new HashSet<SlideshowListener>();

    private List<Page> pages = new ArrayList<Page>();
    private int currentPage = 0;
    private double currentTime = 0;
    private boolean canKeySkip = false;

    private boolean goToPage(int index) {
        if (index < pages.size()) {
            currentPage = index;
            currentTime = 0;
            return true;
        }
        currentPage = pages.size() - 1;
        for (SlideshowListener l : slideshowListeners) l.slideshowEnded(this);
        return false;
    }

    public SlideshowView(int width, int height) {
        super(width, height);
    }

    public SlideshowView(int width, int height, BufferedImage[] imgs, double[] times) {
        this(width, height);
        for (int i = 0; i < imgs.length; i++) {
            double newTime = DEFAULT_TIME;
            if (times != null && i < times.length) newTime = times[i];
            addPage(imgs[i], newTime);
        }
    }

    public void addSlideshowListener(SlideshowListener l) {
        slideshowListeners.add(l);
    }

    public void removeSlideshowListener(SlideshowListener l) {
        slideshowListeners.remove(l);
    }

    public int addPage(BufferedImage image, double time) {
        pages.add(new Page(image, time));
        return pages.size() - 1;
    }

    public void removePage(int index) {
        pages.remove(index);
    }

    public void reset() {
        currentPage = 0;
        currentTime = 0;
    }

    @Override
    public void setVisible(boolean val) {
        if (val == true) {
            reset();
            canKeySkip = false;
        }
        super.setVisible(val);
    }

    @Override
    public void update(double sec) {
        if (!isVisible() || currentPage >= pages.size()) return;

        currentTime += sec * 1000;
        if (currentTime >= pages.get(currentPage).time) goToPage(currentPage + 1);
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);

        if (!isVisible() || currentPage >= pages.size()) return;

        if (pages.get(currentPage).image != null)
            g.drawImage(pages.get(currentPage).image, null, 0, 0);

        String skipStr = "ESC - Skip";
        Rectangle2D skipBounds = SKIP_FONT.getStringBounds(skipStr, g.getFontRenderContext());

        g.setFont(SKIP_FONT);
        g.drawString(skipStr, (float) (getWidth() - skipBounds.getWidth() - SKIP_MARGIN), (float) SKIP_MARGIN);
    }

    @Override
    public void processKeys(Set<Integer> keys) {
        if (keys.contains(KeyEvent.VK_ESCAPE)) {
            if (canKeySkip) {
                goToPage(pages.size() - 1);
                canKeySkip = false;
            }
        }
        else canKeySkip = true;
    }

}
