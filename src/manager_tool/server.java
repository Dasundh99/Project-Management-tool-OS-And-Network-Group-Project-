package manager_tool;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class server {
    private List<Task> tasks;

    public server() {
        tasks = new ArrayList<>();
    }

    public synchronized void addTask(Task task) {
        tasks.add(task);
    }

    public synchronized void deleteTask(int index) {
        if (index >= 0 && index < tasks.size()) {
            tasks.remove(index);
        }
    }

    public synchronized List<Task> getTasks() {
        return new ArrayList<>(tasks);
    }

    public static void main(String[] args) {
        server server = new server();
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started on port 5000");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, server);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private server server;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public ClientHandler(Socket clientSocket, server server) {
            this.clientSocket = clientSocket;
            this.server = server;
            try {
                outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                inputStream = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException e) {
                handleConnectionError(e);
            }
        }

        private void handleConnectionError(Exception e) {
            e.printStackTrace();
            // Handle connection error here
            close();
        }

        private void close() {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                // Ignore any errors while closing resources
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String command = (String) inputStream.readObject();
                    switch (command) {
                        case "ADD":
                            Task task = (Task) inputStream.readObject();
                            server.addTask(task);
                            break;
                        case "DELETE":
                            int deleteIndex = inputStream.readInt();
                            server.deleteTask(deleteIndex);
                            break;
                        case "VIEW":
                            List<Task> tasks = server.getTasks();
                            outputStream.writeObject(tasks);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                handleConnectionError(e);
            } finally {
                close();
            }
        }
    }
}
