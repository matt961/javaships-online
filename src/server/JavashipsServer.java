/**
 * Created by Matt on 11/21/2016.
 */
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class JavashipsServer {
    enum MessageType {
        ATTACK ("ATTACK");

        private final String type;
        MessageType (final String type) {
            this.type = type;
        }
    }

    private static final int PORT = 9876;

    private JavashipsServer() {
        try {
            ServerSocket server = new ServerSocket(PORT);

            System.out.println("Waiting for 2 players to connect.");
            Player player1 = new Player(server.accept());
            Player player2 = new Player(server.accept());
            server.close();
            // done getting players

            player1.setOppenent(player2);
            player2.setOppenent(player1);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new JavashipsServer();
        System.out.println(new Date().toString() + " - Started server!");
    }

    /**
     * Created by Matt on 11/21/2016.
     */
    class Player extends Thread {

        private Socket fromClient;

        private Player opponent;

        private BufferedReader commandReader;

        Player (final Socket fromClient) throws IOException {
            this.fromClient = fromClient;
            commandReader = new BufferedReader(
                    new InputStreamReader(fromClient.getInputStream()));
        }

        void setOppenent (final Player opponent) {
            this.opponent = opponent;
        }

        void attackOpponent (final int x, final int y) {

        }

        @Override
        public void run() {

        }
    }
}


