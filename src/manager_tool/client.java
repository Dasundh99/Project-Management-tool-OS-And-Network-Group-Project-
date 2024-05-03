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
    private JList<String> taskList;

    public Client() {
        super("Project Management Tool (Client)");

        taskInput = new JTextField(20);
        JButton addButton = new JButton("Add Task");
        JButton editButton = new JButton("Mark as Completed");
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);

        // Apply visual enhancements
        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        taskInput.setFont(boldFont);
        addButton.setFont(boldFont);
        editButton.setFont(boldFont);
        taskList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel inputPanel = new JPanel();
        inputPanel.add(taskInput);
        inputPanel.add(addButton);

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove border around JScrollPane

        // Apply color to inputPanel
        inputPanel.setBackground(new Color(240, 240, 240)); // Light gray background

        // Add editButton to inputPanel
        inputPanel.add(editButton);

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

        // Action listener for the editButton
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String taskDescription = taskListModel.getElementAt(selectedIndex);
                    // Remove the "[Completed] " or "[Not Complete] " prefix
                    taskDescription = taskDescription.substring(taskDescription.indexOf(" ") + 1);

                    // Get the original task object from the taskDescription
                    Task selectedTask = new Task(taskDescription);
                    // Toggle the completion status
                    selectedTask.setCompleted(!selectedTask.isCompleted());

                    // Update the task description
                    String updatedTaskDescription;
                    if (selectedTask.isCompleted()) {
                        updatedTaskDescription = "[Completed] " + selectedTask.getDescription();
                    } else {
                        updatedTaskDescription = "[Not Complete] " + selectedTask.getDescription();
                    }

                    // Update the list with the updated task description
                    taskListModel.setElementAt(updatedTaskDescription, selectedIndex);

                    // Send the updated task to the server
                    sendTaskToServer(selectedTask);
                } else {
                    JOptionPane.showMessageDialog(Client.this, "Please select a task to edit.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
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
                String taskDescription;
                if (task.isCompleted()) {
                    taskDescription = task.getDescription();
                    // Check if the task description already has the completion status prefix
                    if (!taskDescription.startsWith("[Completed] ")) {
                        taskDescription = "[Completed] " + taskDescription;
                    }
                } else {
                    taskDescription = task.getDescription();
                    // Check if the task description already has the completion status prefix
                    if (!taskDescription.startsWith("[Not Complete] ")) {
                        taskDescription = "[Not Complete] " + taskDescription;
                    }
                }
                // Add the formatted task description to the list if it's not already present
                if (!taskListModel.contains(taskDescription)) {
                    taskListModel.addElement(taskDescription);
                }
                // Check if both completed and not completed tasks are present
                String completedTaskDescription = "[Completed] " + task.getDescription();
                String notCompletedTaskDescription = "[Not Complete] " + task.getDescription();
                if (taskListModel.contains(completedTaskDescription) && taskListModel.contains(notCompletedTaskDescription)) {
                    // If both are present, remove the not completed task
                    taskListModel.removeElement(notCompletedTaskDescription);
                }
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
