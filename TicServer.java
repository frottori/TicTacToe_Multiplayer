import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;

public class TicServer {

    private static int MAX_CLIENTS = 2;
    private static int currentPlayer = 1;
    private static Semaphore semaphore = new Semaphore(1); // Semaphore for controlling access to currentPlayer

    public static void main(String[] args) {

        int[][] board = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = -1;
            }
        }

        ServerSocket serverSocket = null;
        int port = 1761;

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);

            for (int i = 0; i < MAX_CLIENTS; i++) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Waiting for client " + i + " to connect...");

                Thread clientThread = new ClientHandlerThread(clientSocket, i, board);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int getCurrentPlayer() {
        return currentPlayer;
    }

    public static void setCurrentPlayer(int newPlayer) {
        currentPlayer = newPlayer;
    }

    public static Semaphore getSemaphore() {
        return semaphore;
    }
}

class ClientHandlerThread extends Thread {
    private Socket clientSocket;
    private int clientNumber;
    private int[][] board;

    public ClientHandlerThread(Socket socket, int clientNumber, int[][] board) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.board = board;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + clientSocket.getInetAddress() + " " + clientNumber);
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(TicServer.getCurrentPlayer());
            out.println(clientNumber);

            int code = 1;

            while (code == 1 || code == 2) {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String codeString = in.readLine();
                if (codeString != null && !codeString.isEmpty()) {
                        code = Integer.parseInt(codeString);
                    }
                if (code == 0) // end server
                    break;
                else if (code == 1) { // setAssigned
                    int val = Integer.parseInt(in.readLine());
                    int k = Integer.parseInt(in.readLine());
                    int l = Integer.parseInt(in.readLine());

                    Semaphore semaphore = TicServer.getSemaphore();
                    semaphore.acquire(); // Acquire the semaphore before updating currentPlayer

                    int currentPlayer = Integer.parseInt(in.readLine());
                    System.out.println("Player that played last: " + currentPlayer);

                    if (currentPlayer == 0) {
                        TicServer.setCurrentPlayer(1);
                    } else {
                        TicServer.setCurrentPlayer(0);
                    }

                    semaphore.release(); // Release the semaphore after updating currentPlayer

                    board[k][l] = val;
                } else if (code == 2) { // update
                    ObjectOutputStream ooStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    ooStream.writeObject(this.board);
                    ooStream.flush();

                    Semaphore semaphore = TicServer.getSemaphore();
                    semaphore.acquire(); // Acquire the semaphore before reading currentPlayer

                    int currentPlayer = TicServer.getCurrentPlayer();
                    System.out.println("Player to play: " + currentPlayer);

                    semaphore.release(); // Release the semaphore after reading currentPlayer

                    out.println(currentPlayer);
                    out.flush();
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}