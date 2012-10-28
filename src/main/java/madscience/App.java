package madscience;

import java.awt.BorderLayout;
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

        frame.getContentPane().setLayout(new BorderLayout());

        GameCanvas canvas = new GameCanvas();
        canvas.setSize(new Dimension(480, 640));
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        frame.pack();

        canvas.setVisible(true);
        canvas.setFocusable(true);
        frame.pack();

        frame.setVisible(true);
    }

}
