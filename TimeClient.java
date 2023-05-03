import java.net.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TimeClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

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
        }
    
    }

    private static boolean authenticate(Socket socket, String username, String password) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    
            // send credentials to server
            out.println(username);
            out.println(password);
    
            // wait for server response
            String response = in.readLine();
    
            // check response
            if (response.equals("SUCCESS")) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error in authenticate: " + e.getMessage());
            return false;
        }
    }
    
}
