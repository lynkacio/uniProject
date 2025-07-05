package GUI;

import model.*;
import utils.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class HelperMain extends JFrame {
    private Helper currentHelper;
    private ArrayList<Pet> pets = new ArrayList<>();
    private ArrayList<Task> tasks = new ArrayList<>();
    private static final String PETS_FILE = "pets.txt";
    private static final String TASKS_FILE = "tasks.txt";

    private DefaultTableModel petTableModel;
    private DefaultTableModel taskTableModel;

    public HelperMain(Person p) {
        if (!(p instanceof Helper)) {
            JOptionPane.showMessageDialog(null, "Invalid user type - Only Helpers can access this dashboard");
            System.exit(1);
        }
        
        this.currentHelper = (Helper) p;
        // Load data using DataManager
        this.pets = DataManagerForHelper.loadPetsForHelpers(PETS_FILE);
        this.tasks = DataManagerForHelper.loadTasksFromFileForHelper(TASKS_FILE);

        setTitle("Helper Dashboard - " + p.fullName());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(10, 10));

        // Main panel with padding
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side panel
        JPanel leftPanel = new JPanel(new GridLayout(3, 1, 10, 10));

        // 1. Upper Left - Helper Information
        JPanel infoPanel = createHelperInfoPanel();
        leftPanel.add(infoPanel);

        // 2. Middle Left - Verification Panel
        JPanel verificationPanel = createVerificationPanel();
        leftPanel.add(verificationPanel);

        // 3. Lower Left - Available Tasks (now as table)
        JPanel tasksPanel = createTasksPanel();
        leftPanel.add(tasksPanel);

        // Right side panel - Pets table and credit rating
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Pets table
        JPanel petsPanel = createPetsPanel();
        rightPanel.add(petsPanel, BorderLayout.CENTER);

        // Credit rating
        JPanel creditPanel = createCreditPanel();
        rightPanel.add(creditPanel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHelperInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Helper Information"));

        String infoText = "Name: " + currentHelper.fullName() + "\n" + 
                         "User ID: " + currentHelper.getUserId() + "\n" +
                         "Role: " + currentHelper.getRole() + "\n" +
                         "Credit Score: " + String.format("%.1f", currentHelper.getCreditScore()) + "/10.0\n" +
                         "Tasks Completed: " + currentHelper.getAssignedTasks().size();

        JTextArea helperInfo = new JTextArea(infoText);
        helperInfo.setEditable(false);
        panel.add(new JScrollPane(helperInfo), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createVerificationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Verification Status"));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        try {
            String iconPath = currentHelper.hasVolunteeringCertificate()
                    ? "verified_icon.jpg"
                    : "unproved_icon.jpg";
            ImageIcon icon = new ImageIcon(iconPath);
            JLabel iconLabel = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH)));
            statusPanel.add(iconLabel);
        } catch (Exception e) {
            statusPanel.add(new JLabel("[ICON]"));
        }

        String statusText = currentHelper.hasVolunteeringCertificate()
                ? "You have passed the verification"
                : "Verification pending";
        JLabel statusLabel = new JLabel(statusText);
        statusPanel.add(statusLabel);
        panel.add(statusPanel, BorderLayout.NORTH);

        JButton uploadButton = new JButton("Upload Certificate");
        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (FileTransferManager.sendFile(selectedFile)) {
                    currentHelper.setVolunteeringCertificate(true);
                    JOptionPane.showMessageDialog(this,
                            "Certificate uploaded and sent to admin successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the panel or entire window to reflect the new status
                    SwingUtilities.getWindowAncestor(panel).dispose(); 
                    new HelperMain(currentHelper); // reload UI to refresh icon
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to send certificate to admin",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(uploadButton, BorderLayout.SOUTH);

        return panel;
    }


    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Tasks"));

        // Task table setup
        String[] columns = { "Description", "Hours", "Wage", "Status" };
        taskTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        updateTaskTable();

        JTable taskTable = new JTable(taskTableModel);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton acceptButton = new JButton("Accept Task");
        acceptButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                // Get the actual task from the filtered list
                Task selectedTask = tasks.stream()
                    .filter(t -> !t.isTaken())
                    .skip(selectedRow)
                    .findFirst()
                    .orElse(null);

                if (selectedTask != null) {
                    try {
                        // Set the task as taken and assign to helper
                        selectedTask.setTaken();
                        currentHelper.assignTask(selectedTask);
                        
                        // Save the updated tasks
                        DataManager.saveTasksToFile(TASKS_FILE, tasks);
                        
                        // Update the UI
                        updateTaskTable();
                        JOptionPane.showMessageDialog(this, "Task accepted successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (IllegalStateException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
        panel.add(acceptButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPetsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Available Pets"));

        // Pet table setup
        String[] columns = { "Name", "Type", "Age", "Details" };
        petTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        updatePetTable();

        JTable petTable = new JTable(petTableModel);
        panel.add(new JScrollPane(petTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCreditPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Your Credit Rating"));

        JPanel starsPanel = new JPanel(new GridLayout(1, 10));
        int fullStars = (int) currentHelper.getCreditScore();
        for (int i = 1; i <= 10; i++) {
            JLabel starLabel = new JLabel();
            starLabel.setHorizontalAlignment(SwingConstants.CENTER);
            if (i <= fullStars) {
                starLabel.setIcon(new ImageIcon("full_star.png"));
            } else {
                starLabel.setIcon(new ImageIcon("empty_star.png"));
            }
            starsPanel.add(starLabel);
        }

        panel.add(starsPanel, BorderLayout.CENTER);

        JLabel earningsLabel = new JLabel(
                String.format("Total Earnings: $%.2f", currentHelper.calculateTotalEarnings()), SwingConstants.CENTER);
        panel.add(earningsLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void updatePetTable() {
        petTableModel.setRowCount(0);
        for (Pet pet : pets) {
            Object[] row = new Object[4];
            row[0] = pet.getName();
            row[1] = pet.getTypeDescription();
            row[2] = pet.getAge();

            if (pet instanceof Dog) {
                Dog dog = (Dog) pet;
                row[3] = dog.getBreed() + ", " + dog.getSize() + (dog.isAggressive() ? " (Aggressive)" : "");
            } else if (pet instanceof Cat) {
                Cat cat = (Cat) pet;
                row[3] = cat.getBreed() + (cat.isCastrated() ? " (Castrated)" : "");
            } else if (pet instanceof OtherPet) {
                OtherPet other = (OtherPet) pet;
                row[3] = other.getExactType();
            }
            petTableModel.addRow(row);
        }
    }

    private void updateTaskTable() {
        taskTableModel.setRowCount(0);
        for (Task task : tasks) {
            if (!task.isTaken()) {
                taskTableModel.addRow(new Object[] { 
                    task.getDescription(), 
                    task.getWorkingHours(),
                    task.getHourlyWage(), 
                    "Available" 
                });
            }
        }
    }
}