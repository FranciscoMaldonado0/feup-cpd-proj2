import java.net.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {

    private static final ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("new Date()?".toString());
            //while socket is open
            while(!socket.isClosed()){

                OutputStream o = socket.getOutputStream();
                PrintWriter w = new PrintWriter(o, true);
                BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
                String num = r.readLine();
                w.println(num);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println(time);
                if(num.startsWith("CLOSE")){
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
}
