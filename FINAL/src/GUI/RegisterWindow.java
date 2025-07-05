package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import model.*;
import model.Person.*;
import utils.DataManager;

public class RegisterWindow extends JFrame {

	private LoginWindow loginWindow;
	private ArrayList<Person> people;
	private static final String USER_DATA_FILE = "users.txt";

	public RegisterWindow(ArrayList<Person> people, LoginWindow loginWindow) {
		this.people = people;
		this.loginWindow = loginWindow;
		setTitle("Register");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(null);

		// First Name
		JLabel label = new JLabel("First Name:");
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setFont(new Font("Calibri", Font.PLAIN, 14));
		label.setBounds(10, 10, 98, 19);
		panel.add(label);
		JTextField firstNameField = new JTextField();
		firstNameField.setBounds(84, 0, 378, 29);
		panel.add(firstNameField);

		// Last Name
		JLabel label_1 = new JLabel("Last Name:");
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		label_1.setFont(new Font("Calibri", Font.PLAIN, 14));
		label_1.setBounds(10, 39, 98, 29);
		panel.add(label_1);
		JTextField lastNameField = new JTextField();
		lastNameField.setBounds(84, 39, 378, 29);
		panel.add(lastNameField);

		// Gender
		JLabel label_2 = new JLabel("Gender:");
		label_2.setHorizontalAlignment(SwingConstants.LEFT);
		label_2.setFont(new Font("Calibri", Font.PLAIN, 14));
		label_2.setBounds(10, 78, 98, 29);
		panel.add(label_2);
		JComboBox<Gender> genderCombo = new JComboBox<>(Gender.values());
		genderCombo.setFont(new Font("Calibri", Font.PLAIN, 14));
		genderCombo.setBounds(84, 78, 98, 29);
		panel.add(genderCombo);

		// User ID
		JLabel label_3 = new JLabel("User ID:");
		label_3.setHorizontalAlignment(SwingConstants.LEFT);
		label_3.setFont(new Font("Calibri", Font.PLAIN, 14));
		label_3.setBounds(10, 117, 98, 29);
		panel.add(label_3);
		JTextField userIdField = new JTextField();
		userIdField.setBounds(84, 117, 259, 29);
		panel.add(userIdField);

		// Password
		JLabel label_4 = new JLabel("Password:");
		label_4.setFont(new Font("Calibri", Font.PLAIN, 14));
		label_4.setBounds(10, 156, 98, 29);
		panel.add(label_4);
		JPasswordField passwordField = new JPasswordField();
		passwordField.setBounds(84, 156, 259, 29);
		panel.add(passwordField);

		// Role
		JLabel label_5 = new JLabel("Role:");
		label_5.setFont(new Font("Calibri", Font.PLAIN, 14));
		label_5.setBounds(10, 195, 98, 29);
		panel.add(label_5);
		JComboBox<Person.Role> roleCombo = new JComboBox<>(Person.Role.values());
		roleCombo.setFont(new Font("Calibri", Font.PLAIN, 14));
		roleCombo.setBounds(84, 195, 98, 29);
		panel.add(roleCombo);

		// Register Button
		JButton registerButton = new JButton("Register");
		registerButton.setFont(new Font("Calibri", Font.BOLD, 14));
		registerButton.setBounds(303, 222, 146, 29);
		registerButton.addActionListener(e -> {
			Person newPerson;
			Role selectedRole = (Role) roleCombo.getSelectedItem();

			switch (selectedRole) {
			case ADMIN:
				newPerson = new Admin(firstNameField.getText(), lastNameField.getText(),
						(Gender) genderCombo.getSelectedItem(), userIdField.getText(),
						new String(passwordField.getPassword()));
				break;
			case HELPER:
				Helper helper = new Helper(firstNameField.getText(), lastNameField.getText(),
						(Gender) genderCombo.getSelectedItem(), userIdField.getText(),
						new String(passwordField.getPassword()), selectedRole);
				helper.updateCreditScore(0); // Default credit score
				helper.setVolunteeringCertificate(false); // Default certificate status
				newPerson = helper;
				break;
			case USER:
				newPerson = new User(firstNameField.getText(), lastNameField.getText(),
						(Gender) genderCombo.getSelectedItem(), userIdField.getText(),
						new String(passwordField.getPassword()), selectedRole);
				break;
			default:
				newPerson = new Person(firstNameField.getText(), lastNameField.getText(),
						(Gender) genderCombo.getSelectedItem(), userIdField.getText(),
						new String(passwordField.getPassword()), selectedRole);
			}

			// Add the new person to the list
			people.add(newPerson);

			// Save to file
			DataManager.saveUsersToFile(USER_DATA_FILE, people);

			// Show success message
			JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

			// Close the registration window
			this.dispose();

			// Show the login window again
			loginWindow.setVisible(true);
		});

		panel.add(registerButton);

		getContentPane().add(panel, BorderLayout.CENTER);
	}

	// Static method to load users from file (can be called from LoginWindow)
	public static ArrayList<Person> loadUsersFromFile() {
		return DataManager.loadUsersFromFile(USER_DATA_FILE);
	}

	private void redirectToRegisterGUI() {
		// Close the login window and open register window
		this.dispose();
		new RegisterWindow(people, this.loginWindow).setVisible(true);
	}
}