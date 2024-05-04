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
        JButton completedButton = new JButton("Mark as Completed");
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);

        // Apply styles
        Font boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        taskInput.setFont(boldFont);
        addButton.setFont(boldFont);
        completedButton.setFont(boldFont);
        taskList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        // cell renderer
        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = (String) value;
                if (text.startsWith("[Completed]")) {
                    c.setForeground(Color.BLUE); // Set green color for completed tasks
                } else {
                    c.setForeground(Color.RED); // Set red color for incomplete tasks
                }
                c.setFont(c.getFont().deriveFont(Font.BOLD)); // Make the font bold
                return c;
            }
        });

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        inputPanel.add(taskInput);
        inputPanel.add(addButton);
        inputPanel.add(completedButton);

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        inputPanel.setBackground(new Color(240, 240, 240));
        scrollPane.getViewport().setBackground(Color.WHITE);

        addButton.setMargin(new Insets(5, 10, 5, 10));
        completedButton.setMargin(new Insets(5, 10, 5, 10));

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(Color.WHITE, 5)
        ));

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

        completedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = taskList.getSelectedIndex();
                if (selectedIndex != -1) {
                    String taskDescription = taskListModel.getElementAt(selectedIndex);
                    taskDescription = taskDescription.substring(taskDescription.indexOf(" ") + 1);
                    Task selectedTask = new Task(taskDescription);
                    selectedTask.setCompleted(!selectedTask.isCompleted());
                    String updatedTaskDescription;
                    if (selectedTask.isCompleted()) {
                        updatedTaskDescription = "[Completed] " + selectedTask.getDescription();
                    } else {
                        updatedTaskDescription = "[Incomplete] " + selectedTask.getDescription();
                    }
                    taskListModel.setElementAt(updatedTaskDescription, selectedIndex);
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
                    if (!taskDescription.startsWith("[Completed] ")) {
                        taskDescription = "[Completed] " + taskDescription;
                    }
                } else {
                    taskDescription = task.getDescription();
                    if (!taskDescription.startsWith("[Incomplete] ")) {
                        taskDescription = "[Incomplete] " + taskDescription;
                    }
                }
                if (!taskListModel.contains(taskDescription)) {
                    taskListModel.addElement(taskDescription);
                }
                String completedTaskDescription = "[Completed] " + task.getDescription();
                String notCompletedTaskDescription = "[Incomplete] " + task.getDescription();
                if (taskListModel.contains(completedTaskDescription) && taskListModel.contains(notCompletedTaskDescription)) {
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
