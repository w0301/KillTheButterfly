package madscience.views;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Richard Kakaš
 */
public class SlideshowView extends CanvasView {

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

    public SlideshowView(int width, int height) {
        super(width, height);
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
    public void update(double sec) {
        if (!isVisible() || currentPage >= pages.size()) return;

        currentTime += sec * 1000;
        if (currentTime >= pages.get(currentPage).time) {
            currentPage++;
            currentTime = 0;
        }
        if (currentPage >= pages.size()) {
            currentPage--;
            for (SlideshowListener l : slideshowListeners)
                l.slideshowEnded(this);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible() || currentPage >= pages.size()) return;

        if (pages.get(currentPage).image != null)
            g.drawImage(pages.get(currentPage).image, null, 0, 0);
    }

}
