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
        private String real_word = "boat"; // Default for now

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            // --- AUTENTICATION ---
            try {
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                PrintWriter writer = new PrintWriter(output, true);
    
                String username = reader.readLine(); // reads request from client
                String password = reader.readLine();
                // JOGADOR AUTENTICADO, PODE JOGAR
                if (authenticate(username,password)) {
                    writer.println("SUCCESS");
                    writer.println("(The word to guess is: "+real_word+")");

                    // --- GAME ---
                    try {
                        //create string with string builder
                        StringBuilder client_guesses = new StringBuilder();
                        String message2send = "Any word was entered.";

                        boolean closed = false;
                        // FAZER ALGO COM O INPUT DO CLIENT - JOGO
                        while (!closed) {
                            // LÊ DO CLIENTE
                            String word_client = reader.readLine();
                            System.out.println("word_client: "+word_client);

                            // if the client guesses the word -> close the socket
                            if (word_client.equals(real_word)){
                                message2send = "Winner! You guess the word '" +real_word+ "'!!";
                                lock.lock();
                                try {
                                    // FAZER ALGO COM O INPUT DO CLIENT? - JOGO (BEFORE END)
                                    System.out.println("GAME OVERR");
                                    // ESCREVE E ENVIA PARA O CLIENTE
                                    writer.println(message2send.toString()+" | "+ client_guesses.toString());
                                } finally {
                                    lock.unlock();
                                }

                                socket.close();
                                closed = true;
                                break;
                            }
                            else{
                                message2send = "Your guess is wrong.. try again!";
                                // o cliente não advinhou a word, o jogo continua..
                                client_guesses.append(word_client.toString()).append(", ");
                                System.out.println("client_guesses: "+client_guesses+"\n");

                                // ESCREVE E ENVIA PARA O CLIENTE
                                writer.println(message2send.toString()+" | "+ client_guesses.toString());
                            }

                        }

                    }
                    catch (IOException e) {
                        System.out.println("\nClient disconnected\n");
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    writer.println("ERROR");
                }

            }
            catch (IOException e) {
                System.out.println("\nClient disconnected\n");
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
