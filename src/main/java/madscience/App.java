package madscience;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author Richard Kakaš
 */
public class App {

    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.equals("windows")) System.setProperty("sun.java2d.d3d", "true");
        else System.setProperty("sun.java2d.opengl", "true");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Kill the Butterfly");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                // adding game canvas to frame
                GameCanvas canvas = new GameCanvas();
                canvas.setSize(new Dimension(800, 600));
                frame.add(canvas);
                frame.pack();

                canvas.setVisible(true);
                canvas.setFocusable(true);
                frame.pack();

                // centering frame on screen
                frame.setLocationRelativeTo(null);

                // showing frame to user
                frame.setVisible(true);
                frame.setResizable(false);
            }
        });
    }

}
