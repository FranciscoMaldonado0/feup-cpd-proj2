import java.net.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {

    private static final ReentrantLock lock = new ReentrantLock();

    private static final int BUFFER_SIZE = 1024;
    private static final String CHARSET_NAME = "UTF-8";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TimeClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        /* 
        try (Socket socket = new Socket(hostname, port)) {

            System.out.println("Connected to " + hostname + " on port " + port);

            //while socket is open
            while(!socket.isClosed()){
                boolean authenticated = false;
                // Authenticate with the server using a username and password from a file
                while (!authenticated){
                    Scanner scanner = new Scanner(System.in);
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    if (authenticate(socket, username, password)) {
                        System.out.println("Authentication successful!");
                        authenticated = true;
                    } else {
                        System.out.println("Authentication failed!");
                    }
                }
                    // Send a message to the server
                    Scanner scanner = new Scanner(System.in);
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                    System.out.print("Enter message: ");
                    String message = scanner.nextLine();
                    writer.println(message);
                    writer.flush();

                    // Receive a response from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = reader.readLine();
                    System.out.println("Server response: " + response);
                
            }

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }*/

        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(hostname, port));

            while (!socketChannel.finishConnect()) {
                System.out.println("Connecting to server...");
            }

            System.out.println("Connected to server");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Username: ");
            String username = reader.readLine();
            System.out.print("Password: ");
            String password = reader.readLine();

            String message = username + ":" + password;
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(CHARSET_NAME));
            socketChannel.write(buffer);

            buffer.clear();
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                System.out.println("Server closed the connection");
                return;
            }
            buffer.flip();
            String response = new String(buffer.array(), 0, buffer.limit(), CHARSET_NAME);
            System.out.println("Server response: " + response);

            socketChannel.close();

        } catch (IOException ex) {
            System.out.println("Client exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
