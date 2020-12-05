import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.io.*;
import java.util.Scanner;

/* *
 * 
 * Concurrent Socket Server w/ Multi-Threaded Client 
 * CNT 4504: Computer Networks and Distributed Processing 
 * Professor John Scott Kelly 
 * Alexander Derby, Afsara Chowdhurry, Emily Ottesen 
 * 12/01/2020
 *
 * */

public class Client {   

    // Get IP address, port number, and user response
    public static void main(String[] args) throws IOException, InterruptedException {
        
        Scanner input = new Scanner(System.in);
        ClientConnection[] clients = new ClientConnection[100];
        long[] times = new long[100];
        long totalTurnAroundTime;

        // Get desired address and port number
        System.out.print("Enter address: ");
        String address = input.nextLine();
        System.out.print("Enter port number: ");
        int port = input.nextInt();

        while (true) {

            System.out.println("\n================================================================================");
            System.out.println("|| Type in a command (Case-Insensitive)");
            System.out.println("|| Datetime - Get date and time on the server.");
            System.out.println("|| Uptime - Amount of time server has been running since last boot-up");
            System.out.println("|| MemoryUse - Current memory usage on the server.");
            System.out.println("|| Netstat - Lists network connections on the server.");
            System.out.println("|| CurrentUsers - Lists users currently connected to the server.");
            System.out.println("|| RunningProcesses - Lists programs running on the server.");
            System.out.println("|| Quit - Exits the program.");
            System.out.println("================================================================================");
            
            System.out.print("\nEnter a command: ");
            String userInput = input.next();
            if (userInput.equalsIgnoreCase("quit")) { break; }
            System.out.println("\nNumber of clients to spawn (1, 5, 10, 15, 20, 25, 100): ");
            int clientsToCreate = input.nextInt();
            if (!((clientsToCreate == 1) || (clientsToCreate > 0 && (clientsToCreate <= 25 || clientsToCreate == 100) && clientsToCreate % 5 == 0))) {
                System.out.println("You may only create 1, 5, 10, 15, 20, 25, or 100 clients.");
                continue;
            }

            totalTurnAroundTime = 0;
            
            // Create desired number of clients
            for (int i = 0; i < clientsToCreate; i++) {
                ClientConnection client = new ClientConnection(address, port, userInput);
                clients[i] = client;
            }

            for (int j = 0; j < clientsToCreate; j++) {
                clients[j].start();
                clients[j].join();
                times[j] = clients[j].getTurnAroundTime();
                totalTurnAroundTime = totalTurnAroundTime + times[j];
            }

            System.out.println("Total turn-around time for " + clientsToCreate + " clients: " + totalTurnAroundTime + "ms.");
            System.out.println("Average turn-around time: " + (totalTurnAroundTime / clientsToCreate) + "ms.");

        }

        //Close the Scanner
        input.close();
    }
}

class ClientConnection extends Thread {

    private String response;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private Instant start;
    private Instant finish;

    public ClientConnection(String address, int port, String response) throws IOException {
        this.socket = new Socket(address, port);
        this.response = response;
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {

        // Send the user's response to Server
        try {
            start = Instant.now();
            output.writeUTF(response);
            output.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Wait for the server to send a response, print each line,
        // and record the Turn-around time.
        try {
            while (true) {
                if (input.available() > 0) {
                    System.out.println(input.readUTF());
                    break;
                }
            }
            finish = Instant.now();
            System.out.println("Turn-around time for Client: " + getTurnAroundTime() + " ms.\n");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Kill the connection
        try { socket.close(); } 
        catch (IOException ioException) { ioException.printStackTrace(); }
    }

    //Return the Turn-around time for the process
    public long getTurnAroundTime() {
        return Duration.between(start, finish).toMillis();
    }
}