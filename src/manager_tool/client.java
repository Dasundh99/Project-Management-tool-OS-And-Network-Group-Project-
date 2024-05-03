package manager_tool;

import java.io.*;
import java.net.*;
import java.util.List;

import javax.swing.JOptionPane;

public class client {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    public client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    private void handleConnectionError(Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to connect to the server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        close();
    }

    private void close() {
        try {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            // Ignore any errors while closing resources
        }
    }

    public void addTask(Task task) {
        try {
            outputStream.writeObject("ADD");
            outputStream.writeObject(task);
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    public void deleteTask(int index) {
        try {
            outputStream.writeObject("DELETE");
            outputStream.writeInt(index);
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Task> viewTasks() {
        try {
            outputStream.writeObject("VIEW");
            return (List<Task>) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            handleConnectionError(e);
        }
        return null;
    }

    public static void main(String[] args) {
        client client = new client("localhost", 5000); // Connect to the server running on localhost:5000
        if (client.socket != null && client.socket.isConnected()) {
            // Now you can use client.addTask(task), client.deleteTask(index), and client.viewTasks() methods to interact with the server
        } else {
            JOptionPane.showMessageDialog(null, "Failed to connect to the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
