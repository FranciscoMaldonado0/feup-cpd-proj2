import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;

public class TimeServer {

    private static final String USERS_FILE = "database.txt";
    public static int sum = 0;
    private static final ReentrantLock lock = new ReentrantLock();
    
    private static Map<String,String> users;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        users = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();                
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    
                String username = reader.readLine(); // reads request from client
                String password = reader.readLine();
                if (authenticate(username,password)) {
                    writer.println("SUCCESS");
    
                }
                else {
                    writer.println("ERROR");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

            
        private boolean authenticate(String username, String password) {
            String expectedPassword = users.get(username);
            return expectedPassword != null && expectedPassword.equals(password);
        }
    }
    
}
