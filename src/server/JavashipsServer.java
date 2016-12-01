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
     * that facilitates the sendMessage passing between
     * clients. All game logic is processed client-side.
     */
    public JavashipsServer() {
        try {
            PrintWriter serverWriter;

            ServerSocket server = new ServerSocket(PORT);

            System.out.println("Waiting for 2 players to connect.");

            Player player1 = new Player(server.accept(), "Player 1");
            System.out.println(new Date().toString() +
                    " - First player has been found: " +
                    player1.playerSocket.getInetAddress().getHostAddress() +
                    "/" +
                    player1.playerSocket.getPort());

            Player player2 = new Player(server.accept(), "Player 2");
            System.out.println(new Date().toString() +
                    " - Second player has been found: " +
                    player2.playerSocket.getInetAddress().getHostAddress() +
                    "/" +
                    player2.playerSocket.getPort());

            server.close();
            // done getting players

            serverWriter = new PrintWriter(player1.playerSocket.getOutputStream());
            serverWriter.println();

            player1.setOpponent(player2);
            player2.setOpponent(player1);

            player1.start();
            player2.start();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new Date().toString() + " - Started server!");
        new JavashipsServer();
    }

    /**
     * Created by Matt on 11/21/2016.
     */
    class Player extends Thread {

        private Socket playerSocket;

        private Player opponentPlayer;

        private BufferedReader commandReader;

        private PrintWriter toOpponentCommandWriter;

        private String name;

        /**
         * @param playerSocket The client's socket used for receiving and
         *                     sending commands.
         * @throws IOException
         */
        Player(final Socket playerSocket, final String name) throws IOException {
            this.playerSocket = playerSocket;
            this.name = name;

            commandReader = new BufferedReader(
                    new InputStreamReader(playerSocket.getInputStream()));
        }

        /**
         * @param opponentPlayer This player's opponent. The player echoes
         *                       a command received to the opponent for parsing.
         * @throws IOException
         */
        void setOpponent(Player opponentPlayer) throws IOException {
            this.opponentPlayer = opponentPlayer;

            this.toOpponentCommandWriter = new PrintWriter(
                    getOpponentSocket().getOutputStream(), true);
        }

        Socket getOpponentSocket() {
            return opponentPlayer.playerSocket;
        }

        /**
         * @return The command that this Player's client sends.
         * @throws IOException
         */
        String getCommandFromPlayer() throws IOException {
            return commandReader.readLine();
        }

        /**
         * @param command The command that getCommandFromPlayer
         *                receives is to be sent to the opponent's
         *                client for parsing.
         * @throws IOException
         */
        void sendCommandToOpponent(final String command) throws IOException {
            toOpponentCommandWriter.println(command);
        }

        @Override
        public void run() {

            String command;

            while (true) {
                try {
                    command = getCommandFromPlayer();
                    if (command == null) {
                        System.out.println(new Date().toString() + " - " + name + " connection lost.");
                        return;
                    }
                    if (!command.isEmpty()) {
                        System.out.println(new Date().toString() + " :: " + command);
                        sendCommandToOpponent(command);
                    }
                } catch (IOException e) {
                    System.err.println(new Date().toString() + " - " + name + " connection lost.");
                }
            }
        }
    }
}
