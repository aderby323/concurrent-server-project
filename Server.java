import java.net.*;
import java.io.*;

/* *
 * 
 * Iterative Socket Server w/ Multi-Threaded Client 
 * CNT 4504: Computer Networks and Distributed Processing 
 * Professor John Scott Kelly 
 * Alexander Derby, Afsara Chowdhurry, Emily Ottesen 
 * 11/30/2020
 *
 * */

public class Server  {

    // Get port number from user and create new Server
    public static void main(String[] args) throws IOException {
        ServerSocket server;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter port: ");
        int port = Integer.parseInt(reader.readLine());
        
        try {
            server = new ServerSocket(port);
            System.out.println("Server created");
            while (true) {
                System.out.println("Waiting for a connection...");
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress().getHostAddress());
                ClientHandler cHandler = new ClientHandler(client);

                new Thread(cHandler).start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {

    DataInputStream input;
    DataOutputStream output;
    Socket socket;

    // Instantiate new connection to the Server
    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            String request = input.readUTF();
            System.out.println(request);
            switch (request.toLowerCase()) {
                case "datetime":
                    executeRequest("date +%d/%m/%Y%t%H:%M:%S");
                    break;
                case "uptime":
                    executeRequest("uptime");
                    break;
                case "memoryuse":
                    executeRequest("free");
                    break;
                case "netstat":
                    executeRequest("netstat");
                    break;
                case "currentusers":
                    executeRequest("who");
                    break;
                case "runningprocesses":
                    executeRequest("ps -e");
                    break;
                default:
                    output.writeUTF("Unknown request.");
                    break;
                }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                output.close();
                input.close();
                socket.close();
            } catch (IOException ioException) { ioException.printStackTrace(); }
        }
    }

    // Execute command given by user 
    private void executeRequest(String request) throws IOException {
 
		Runtime runtime = Runtime.getRuntime();
        Process process = runtime.exec(request);
        StringBuilder response = new StringBuilder();
        BufferedReader responseInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;

        while ((line = responseInput.readLine()) != null) {
			System.out.println(line);
            response.append(line + "\n");
        }
        respondToClient(response);
    }

    // Send response back to Client
    private void respondToClient(StringBuilder response) {
        try {
            output.writeUTF(response.toString());
            output.flush();
        } catch (IOException ioException) { ioException.printStackTrace(); }
    }
}