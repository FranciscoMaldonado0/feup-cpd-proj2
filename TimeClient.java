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

            Scanner scanner = new Scanner(System.in);
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            BufferedReader sys_reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true);

            boolean authenticated = false;
            int aux = 0;
            //while socket is open
            while(!socket.isClosed()){
                // Authenticate with the server using a username and password from a file
                while (!authenticated){
                    System.out.print("\nEnter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    if (authenticate(socket, username, password)) {
                        System.out.println("\nAuthentication successful!");
                        authenticated = true;
                    } else {
                        System.out.println("\nAuthentication failed!");
                    }
                }

                // to delete later:
                if (aux == 0){
                    String solution = reader.readLine();
                    System.out.println(solution);
                    aux++;
                }

                //              --- GAME ---

                //------- Send a message to the server

                System.out.print("\nEnter your guess: ");
                String guess = sys_reader.readLine();
                writer.println(guess);

                //------- Receive a response from the server

                String result = reader.readLine();
                System.out.println("> " + result);

                // if the client wins the game -> close the socket
                if(result.startsWith("Winner!")){
                    lock.lock();
                    try {
                        socket.close();
                    } finally {
                        lock.unlock();
                    }
                }

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
