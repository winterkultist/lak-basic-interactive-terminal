package terminal;

public class KeyHandler {

    /**
     * Handles the current 16x32 display state and returns the next display state.
     * Replace this logic with application behavior.
     */
    public String[][] handle(String[][] grid, String key) {
        grid[5][5] = key;
        return grid;
    }
}
