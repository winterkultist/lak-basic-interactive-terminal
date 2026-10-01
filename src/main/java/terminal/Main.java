package terminal;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
    }

    static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createWindow);
    }

    private static void createWindow() {
        JFrame window = new JFrame("Interactive Board");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setContentPane(new BoardPanel(new KeyHandler(), new StartingBoard().getStartingBoard()));
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
