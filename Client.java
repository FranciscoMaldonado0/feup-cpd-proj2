import java.net.*;
import java.io.*;


public class Client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <host> <port>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(host,port)) {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter username: ");
            String username = input.readLine();
            System.out.println("Enter password: ");
            String password = input.readLine();
            
            OutputStream output = socket.getOutputStream();
            output.write((username + "\n").getBytes());
            output.write((password + "\n").getBytes());
            output.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            System.out.println("Server response: " + response);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
