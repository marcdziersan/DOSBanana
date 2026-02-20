package dosbanana;

import javax.swing.*;

public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("DOS Banana");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setResizable(false);

            GamePanel panel = new GamePanel();
            f.setContentPane(panel);

            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            panel.start();
        });
    }
}
