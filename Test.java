import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;

public class Test {

    private static final String USERS_FILE = "database.txt";
    private static final ReentrantLock lock = new ReentrantLock();
    private static Map<String,String> users;
    //private static Map<Socket, ClientThread> clients = new HashMap<>();

    private static final int PORT = 8000;
    private static final int MAX_CLIENTS = 2;
    private static final LinkedBlockingQueue<Socket> clientQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) throws IOException {
        users = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Accept incoming client connections and add them to the queue
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("    New client connected");

                if (clientQueue.size() == MAX_CLIENTS) {

                    // Start a separate thread to handle client connections
                    Thread clientThread = new Thread(() -> {
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                // Wait for a client connection in the queue
                                while (true) {

                                    // 1st client
                                    Socket clientSocket1 = clientQueue.take();
                                    // Handle the client connection in a separate thread
                                    Thread clientHandlerThread1 = new Thread(new ClientHandler(clientSocket1));
                                    //clients.put(clientSocket1, clientHandlerThread1);
                                    clientHandlerThread1.start();

                                    // 2nd client
                                    Socket clientSocket2 = clientQueue.take();
                                    // Handle the client connection in a separate thread
                                    Thread clientHandlerThread2 = new Thread(new ClientHandler(clientSocket2));
                                    //clients.put(clientSocket2, clientHandlerThread2);
                                    clientHandlerThread2.start();

                                    // 3rd client
                                    Socket clientSocket3 = clientQueue.take();
                                    // Handle the client connection in a separate thread
                                    Thread clientHandlerThread3 = new Thread(new ClientHandler(clientSocket3));
                                    //clients.put(clientSocket3, clientHandlerThread3);
                                    clientHandlerThread3.start();

                                    try {
                                        clientHandlerThread1.join();
                                        // Client thread has finished
                                        try {
                                            clientSocket1.close();
                                            clientSocket2.close();
                                            clientSocket3.close();

                                            System.out.println("Clients connection closed");
                                            break;
                                        }
                                        catch (IOException i) {
                                            System.out.println("ERROR closing Clients connection");
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    /*try {
                                        clientHandlerThread2.join();
                                        // Client thread has finished
                                        try {
                                            clientSocket1.close();
                                            clientSocket2.close();
                                            clientSocket3.close();

                                            System.out.println("Clients connection closed");
                                            break;
                                        }
                                        catch (IOException i) {
                                            System.out.println("ERROR closing Clients connection");
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        clientHandlerThread3.join();
                                        // Client thread has finished
                                        try {
                                            clientSocket1.close();
                                            clientSocket2.close();
                                            clientSocket3.close();

                                            System.out.println("Clients connection closed");
                                            break;
                                        }
                                        catch (IOException i) {
                                            System.out.println("ERROR closing Clients connection");
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }*/

                                }

                            }
                            catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            System.out.println("   HERE");
                            Thread.currentThread().interrupt();

                        }
                    });
                    clientThread.start();


                    // Close the client socket if the queue is full
                    System.out.println("--> Client socket CLOSED");
                    clientSocket.close();
                }
                else {
                    clientQueue.add(clientSocket);
                }
                System.out.println("clientQueue: " + clientQueue);

            }
        }
        catch (IOException e) {
            System.out.println("Exception in serverSocket.");
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

                while (true) {
                    String username = reader.readLine(); // reads request from client
                    String password = reader.readLine();
                    if (authenticate(username, password)) {
                        writer.println("SUCCESS");
                        break;
                    }
                    System.out.println("ERROR - Invalid credentials.");
                    writer.println("ERROR");

                }
                // JOGADOR AUTENTICADO, PODE JOGAR


                // --- GAME ---
                writer.println("(The word to guess is: "+real_word+")");

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
            catch (IOException e) {
                System.out.println("\nClient disconnected\n");
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                }
                catch (IOException e) {
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
