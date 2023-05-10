import java.io.*;
import java.net.*;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;

public class TimeServer {

    private static final String USERS_FILE = "database.txt";
    private static final int BUFFER_SIZE = 1024;
    private static final String CHARSET_NAME = "UTF-8";
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

        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("Server is listening on port " + port);

            while (true) {
                int readyChannels = selector.select();
                System.out.println("readyChannels: " + readyChannels);
                if (readyChannels == 0) {
                    continue;
                }   

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("New client connected");

                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                        int bytesRead = socketChannel.read(buffer);
                        if (bytesRead == -1) {
                            System.out.println("Client disconnected");
                            key.cancel();
                            socketChannel.close();
                            continue;
                        }
                        String message = new String(buffer.array(), 0, bytesRead);
                        String[] credentials = message.split(":");
                        if (credentials.length == 2) {
                            boolean authenticated = authenticate(credentials[0], credentials[1]);
                            if (authenticated) {
                                socketChannel.register(selector, SelectionKey.OP_WRITE, "SUCCESS");
                            } else {
                                socketChannel.register(selector, SelectionKey.OP_WRITE, "ERROR");
                            }
                        }
                    } else if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        String response = (String) key.attachment();
                        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
                        socketChannel.write(buffer);
                        socketChannel.close();
                    }

                }
            }


        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        /*
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
        }*/
    }

                
    private static boolean authenticate(String username, String password) {
        String expectedPassword = users.get(username);
        return expectedPassword != null && expectedPassword.equals(password);
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

                boolean authenticated = false;
                while (!authenticated) {
                    authenticated = authenticate(username, password);
                    if (!authenticated) {
                        writer.println("ERROR");
                        username = reader.readLine();
                        password = reader.readLine();
                    }
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

    }
    
}
