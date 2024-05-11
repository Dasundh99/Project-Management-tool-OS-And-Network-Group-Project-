package manager_tool;

import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private static final int PORT = 5000;
    private List<ClientHandler> clients = new ArrayList<>();

    public server() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream outputStream;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                outputStream = new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                while (socket.isConnected()) {
                    Task task = (Task) inputStream.readObject();
                    broadcastTask(task);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client disconnected: " + socket);
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void sendTask(Task task) {
            try {
                outputStream.writeObject(task);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized void broadcastTask(Task task) {
        for (Iterator<ClientHandler> iterator = clients.iterator(); iterator.hasNext();) {
            ClientHandler client = iterator.next();
            if (!client.isAlive()) {
                iterator.remove();
                continue;
            }
            client.sendTask(task);
        }
    }

    public static void main(String[] args) {
        new server();
    }
}
