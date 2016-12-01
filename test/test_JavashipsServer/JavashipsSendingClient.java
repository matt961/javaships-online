package test_JavashipsServer; /**
 * Created by matt on 29/11/16.
 */

import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import static protocol.JavashipsProtocol.sendMessage;

public class JavashipsSendingClient {

    @Test
    public void testJavashipsServerMessagePassing() {
        try {
            Socket player1 = new Socket("127.0.0.1", 9876);

            InetAddress thisAddress = player1.getInetAddress();

            System.out.println("Host: " + thisAddress.getHostAddress());
            System.out.println("Port: " + player1.getPort());

            PrintWriter writer = new PrintWriter(player1.getOutputStream(), true);

            sendMessage(writer, "Hello world XD");

            player1.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
