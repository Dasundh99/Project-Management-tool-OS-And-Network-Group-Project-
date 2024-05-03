package manager_tool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 5000;
    private ObjectOutputStream outputStream;
    private JTextField taskInput;
    private DefaultListModel<String> taskListModel;

    public Client() {
        super("Project Management Tool (Client)");

        taskInput = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        taskListModel = new DefaultListModel<>();
        JList<String> taskList = new JList<>(taskListModel);

        JPanel inputPanel = new JPanel();
        inputPanel.add(taskInput);
        inputPanel.add(addButton);

        JScrollPane scrollPane = new JScrollPane(taskList);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String description = taskInput.getText().trim();
                if (!description.isEmpty()) {
                    Task task = new Task(description);
                    sendTaskToServer(task);
                    taskInput.setText("");
                }
            }
        });

        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        connectToServer();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            Task task = (Task) inputStream.readObject();
                            addTask(task);
                        }
                    } catch (SocketException e) {
                        System.err.println("Connection reset by server.");
                        e.printStackTrace();
                        System.exit(0);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendTaskToServer(Task task) {
        try {
            outputStream.writeObject(task);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTask(final Task task) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                taskListModel.addElement(task.getDescription());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}
