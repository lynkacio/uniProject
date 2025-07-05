package model;

import java.util.ArrayList;

public class User extends Person {
    private ArrayList<Pet> pets;
    private ArrayList<Task> publishedTasks;

    public void setPets(ArrayList<Pet> pets) {
		this.pets = pets;
	}

	public void setPublishedTasks(ArrayList<Task> publishedTasks) {
		this.publishedTasks = publishedTasks;
	}

	public User(String firstName, String lastName, Gender gender, String userId, String password, Role role) {
        super(firstName, lastName, gender, userId, password, role);
        this.pets = new ArrayList<>();
        this.publishedTasks = new ArrayList<>();
    }

    // Pet management
    public ArrayList<Pet> getPets() {
        return pets;
    }

    public void addPet(Pet pet) {
        this.pets.add(pet);
    }

    public boolean removePet(Pet pet) {
        return this.pets.remove(pet);
    }

    // Task management
    public ArrayList<Task> getPublishedTasks() {
        return publishedTasks;
    }

    public Task createTask(String description, int workingHours, double hourlyWage) {
        Task task = new Task(this, description, workingHours, hourlyWage);
        this.publishedTasks.add(task);
        return task;
    }

    public boolean removeTask(Task task) {
        return this.publishedTasks.remove(task);
    }

    public double calculateTotalSpending() {
        return publishedTasks.stream()
                .filter(Task::isTaken)
                .mapToDouble(Task::calculateTotalPayment)
                .sum();
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName=" + fullName() +
                ", petCount=" + pets.size() +
                ", taskCount=" + publishedTasks.size() +
                '}';
    }
}