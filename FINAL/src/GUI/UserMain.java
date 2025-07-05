package GUI;

import model.*;
import utils.*;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class UserMain extends JFrame {
	private User currentUser;
	private ArrayList<Pet> pets = new ArrayList<>();
	private ArrayList<Task> tasks = new ArrayList<>();
	private static final String PETS_FILE = "pets.txt";
	private static final String TASKS_FILE = "tasks.txt";

	private DefaultTableModel petTableModel;
	private DefaultTableModel taskTableModel;
	private JTable petTable;
	private JTable taskTable;

	public UserMain(Person loggedInUser) {
		if (!(loggedInUser instanceof User)) {
			JOptionPane.showMessageDialog(null, "Invalid user type");
			System.exit(1);
		}
		this.currentUser = (User) loggedInUser;

		// Initialize UI Components
		setTitle("Pet Care System - " + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
		setSize(900, 650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(10, 10));

		// Load data - pass currentUser as the owner
        this.pets = DataManagerForUser.loadPets(currentUser);
        this.tasks = DataManagerForUser.loadTasks(currentUser);
		// Main panels
		JPanel upperPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		JPanel lowerPanel = new JPanel(new BorderLayout());

		// 1. User Information Panel
		JPanel userPanel = createUserPanel();

		// 2. Pets Panel with Table
		JPanel petPanel = createPetPanel();

		// 3. Tasks Panel with Table
		JPanel taskPanel = createTaskPanel();

		// Add components to frame
		upperPanel.add(userPanel);
		upperPanel.add(petPanel);
		lowerPanel.add(taskPanel, BorderLayout.CENTER);

		getContentPane().add(upperPanel, BorderLayout.NORTH);
		getContentPane().add(lowerPanel, BorderLayout.CENTER);

		// Window listener for saving data
		addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataManagerForUser.savePets(currentUser, pets);
                DataManagerForUser.saveTasks(currentUser, tasks);
            }
        });
	}

	private JPanel createUserPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("User Information"));
		panel.setBackground(new Color(240, 240, 240));

		// Create styled text area
		JTextArea userInfo = new JTextArea();
		userInfo.setEditable(false);
		userInfo.setBackground(new Color(240, 240, 240));
		userInfo.setFont(new Font("Arial", Font.PLAIN, 14));
		userInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Format user information beautifully
		String info = String.format(
				"╔════════════════════════════════════╗\n" + "║ %-34s ║\n" + "╠════════════════════════════════════╣\n"
						+ "║ First Name: %-20s ║\n" + "║ Last Name:  %-20s ║\n" + "║ User ID:    %-20s ║\n"
						+ "║ Role:       %-20s ║\n" + "╚════════════════════════════════════╝",
				"User Profile", currentUser.getFirstName(), currentUser.getLastName(), currentUser.getUserId(),
				currentUser.getRole());

		userInfo.setText(info);
		panel.add(new JScrollPane(userInfo), BorderLayout.CENTER);

		return panel;
	}

	private JPanel createPetPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Pets"));

		// Pet table setup
		String[] petColumns = { "Name", "Type", "Age", "Details" };
		petTableModel = new DefaultTableModel(petColumns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		petTable = new JTable(petTableModel);
		petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		updatePetTable();

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("Add Pet");
		addButton.addActionListener(e -> showAddPetDialog(null));
		JButton deleteButton = new JButton("Delete Pet");
		deleteButton.addActionListener(e -> {
            int selectedRow = petTable.getSelectedRow();
            if (selectedRow >= 0) {
                pets.remove(selectedRow);
                updatePetTable();
                DataManagerForUser.savePets(currentUser, pets);  // Changed to DataManagerForUser
            }
		});

		// Add components
		panel.add(new JScrollPane(petTable), BorderLayout.CENTER);
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel createTaskPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Tasks"));

		// Task table setup
		String[] taskColumns = { "Description", "Hours", "Wage", "Status" };
		taskTableModel = new DefaultTableModel(taskColumns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		taskTable = new JTable(taskTableModel);
		taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		updateTaskTable();

		// Buttons panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton addButton = new JButton("Add Task");
		addButton.addActionListener(e -> showAddTaskDialog(null));

		JButton deleteButton = new JButton("Delete Task");
		deleteButton.addActionListener(e -> {
			int selectedRow = taskTable.getSelectedRow();
			if (selectedRow >= 0) {
				Task selectedTask = tasks.get(selectedRow);
				if (selectedTask.getCreator().getUserId().equals(currentUser.getUserId())) {
					tasks.remove(selectedRow);
					updateTaskTable();
					DataManagerForUser.saveTasks(currentUser, tasks);
				} else {
					JOptionPane.showMessageDialog(null, "You can only delete tasks you created.", "Access Denied",
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Please select a task to delete", "No Selection",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		// Add components
		panel.add(new JScrollPane(taskTable), BorderLayout.CENTER);
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private void showAddPetDialog(DefaultListModel<Pet> listModel) {
		JDialog dialog = new JDialog(this, "Add New Pet", true);
		dialog.getContentPane().setLayout(new GridLayout(0, 2));

		JComboBox<Pet.PetType> typeCombo = new JComboBox<>(Pet.PetType.values());
		JTextField nameField = new JTextField();
		JTextField ageField = new JTextField();

		// Dynamic fields panel
		JPanel dynamicFields = new JPanel(new GridLayout(0, 2));

		typeCombo.addActionListener(e -> {
			dynamicFields.removeAll();
			Pet.PetType selected = (Pet.PetType) typeCombo.getSelectedItem();

			switch (selected) {
			case DOG:
				dynamicFields.add(new JLabel("Size:"));
				JComboBox<Dog.Size> sizeCombo = new JComboBox<>(Dog.Size.values());
				dynamicFields.add(sizeCombo);

				dynamicFields.add(new JLabel("Breed:"));
				JTextField dogBreedField = new JTextField();
				dynamicFields.add(dogBreedField);

				dynamicFields.add(new JLabel("Aggressive:"));
				JCheckBox aggressiveCheck = new JCheckBox();
				dynamicFields.add(aggressiveCheck);
				break;

			case CAT:
				dynamicFields.add(new JLabel("Breed:"));
				JTextField catBreedField = new JTextField();
				dynamicFields.add(catBreedField);

				dynamicFields.add(new JLabel("Castrated:"));
				JCheckBox castratedCheck = new JCheckBox();
				dynamicFields.add(castratedCheck);

				dynamicFields.add(new JLabel("Medical Needs:"));
				JTextArea medicalNeedsArea = new JTextArea(3, 20);
				dynamicFields.add(new JScrollPane(medicalNeedsArea));
				break;

			case OTHERS:
				dynamicFields.add(new JLabel("Type:"));
				JTextField otherTypeField = new JTextField();
				dynamicFields.add(otherTypeField);

				dynamicFields.add(new JLabel("Description:"));
				JTextArea descriptionArea = new JTextArea(3, 20);
				dynamicFields.add(new JScrollPane(descriptionArea));

				dynamicFields.add(new JLabel("Care Instructions:"));
				JTextArea careInstructionsArea = new JTextArea(3, 20);
				dynamicFields.add(new JScrollPane(careInstructionsArea));
				break;
			}
			dialog.pack();
		});

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			try {
				String name = nameField.getText();
				int age = Integer.parseInt(ageField.getText());
				Pet.PetType type = (Pet.PetType) typeCombo.getSelectedItem();
				Pet newPet = null;

				switch (type) {
				case DOG:
					JComboBox<Dog.Size> sizeCombo = (JComboBox) dynamicFields.getComponent(1);
					JTextField dogBreedField = (JTextField) dynamicFields.getComponent(3);
					JCheckBox aggressiveCheck = (JCheckBox) dynamicFields.getComponent(5);

					newPet = new Dog(name, age, (Dog.Size) sizeCombo.getSelectedItem(), dogBreedField.getText(),
							aggressiveCheck.isSelected(), currentUser);
					break;

				case CAT:
					JTextField catBreedField = (JTextField) dynamicFields.getComponent(1);
					JCheckBox castratedCheck = (JCheckBox) dynamicFields.getComponent(3);
					JTextArea medicalNeedsArea = (JTextArea) ((JScrollPane) dynamicFields.getComponent(5)).getViewport()
							.getView();

					newPet = new Cat(name, age, catBreedField.getText(), castratedCheck.isSelected(),
							medicalNeedsArea.getText(), currentUser);
					break;

				case OTHERS:
					JTextField otherTypeField = (JTextField) dynamicFields.getComponent(1);
					JTextArea descriptionArea = (JTextArea) ((JScrollPane) dynamicFields.getComponent(3)).getViewport()
							.getView();
					JTextArea careInstructionsArea = (JTextArea) ((JScrollPane) dynamicFields.getComponent(5))
							.getViewport().getView();

					newPet = new OtherPet(name, age, otherTypeField.getText(), descriptionArea.getText(),
							careInstructionsArea.getText(), currentUser);
					break;
				}
				pets.add(newPet);
				updatePetTable();
				DataManagerForUser.savePets(currentUser, pets);
				dialog.dispose();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "Please enter valid age!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		dialog.getContentPane().add(new JLabel("Type:"));
		dialog.getContentPane().add(typeCombo);
		dialog.getContentPane().add(new JLabel("Name:"));
		dialog.getContentPane().add(nameField);
		dialog.getContentPane().add(new JLabel("Age:"));
		dialog.getContentPane().add(ageField);
		dialog.getContentPane().add(dynamicFields);
		dialog.getContentPane().add(saveButton);

		dialog.pack();
		dialog.setVisible(true);
	}

	private void showAddTaskDialog(DefaultListModel<Task> listModel) {
		JDialog dialog = new JDialog(this, "Add New Task", true);
		dialog.getContentPane().setLayout(new GridLayout(0, 2));

		JTextArea descriptionArea = new JTextArea(3, 20);
		JTextField hoursField = new JTextField();
		JTextField wageField = new JTextField();

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			try {
				String description = descriptionArea.getText();
				int hours = Integer.parseInt(hoursField.getText());
				double wage = Double.parseDouble(wageField.getText());

				Task newTask = new Task(currentUser, description, hours, wage);
				tasks.add(newTask);
				updateTaskTable();
				DataManagerForUser.saveTasks(currentUser, tasks);
				dialog.dispose();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(dialog, "Please enter valid numbers!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		dialog.getContentPane().add(new JLabel("Description:"));
		dialog.getContentPane().add(new JScrollPane(descriptionArea));
		dialog.getContentPane().add(new JLabel("Working Hours:"));
		dialog.getContentPane().add(hoursField);
		dialog.getContentPane().add(new JLabel("Hourly Wage:"));
		dialog.getContentPane().add(wageField);
		dialog.getContentPane().add(saveButton);

		dialog.pack();
		dialog.setVisible(true);
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
			taskTableModel.addRow(new Object[] { task.getDescription(), task.getWorkingHours(), task.getHourlyWage(),
					task.isTaken() ? "Taken" : "Available" });
		}
	}
}