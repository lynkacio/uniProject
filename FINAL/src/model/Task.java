package model;

public class Task {
	private User creator;
	private String description;
	private boolean isTaken;
	private int workingHours; // in hours
	private double hourlyWage;
	private Helper assignedHelper;

	public Task(Person creator2, String description, int workingHours, double hourlyWage) {
		this.creator = (User) creator2;
		this.description = description;
		this.isTaken = false;
		this.workingHours = workingHours;
		this.hourlyWage = hourlyWage;
		this.assignedHelper = null;
	}

	// Getters and Setters
	public User getCreator() {
		return creator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isTaken() {
		return isTaken;
	}

	public void setTaken() {
		this.isTaken = true;
	}

	public void RevokeTaken() {
		this.isTaken = false;
	}

	public Helper getAssignedHelper() {
		return assignedHelper;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		this.workingHours = workingHours;
	}

	public double getHourlyWage() {
		return hourlyWage;
	}

	public void setHourlyWage(double hourlyWage) {
		this.hourlyWage = hourlyWage;
	}

	public double calculateTotalPayment() {
		return workingHours * hourlyWage;
	}

	@Override
	public String toString() {
		return "Task{" + "description='" + description + '\'' + ", workingHours=" + workingHours + ", hourlyWage="
				+ hourlyWage + ", isTaken=" + isTaken + '}';
	}

	public void assignHelper(Person helper) {
		this.assignedHelper = (Helper) helper;
	}
}