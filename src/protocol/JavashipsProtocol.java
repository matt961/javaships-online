package protocol;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by matt on 24/11/16.
 *
 * {@link JavashipsProtocol} is used for the creation of commands for {@link client.JavashipsClient}
 * so that they are distinguishable by the logic.
 */
public final class JavashipsProtocol {
    public static final String QUIT = "quit";
    public static final String ATTACK = "attack";
    public static final String SEEKING = "seek";
	public static final String REDRAW = "rd";
	public static final String FIRST = "first";
    public static final String MESSAGE = "msg";

    public static final String SEPARATOR = "-";

    /**
     * @param sender      The client's PrintWriter, which is connected to {@link server.JavashipsServer}.
     * @param chatMessage The chat message that the client is sending to the other player.
     * @throws BadMessageException If the message is blank, don't send it.
     * @throws IOException         If the PrintWriter is null, stop!
     *                             <p>
     *                             Used by {@link client.JavashipsClient} to send chat messages.
     */
    public static void sendMessage(PrintWriter sender, final String chatMessage) throws IOException, BadMessageException {
        if (sender == null) {
            throw new IOException("The specified PrintWriter is null.");
        }
        if (chatMessage.isEmpty()) {
            throw new BadMessageException("The specified message String is null.");
        }

        sender.println(MESSAGE + SEPARATOR + chatMessage);
    }

    /**
	 * Update the other client with the OppGrid position that attacking player moused over.
	 *
	 * @param sender The client's PrintWriter, which is connected to {@link server.JavashipsServer}.
     * @param x      The horizontal coordinate of the OppGrid in {@link client.JavashipsClient}
     *               that the player wishes to attack.
     * @param y      The vertical coordinate of the OppGrid in {@link client.JavashipsClient}
     *               that the player wishes to attack.
     */
    public static void sendSeek(PrintWriter sender, final int x, final int y) {
        sender.println(SEEKING + SEPARATOR + x + SEPARATOR + y);
    }

	/**
	 * Make the other client quit their game.
	 *
	 * @param sender The client's PrintWriter, which is connected to {@link server.JavashipsServer}.
	 */
	public static void sendQuit(PrintWriter sender) {
		sender.println(QUIT);
	}

	/**
	 * For restoring the ship placement colors on the opponent's PlayerButtons grid after the SEEKING position
	 * has changed.
	 *
	 * @param sender
	 */
	public static void sendRedraw(PrintWriter sender) {
		sender.println(REDRAW);
	}
}

class BadMessageException extends Exception {
    public BadMessageException(String message) { super(message); }
}