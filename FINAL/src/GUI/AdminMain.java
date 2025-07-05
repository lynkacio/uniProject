package GUI;

import model.*;
import utils.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.filechooser.FileNameExtensionFilter;

public class AdminMain extends JFrame {
	private ArrayList<Task> tasks = new ArrayList<>();
	private ArrayList<Person> allUsers = new ArrayList<>(); // All users in the file
	private ArrayList<Person> users = new ArrayList<>(); // only USER in "users.txt"
	private ArrayList<Helper> helpers = new ArrayList<>();
	private static final String CERTIFICATES_DIR = "certificates/";
	private static final String PETS_FILE = "pets.txt";
	private static final String TASKS_FILE = "tasks.txt";
	private static final String USERS_FILE = "users.txt";

	private DefaultTableModel tasksTableModel;
	private DefaultTableModel usersTableModel;
	private DefaultTableModel helpersTableModel;

	public AdminMain(Person admin) {
		// Initialize data manager and load all data
		DataManagerForAdmin dataManager = new DataManagerForAdmin();
		tasks = dataManager.loadTasks();

		// Load ALL users (including ADMIN and HELPER)
		allUsers = DataManager.loadUsersFromFile(USERS_FILE);
		users = allUsers.stream().filter(p -> p.getRole() == Person.Role.USER)
				.collect(Collectors.toCollection(ArrayList::new));
		helpers = allUsers.stream().filter(p -> p.getRole() == Person.Role.HELPER).map(p -> (Helper) p)
				.collect(Collectors.toCollection(ArrayList::new));

		// Then filter them into separate lists
		users = new ArrayList<>();
		helpers = new ArrayList<>();

		for (Person person : allUsers) {
			if (person.getRole() == Person.Role.USER) {
				users.add(person);
			} else if (person.getRole() == Person.Role.HELPER) {
				helpers.add((Helper) person);
			}
		}

		dataManager.loadPets(users);
		FileTransferManager.startFileReceiver();

		setTitle("Admin Dashboard - " + admin.fullName());
		setSize(1200, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Main panel with 3 columns
		JPanel mainPanel = new JPanel(new GridLayout(1, 3, 10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// 1. Left Column - Tasks
		JPanel tasksPanel = createTasksPanel();
		mainPanel.add(tasksPanel);

		// 2. Middle Column - Users
		JPanel usersPanel = createUsersPanel();
		mainPanel.add(usersPanel);

		// 3. Right Column - Helpers
		JPanel helpersPanel = createHelpersPanel();
		mainPanel.add(helpersPanel);

		getContentPane().add(mainPanel);

		// Create certificates directory if it doesn't exist
		new File(CERTIFICATES_DIR).mkdirs();

		// Add menu bar with certificate checking functionality
		JMenuBar menuBar = new JMenuBar();
		JMenu certificatesMenu = new JMenu("Certificates");
		JMenuItem checkCertificatesItem = new JMenuItem("Check for New Certificates");
		checkCertificatesItem.addActionListener(e -> checkForNewCertificates());
		certificatesMenu.add(checkCertificatesItem);
		menuBar.add(certificatesMenu);
		setJMenuBar(menuBar);

		// Start file receiver for certificates
		FileTransferManager.startFileReceiver();
	}

	private JPanel createTasksPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Tasks Management"));

		// Tasks table setup with all variables
		String[] columns = { "ID", "Description", "Hours", "Wage", "Status", "Creator ID", "Created At" };
		tasksTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		updateTasksTable();

		JTable tasksTable = new JTable(tasksTableModel);
		tasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Task control buttons
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(e -> {
			int selectedRow = tasksTable.getSelectedRow();
			if (selectedRow >= 0) {
				Task selectedTask = tasks.get(selectedRow);
				showTaskEditDialog(selectedTask);
				updateTasksTable();
				DataManagerForAdmin.saveTasks(tasks);
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			int selectedRow = tasksTable.getSelectedRow();
			if (selectedRow >= 0) {
				tasks.remove(selectedRow);
				updateTasksTable();
				DataManagerForAdmin.saveTasks(tasks);
			}
		});

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> {
			tasks = DataManagerForAdmin.loadTasks();
			updateTasksTable();
		});

		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(refreshButton);

		panel.add(new JScrollPane(tasksTable), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createUsersPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Users Management"));

		// Users table setup with all variables
		String[] columns = { "ID", "First Name", "Last Name", "Gender", "User ID", "Role", "Pets Count" };
		usersTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		updateUsersTable();

		JTable usersTable = new JTable(usersTableModel);
		usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// User control buttons
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(e -> {
			int selectedRow = usersTable.getSelectedRow();
			if (selectedRow >= 0) {
				Person selectedUser = users.get(selectedRow);
				showPersonEditDialog(selectedUser);
				updateUsersTable();
				DataManagerForAdmin.saveUsers(users);
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			int selectedRow = usersTable.getSelectedRow();
			if (selectedRow >= 0) {
				Person selectedUser = users.get(selectedRow);

				// Only delete from users
				users.remove(selectedRow);

				// Only delete duplicates from allUsers
				allUsers.removeIf(u -> u.getUserId().equals(selectedUser.getUserId()));

				updateUsersTable();
				// save all data
				DataManager.saveUsersToFile(USERS_FILE, allUsers);
			}
		});

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> {
			users = DataManagerForAdmin.loadUsers();
			updateUsersTable();
		});

		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(refreshButton);

		panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createHelpersPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Helpers Management"));

		// Helpers table setup with all variables
		String[] columns = { "ID", "First Name", "Last Name", "Gender", "User ID", "Credit Score", "Tasks Count",
				"Certified" };
		helpersTableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		updateHelpersTable();

		JTable helpersTable = new JTable(helpersTableModel);
		helpersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Helper control buttons
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(e -> {
			int selectedRow = helpersTable.getSelectedRow();
			if (selectedRow >= 0) {
				Helper selectedHelper = helpers.get(selectedRow);
				showHelperEditDialog(selectedHelper);
				updateHelpersTable();

				for (int i = 0; i < users.size(); i++) {
					if (users.get(i).getUserId().equals(selectedHelper.getUserId())) {
						users.set(i, selectedHelper);
						break;
					}
				}

				DataManagerForAdmin.saveUsers(users);
			}
		});

		JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            int selectedRow = helpersTable.getSelectedRow();
            if (selectedRow >= 0) {
                Helper selectedHelper = helpers.get(selectedRow);
                helpers.remove(selectedRow);
                
                // remove dupliactes from allUsers
                allUsers.removeIf(u -> u.getUserId().equals(selectedHelper.getUserId()));
                
                updateHelpersTable();
                // save all data
                DataManager.saveUsersToFile(USERS_FILE, allUsers);
            }
        });

		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(e -> {
			// Reload all users first
			ArrayList<Person> allUsers = DataManager.loadUsersFromFile(USERS_FILE);

			// Then filter helpers
			helpers.clear();
			for (Person person : allUsers) {
				if (person.getRole() == Person.Role.HELPER && person instanceof Helper) {
					helpers.add((Helper) person);
				}
			}
			updateHelpersTable();
		});

		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(refreshButton);

		// Certificate download button
		JButton downloadButton = new JButton("Download Certificate");
		downloadButton.addActionListener(e -> {
			int selectedRow = helpersTable.getSelectedRow();
			if (selectedRow >= 0) {
				Helper selected = helpers.get(selectedRow);
				if (selected.hasVolunteeringCertificate()) {
					// Changed to match the correct filename format: userID_certificate.pdf
					File certFile = new File(CERTIFICATES_DIR + selected.getUserId() + "_certificate.pdf");

					// Check if file exists before trying to download
					if (certFile.exists()) {
						downloadCertificate(certFile);
					} else {
						JOptionPane.showMessageDialog(this, "Certificate file not found: " + certFile.getName(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(this, "No certificate available for this helper", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JPanel southPanel = new JPanel(new BorderLayout());
		southPanel.add(buttonPanel, BorderLayout.NORTH);
		southPanel.add(downloadButton, BorderLayout.SOUTH);

		panel.add(new JScrollPane(helpersTable), BorderLayout.CENTER);
		panel.add(southPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void updateTasksTable() {
		tasksTableModel.setRowCount(0);
		for (int i = 0; i < tasks.size(); i++) {
			Task task = tasks.get(i);
			String creatorId = (task.getCreator() != null) ? task.getCreator().getUserId() : "Unknown";

			tasksTableModel.addRow(new Object[] { i + 1, task.getDescription(), task.getWorkingHours(),
					task.getHourlyWage(), task.isTaken() ? "Taken" : "Available", creatorId, "N/A" });
		}
	}

	private void updateUsersTable() {
		usersTableModel.setRowCount(0);
		Map<String, Integer> petCounts = DataManagerForAdmin.countPetsByOwner();

		for (int i = 0; i < users.size(); i++) {
			Person user = users.get(i);
			int petsCount = petCounts.getOrDefault(user.getUserId(), 0);

			usersTableModel.addRow(new Object[] { i + 1, user.getFirstName(), user.getLastName(), user.getGender(),
					user.getUserId(), user.getRole(), petsCount });
		}
	}

	private void updateHelpersTable() {
		System.out.println("Updating helpers table..."); // Debug output
		helpersTableModel.setRowCount(0);
		for (int i = 0; i < helpers.size(); i++) {
			Helper helper = helpers.get(i);
			System.out.println("Adding helper: " + helper.getFirstName() + " " + helper.getLastName()); // Debug output
			helpersTableModel.addRow(new Object[] { i + 1, helper.getFirstName(), helper.getLastName(),
					helper.getGender(), helper.getUserId(), String.format("%.1f", helper.getCreditScore()),
					helper.getAssignedTasks().size(), helper.hasVolunteeringCertificate() ? "Yes" : "No" });
		}
		System.out.println("Helpers table updated with " + helpers.size() + " entries"); // Debug output
	}

	private void showTaskEditDialog(Task task) {
		JDialog dialog = new JDialog(this, "Edit Task", true);
		dialog.setLayout(new GridLayout(0, 2));

		JTextField descField = new JTextField(task.getDescription());
		JTextField hoursField = new JTextField(String.valueOf(task.getWorkingHours()));
		JTextField wageField = new JTextField(String.valueOf(task.getHourlyWage()));

		dialog.add(new JLabel("Description:"));
		dialog.add(descField);
		dialog.add(new JLabel("Working Hours:"));
		dialog.add(hoursField);
		dialog.add(new JLabel("Hourly Wage:"));
		dialog.add(wageField);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			try {
				task.setDescription(descField.getText());
				task.setWorkingHours(Integer.parseInt(hoursField.getText()));
				task.setHourlyWage(Double.parseDouble(wageField.getText()));
				dialog.dispose();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "Invalid number format", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		dialog.add(saveButton);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void showPersonEditDialog(Person person) {
		JDialog dialog = new JDialog(this, "Edit User", true);
		dialog.setLayout(new GridLayout(0, 2));

		JTextField firstNameField = new JTextField(person.getFirstName());
		JTextField lastNameField = new JTextField(person.getLastName());
		JComboBox<Person.Gender> genderCombo = new JComboBox<>(Person.Gender.values());
		genderCombo.setSelectedItem(person.getGender());
		JTextField userIdField = new JTextField(person.getUserId());
		userIdField.setEditable(false);

		dialog.add(new JLabel("First Name:"));
		dialog.add(firstNameField);
		dialog.add(new JLabel("Last Name:"));
		dialog.add(lastNameField);
		dialog.add(new JLabel("Gender:"));
		dialog.add(genderCombo);
		dialog.add(new JLabel("User ID:"));
		dialog.add(userIdField);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			person.setFirstName(firstNameField.getText());
			person.setLastName(lastNameField.getText());
			person.setGender((Person.Gender) genderCombo.getSelectedItem());
			dialog.dispose();
		});

		dialog.add(saveButton);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void showHelperEditDialog(Helper helper) {
		JDialog dialog = new JDialog(this, "Edit Helper", true);
		dialog.setLayout(new GridLayout(0, 2));

		JTextField firstNameField = new JTextField(helper.getFirstName());
		JTextField lastNameField = new JTextField(helper.getLastName());
		JComboBox<Person.Gender> genderCombo = new JComboBox<>(Person.Gender.values());
		genderCombo.setSelectedItem(helper.getGender());
		JTextField userIdField = new JTextField(helper.getUserId());
		userIdField.setEditable(false);
		JTextField creditField = new JTextField(String.valueOf(helper.getCreditScore()));

		dialog.add(new JLabel("First Name:"));
		dialog.add(firstNameField);
		dialog.add(new JLabel("Last Name:"));
		dialog.add(lastNameField);
		dialog.add(new JLabel("Gender:"));
		dialog.add(genderCombo);
		dialog.add(new JLabel("User ID:"));
		dialog.add(userIdField);
		dialog.add(new JLabel("Credit Score:"));
		dialog.add(creditField);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			try {
				helper.setFirstName(firstNameField.getText());
				helper.setLastName(lastNameField.getText());
				helper.setGender((Person.Gender) genderCombo.getSelectedItem());
				helper.updateCreditScore(Double.parseDouble(creditField.getText()) - helper.getCreditScore());
				dialog.dispose();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "Invalid credit score", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		dialog.add(saveButton);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void checkForNewCertificates() {
		File[] certs = FileTransferManager.getReceivedCertificates();
		if (certs != null && certs.length > 0) {
			// Create a dialog with certificate list and download options
			JPanel panel = new JPanel(new BorderLayout());

			// List of certificates
			DefaultListModel<String> listModel = new DefaultListModel<>();
			for (File cert : certs) {
				listModel.addElement(cert.getName());
			}
			JList<String> certList = new JList<>(listModel);
			certList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			// Buttons panel
			JPanel buttonPanel = new JPanel();
			JButton viewButton = new JButton("View Certificate");
			JButton downloadButton = new JButton("Download");
			JButton deleteButton = new JButton("Delete");

			viewButton.addActionListener(e -> {
				int selectedIndex = certList.getSelectedIndex();
				if (selectedIndex >= 0) {
					viewCertificate(certs[selectedIndex]);
				} else {
					JOptionPane.showMessageDialog(this, "Please select a certificate first", "No Selection",
							JOptionPane.WARNING_MESSAGE);
				}
			});

			downloadButton.addActionListener(e -> {
				int selectedIndex = certList.getSelectedIndex();
				if (selectedIndex >= 0) {
					downloadCertificate(certs[selectedIndex]);
				} else {
					JOptionPane.showMessageDialog(this, "Please select a certificate first", "No Selection",
							JOptionPane.WARNING_MESSAGE);
				}
			});

			deleteButton.addActionListener(e -> {
				int selectedIndex = certList.getSelectedIndex();
				if (selectedIndex >= 0) {
					if (certs[selectedIndex].delete()) {
						listModel.remove(selectedIndex);
						JOptionPane.showMessageDialog(this, "Certificate deleted", "Success",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this, "Failed to delete certificate", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});

			buttonPanel.add(viewButton);
			buttonPanel.add(downloadButton);
			buttonPanel.add(deleteButton);

			panel.add(new JScrollPane(certList), BorderLayout.CENTER);
			panel.add(buttonPanel, BorderLayout.SOUTH);

			JOptionPane.showOptionDialog(this, panel, "Received Certificates", JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, new Object[] {}, null);
		} else {
			JOptionPane.showMessageDialog(this, "No new certificates available", "Certificates",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void viewCertificate(File certFile) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(certFile);
			} else {
				JOptionPane.showMessageDialog(this, "Desktop operations not supported on this platform", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error opening certificate: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void downloadCertificate(File certFile) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save Certificate As");
		fileChooser.setSelectedFile(new File(certFile.getName()));
		fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File destination = fileChooser.getSelectedFile();
			try {
				Files.copy(certFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				JOptionPane.showMessageDialog(this, "Certificate saved to: " + destination.getAbsolutePath(), "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "Error saving certificate: " + ex.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}