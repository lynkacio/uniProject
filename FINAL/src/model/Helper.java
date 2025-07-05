package model;

import java.util.ArrayList;

public class Helper extends Person {
	private double creditScore;
	private boolean hasVolunteeringCertificate;
	private ArrayList<Task> assignedTasks;

	public Helper(String firstName, String lastName, Gender gender, String userId, String password, Role role) {
		super(firstName, lastName, gender, userId, password, role);
		this.creditScore = 5.0; // default value
		this.hasVolunteeringCertificate = false;
		this.assignedTasks = new ArrayList<>();
	}

	// Getters and Setters
	public double getCreditScore() {
		return creditScore;
	}

	public void updateCreditScore(double change) {
		this.creditScore = Math.max(0, Math.min(10, this.creditScore + change));
	}

	public boolean hasVolunteeringCertificate() {
		return hasVolunteeringCertificate;
	}

	public void setVolunteeringCertificate(boolean hasCertificate) {
		this.hasVolunteeringCertificate = hasCertificate;
	}

	public boolean canVolunteer() {
		return hasVolunteeringCertificate;
	}

	public ArrayList<Task> getAssignedTasks() {
		return assignedTasks;
	}

	public void assignTask(Task task) {
		if (canVolunteer()) {
			this.assignedTasks.add(task);
			task.assignHelper(this);
		} else {
			throw new IllegalStateException("Helper cannot volunteer without certificate");
		}
	}

	public double calculateTotalEarnings() {
		return assignedTasks.stream().mapToDouble(Task::calculateTotalPayment).sum();
	}

	@Override
	public String toString() {
		return "Helper{" + "fullName=" + fullName() + ", creditScore=" + creditScore + ", canVolunteer="
				+ canVolunteer() + '}';
	}
}
