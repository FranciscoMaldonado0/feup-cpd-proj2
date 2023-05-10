import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;



public class WorkerRunnable implements Runnable{

    protected Socket clientSocket = null;
    protected String serverText   = null;
    private Map<String, String> users = new HashMap<String, String>();

    public WorkerRunnable(Socket clientSocket, String serverText, Map<String, String> users) {
        this.clientSocket = clientSocket;
        this.serverText   = serverText;
        this.users = users;
    }

    public void run() {
        try {
            
        
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
            String username = bufferedReader.readLine();
            String password = bufferedReader.readLine();

            System.out.println("Username: " + username);
            System.out.println("Password: " + password);
            
            boolean authenticated = false;
                while (!authenticated) {
                    authenticated = authenticate(username, password);
                    if (!authenticated) {
                        output.write("Error\n".getBytes());
                        //username = reader.readLine();
                        //password = reader.readLine();
                    }
                    else {
                        output.write("OK\n".getBytes());
                    }
                }
            
            
            long time = System.currentTimeMillis();
            /* 
            output.write(("HTTP/1.1 200 OK\n\nWorkerRunnable: " +
this.serverText + " - " +
time + " - " + username + " - " + password +
"").getBytes());*/
            output.close();
            input.close();
            System.out.println("Request processed: " + time);
        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }


    private boolean authenticate(String username, String password) {
        String expectedPassword = users.get(username);
        return expectedPassword != null && expectedPassword.equals(password);
    }



}
