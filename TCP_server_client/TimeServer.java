import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

public class TimeServer {

    public static int sum = 0;
    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        if (args.length < 1) return;

        int port = Integer.parseInt(args[0]);

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

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private int clientSum = 0;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                //create string with string builder
                StringBuilder sb = new StringBuilder();

                String time = reader.readLine();

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                boolean closed = false;

                while (!closed) {
                    String t = reader.readLine();

                    // if t starts with "CLOSE" then close the socket
                    if (t != null && t.startsWith("CLOSE")) {
                        lock.lock();
                        try {
                            sum += clientSum;
                        } finally {
                            lock.unlock();
                        }
                        writer.println(sum);
                        socket.close();
                        closed = true;
                        break;
                    }
                    clientSum += Integer.parseInt(t);
                    sb.append(t.toString()).append(" ");
                    System.out.println(t);

                    writer.println(sb.toString());
                }

            } catch (IOException ex) {
                System.out.println("Client disconnected");
            }
        }
    }
}
