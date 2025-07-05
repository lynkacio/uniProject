package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import model.*;
import model.Person.*;

public class LoginWindow extends JFrame {

	private JTextField userIdField;
	private JPasswordField passwordField;
	private JTextField captchaField;
	private JLabel captchaLabel;
	private String currentCaptcha;
	private static ArrayList<Person> people = RegisterWindow.loadUsersFromFile();

	public LoginWindow() {
		setTitle("Login");
		setSize(561, 250);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		// Create panel
		JPanel panel = new JPanel();
		panel.setLayout(null);

		// User ID components
		JLabel label = new JLabel("User ID:");
		label.setBounds(0, 10, 93, 38);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(new Font("Calibri", Font.PLAIN, 16));
		panel.add(label);
		userIdField = new JTextField();
		userIdField.setBounds(90, 10, 385, 33);
		panel.add(userIdField);

		// Password components
		JLabel label_1 = new JLabel("Password:");
		label_1.setBounds(0, 48, 93, 47);
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setFont(new Font("Calibri", Font.PLAIN, 16));
		panel.add(label_1);
		passwordField = new JPasswordField();
		passwordField.setBounds(90, 58, 385, 33);
		panel.add(passwordField);

		// CAPTCHA components
		JLabel captchaTextLabel = new JLabel("CAPTCHA:");
		captchaTextLabel.setBounds(0, 96, 93, 47);
		captchaTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
		captchaTextLabel.setFont(new Font("Calibri", Font.PLAIN, 16));
		panel.add(captchaTextLabel);

		captchaField = new JTextField();
		captchaField.setBounds(90, 106, 200, 33);
		panel.add(captchaField);

		captchaLabel = new JLabel();
		captchaLabel.setHorizontalAlignment(SwingConstants.CENTER);
		captchaLabel.setBounds(300, 106, 150, 33);
		captchaLabel.setFont(new Font("Baskerville Old Face", Font.BOLD, 23));
		captchaLabel.setForeground(Color.BLUE);
		captchaLabel.setBackground(Color.LIGHT_GRAY);
		captchaLabel.setOpaque(true);
		panel.add(captchaLabel);

		JButton refreshCaptchaButton = new JButton("Refresh");
		refreshCaptchaButton.setFont(new Font("Calibri", Font.BOLD, 14));
		refreshCaptchaButton.setBounds(460, 106, 80, 33);
		refreshCaptchaButton.addActionListener(e -> generateCaptcha());
		panel.add(refreshCaptchaButton);

		// Buttons
		JButton registerButton = new JButton("Register");
		registerButton.setBounds(10, 160, 111, 33);
		registerButton.setFont(new Font("Calibri", Font.BOLD, 13));
		registerButton.addActionListener(e -> redirectToRegisterGUI());
		panel.add(registerButton);

		JButton loginButton = new JButton("Login");
		loginButton.setBounds(427, 163, 111, 33);
		loginButton.setFont(new Font("Calibri", Font.BOLD, 13));
		panel.add(loginButton);

		getContentPane().add(panel, BorderLayout.CENTER);

		loginButton.addActionListener(e -> handleLogin());

		// Generate initial CAPTCHA
		generateCaptcha();

		setVisible(true);
	}

	private void generateCaptcha() {
		// Generate a random 6-character CAPTCHA
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder captcha = new StringBuilder();
		Random rnd = new Random();
		while (captcha.length() < 6) {
			int index = (int) (rnd.nextFloat() * chars.length());
			captcha.append(chars.charAt(index));
		}
		currentCaptcha = captcha.toString();
		captchaLabel.setText(currentCaptcha);
	}

	private void redirectToRegisterGUI() {
		// Close the login window and open register window
		this.dispose();
		new RegisterWindow(people, this).setVisible(true);
	}

	private void handleLogin() {
	    String inputId = userIdField.getText().trim();
	    String inputPassword = new String(passwordField.getPassword());
	    String inputCaptcha = captchaField.getText().trim();

	    // Validate inputs
	    if (inputId.isEmpty() || inputPassword.isEmpty() || inputCaptcha.isEmpty()) {
	        JOptionPane.showMessageDialog(this, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
	        return;
	    }

	    // Verify CAPTCHA
	    if (!inputCaptcha.equalsIgnoreCase(currentCaptcha)) {
	        JOptionPane.showMessageDialog(this, "Invalid CAPTCHA", "Error", JOptionPane.ERROR_MESSAGE);
	        generateCaptcha();
	        return;
	    }

	    // Reload users in case of updates
	    people = RegisterWindow.loadUsersFromFile();
	    
	    // Find user by ID (case insensitive)
	    Optional<Person> userOptional = people.stream()
	        .filter(p -> p.getUserId().equalsIgnoreCase(inputId))
	        .findFirst();

	    if (!userOptional.isPresent()) {
	        JOptionPane.showMessageDialog(this, "User not found", "Error", JOptionPane.ERROR_MESSAGE);
	        generateCaptcha();
	        return;
	    }

	    Person user = userOptional.get();
	    
	    // Verify password (use password hashing in real application)
	    if (!user.getPassword().equals(inputPassword)) {
	        JOptionPane.showMessageDialog(this, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
	        generateCaptcha();
	        return;
	    }

	    // Redirect based on role
	    switch (user.getRole()) {
	        case USER:
	            redirectToUserGUI(user);
	            break;
	        case ADMIN:
	            redirectToAdminGUI(user);
	            break;
	        case HELPER:
	            redirectToHelperGUI(user);
	            break;
	        default:
	            JOptionPane.showMessageDialog(this, "Unknown user role", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}

	private void redirectToUserGUI(Person p) {
		// Close the login window and open register window
		this.dispose();
		new UserMain(p).setVisible(true);
	}

	private void redirectToAdminGUI(Person p) {
		// Close the login window and open register window
		this.dispose();
		new AdminMain(p).setVisible(true);
	}

	private void redirectToHelperGUI(Person p) {
		// Close the login window and open register window
		this.dispose();
		new HelperMain(p).setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(LoginWindow::new);
	}
}