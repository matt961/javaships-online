package test_JavashipsServer;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by matt on 29/11/16.
 */
public class JavashipsReceivingClient {
    @Test
    public void testJavashipsServerMessagePassing() {
        try {
            Socket player2 = new Socket("127.0.0.1", 9876);

            InetAddress thisAddress = player2.getInetAddress();

            System.out.println("Host: " + thisAddress.getHostAddress());
            System.out.println("Port: " + player2.getPort());

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(player2.getInputStream()));

            String received = reader.readLine();

            System.out.println("Received message: " + received);

            assertNotEquals("Received string is null.", "", received);
            assertNotNull(received);

            player2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
