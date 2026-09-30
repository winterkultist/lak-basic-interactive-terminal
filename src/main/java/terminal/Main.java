package terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        KeyHandler keyHandler = new KeyHandler();
        String[][] output = new StartingBoard().getStartingBoard();

        // Put the terminal into character-at-a-time mode so that Enter is not required.
        TerminalMode terminalMode = TerminalMode.enable();
        try {
            print(output);

            int character;
            while ((character = terminalMode.input().read()) != -1) {
                if (character == 27) { // Escape
                    break;
                }

                if (character >= 'a' && character <= 'z') {
                    output = keyHandler.handle(output, String.valueOf((char) character));
                    print(output);
                }
            }
        } finally {
            terminalMode.close();
        }
    }

    private static void print(String[][] output) {
        System.out.print("\033[H\033[2J"); // Clear the screen and move the cursor home.
        String horizontalBorder = "+" + "-".repeat(32) + "+";
        System.out.println(horizontalBorder);
        for (String[] row : output) {
            System.out.println("|" + String.join("", row) + "|");
        }
        System.out.println(horizontalBorder);
        System.out.flush();
    }

    private static final class TerminalMode implements AutoCloseable {
        private final Process process;

        private TerminalMode(Process process) {
            this.process = process;
        }

        static TerminalMode enable() throws IOException, InterruptedException {
            Process process = new ProcessBuilder(
                    "/bin/sh", "-c", "stty -f /dev/tty raw -echo")
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (process.waitFor() != 0) {
                // IDE consoles commonly do not expose /dev/tty. In that case, keep
                // the normal buffered input behavior instead of failing at startup.
                return new TerminalMode(null);
            }
            return new TerminalMode(process);
        }

        InputStream input() throws IOException {
            return process == null ? System.in : new FileInputStream("/dev/tty");
        }

        @Override
        public void close() throws IOException, InterruptedException {
            if (process == null) {
                return;
            }
            Process restore = new ProcessBuilder("/bin/sh", "-c", "stty -f /dev/tty sane")
                    .inheritIO()
                    .start();
            if (restore.waitFor() != 0) {
                throw new IOException("Could not restore terminal mode");
            }
        }
    }
}
