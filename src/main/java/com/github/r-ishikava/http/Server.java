import java.io.*;
import java.net.*;

public class Server {
    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Read from client
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream())
                );

                // Write to client
                PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true
                );

                String requestLine = in.readLine();
                System.out.println("Received: " + requestLine);

                out.println("Hello from the server");
                
                // Handle client...
                clientSocket.close();
            }
        }
    }
}
