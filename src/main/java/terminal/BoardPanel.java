package terminal;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public final class BoardPanel extends JPanel {

    private static final int CELL_SIZE = 24;
    private static final Font CELL_FONT = new Font(Font.MONOSPACED, Font.BOLD, 16);

    private final KeyHandler keyHandler;
    private String[][] board;

    public BoardPanel(KeyHandler keyHandler, String[][] startingBoard) {
        this.keyHandler = keyHandler;
        this.board = startingBoard;
        setBackground(Color.WHITE);
        setFocusable(true);
        installKeyBindings();
    }

    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        return new Dimension(
                board[0].length * CELL_SIZE + insets.left + insets.right + 1,
                board.length * CELL_SIZE + insets.top + insets.bottom + 1);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Insets insets = getInsets();
            int left = insets.left;
            int top = insets.top;

            canvas.setColor(Color.BLACK);
            canvas.setFont(CELL_FONT);
            FontMetrics fontMetrics = canvas.getFontMetrics();

            for (int row = 0; row < board.length; row++) {
                for (int column = 0; column < board[row].length; column++) {
                    int x = left + column * CELL_SIZE;
                    int y = top + row * CELL_SIZE;
                    canvas.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                    String cell = board[row][column];
                    if (cell != null && !cell.isEmpty() && !cell.equals(" ")) {
                        int textWidth = fontMetrics.stringWidth(cell);
                        int textX = x + (CELL_SIZE - textWidth) / 2;
                        int textY = y + (CELL_SIZE - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
                        canvas.drawString(cell, textX, textY);
                    }
                }
            }
        } finally {
            canvas.dispose();
        }
    }

    private void installKeyBindings() {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        for (char key = 32; key <= 126; key++) {
            String actionName = "typed-key-" + (int) key;
            String keyValue = String.valueOf(key);
            inputMap.put(KeyStroke.getKeyStroke(key), actionName);
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    board = keyHandler.handle(board, keyValue);
                    repaint();
                }
            });
        }

        String escapeAction = "escape";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escapeAction);
        actionMap.put(escapeAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Window window = SwingUtilities.getWindowAncestor(BoardPanel.this);
                if (window != null) {
                    window.dispose();
                }
            }
        });
    }
}
