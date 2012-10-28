package madscience;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 *
 * @author Richard Kakaš
 */
public class App {

    public static void main(String[] args) {
        JFrame frame = new JFrame("MadScience");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // adding game canvas to frame
        GameCanvas canvas = new GameCanvas();
        canvas.setSize(new Dimension(480, 640));
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

}
