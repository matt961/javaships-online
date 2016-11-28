package protocol;

import java.io.PrintWriter;

/**
 * Created by matt on 24/11/16.
 */
public final class JavashipsProtocol {
        public static final String QUIT = "quit";
        public static final String ATTACK = "attack";
        public static final String SEEKING = "seek";
        public static final String FIRST = "first";
        public static final String MESSAGE = "msg";

        public static void sendMessage(PrintWriter sender, final String message) {
            sender.println(MESSAGE + "." + message);
        }

        public static void sendSeek(PrintWriter sender, int x, int y) {
            sender.println(SEEKING + "." + x + "." + y);
        }
}