package terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Main {

    private Main() {
    }

    static void main() throws IOException, InterruptedException {
        KeyHandler keyHandler = new KeyHandler();
        String[][] output = new StartingBoard().getStartingBoard();

        // Put the terminal into character-at-a-time mode so that Enter is not required.
        try (TerminalMode terminalMode = TerminalMode.enable()) {
            print(output, terminalMode.isInteractive());

            if (terminalMode.isInteractive()) {
                output = runInteractive(keyHandler, output, terminalMode.input());
            } else {
                System.out.println("Interactive input is unavailable in this console.");
                System.out.println("Type a key and press Enter. Use Ctrl+D to exit.");
                output = runLineBuffered(keyHandler, output, terminalMode.input());
            }
        }
    }

    private static String[][] runInteractive(KeyHandler keyHandler, String[][] output, InputStream input)
            throws IOException {
        int character;
        while ((character = input.read()) != -1) {
            if (character == 27) { // Escape
                break;
            }

            if (!Character.isISOControl(character)) {
                output = keyHandler.handle(output, String.valueOf((char) character));
                print(output, true);
            }
        }
        return output;
    }

    private static String[][] runLineBuffered(KeyHandler keyHandler, String[][] output, InputStream input)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (int index = 0; index < line.length(); index++) {
                    output = keyHandler.handle(output, String.valueOf(line.charAt(index)));
                    print(output, false);
                }
            }
        }
        return output;
    }

    private static void print(String[][] output, boolean clearScreen) {
        if (clearScreen) {
            System.out.print("\033[2J\033[H"); // Clear the screen and move the cursor home.
        }
        String horizontalBorder = "+" + "-".repeat(32) + "+";
        System.out.println(horizontalBorder);
        for (String[] row : output) {
            System.out.println("|" + String.join("", row) + "|");
        }
        System.out.println(horizontalBorder);
        System.out.flush();
    }

    private static final class TerminalMode implements AutoCloseable {
        private final boolean interactive;
        private final String previousSettings;

        private TerminalMode(boolean interactive, String previousSettings) {
            this.interactive = interactive;
            this.previousSettings = previousSettings;
        }

        static TerminalMode enable() throws IOException, InterruptedException {
            String previousSettings;
            try {
                previousSettings = runStty("-g");
            } catch (IOException exception) {
                return new TerminalMode(false, null);
            }
            if (previousSettings.isBlank()) {
                return new TerminalMode(false, null);
            }

            try {
                // Keep input character-at-a-time, but preserve normal terminal line breaks.
                runStty("raw", "-echo", "opost", "onlcr");
            } catch (IOException exception) {
                return new TerminalMode(false, null);
            }
            return new TerminalMode(true, previousSettings);
        }

        InputStream input() {
            return System.in;
        }

        boolean isInteractive() {
            return interactive;
        }

        @Override
        public void close() throws IOException, InterruptedException {
            if (interactive) {
                runStty(previousSettings);
            }
        }

        private static String runStty(String... arguments) throws IOException, InterruptedException {
            String[] command = new String[arguments.length + 1];
            command[0] = "stty";
            System.arraycopy(arguments, 0, command, 1, arguments.length);

            Process process = new ProcessBuilder(command)
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.US_ASCII);
            if (process.waitFor() != 0) {
                throw new IOException("Could not change terminal mode");
            }
            return output.trim();
        }
    }
}
