
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class JavashipsServer {
    private static final int PORT = 9876;

    /**
     * The main method creates a new JavashipsServer
     * that facilitates the message passing between
     * clients. All game logic is processed client-side.
     */
    private JavashipsServer() {
        try {
            ServerSocket server = new ServerSocket(PORT);

            System.out.println("Waiting for 2 players to connect.");
            Player player1 = new Player(server.accept(), true);
            Player player2 = new Player(server.accept(), false);
            server.close();
            // done getting players

            player1.setOpponent(player2);
            player2.setOpponent(player1);

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
        private boolean turn;

        private Socket playerSocket;

        private Player opponentPlayer;

        private BufferedReader commandReader;

        private PrintWriter toOpponentCommandWriter;

        /**
         * @param playerSocket The client's socket used for receiving and
         *                     sending commands.
         * @param turn The first player to connect gets "true" and the last
         *             player to connect gets "false"
         * @throws IOException
         */
        Player(final Socket playerSocket, final boolean turn) throws IOException {
            this.playerSocket = playerSocket;

            this.turn = turn;

            commandReader = new BufferedReader(
                    new InputStreamReader(playerSocket.getInputStream()));
        }

        /**
         *
         * @param opponentPlayer This player's opponent. The player echoes
         *                       a command received to the opponent for parsing.
         * @throws IOException
         */
        void setOpponent(final Player opponentPlayer) throws IOException {
            this.opponentPlayer = opponentPlayer;

            this.toOpponentCommandWriter = new PrintWriter(
                    getOpponentSocket().getOutputStream());
        }

        /**
         * Let the other player receive and send a command.
         */
        void changeTurns() {
            opponentPlayer.turn = true;
            this.turn = false;
        }

        Socket getOpponentSocket() {
            return opponentPlayer.playerSocket;
        }

        /**
         *
         * @return The command that this Player's client sends.
         * @throws IOException
         */
        String getCommandFromPlayer() throws IOException {
            return commandReader.readLine();
        }

        /**
         *
         * @param command The command that getCommandFromPlayer
         *                receives is to be sent to the opponent's
         *                client for parsing.
         * @throws IOException
         */
        void sendCommandToOpponent(final String command) throws IOException {
            toOpponentCommandWriter.println(command);
            toOpponentCommandWriter.flush();
        }

        @Override
        public void run() {
            while (true) {
                if (turn) {
                    try {
                        String command = getCommandFromPlayer();

                        sendCommandToOpponent(command);

                        changeTurns();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println(new Date().toString() + " - Connection lost.");
                        System.exit(1);
                    }
                } else {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


