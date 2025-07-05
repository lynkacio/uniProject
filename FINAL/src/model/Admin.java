package model;

public class Admin extends Person {

	public Admin(String firstName, String lastName, Gender gender, String userId, String password) {
		super(firstName, lastName, gender, userId, password, Role.ADMIN);
	}

	@Override
	public String toString() {
		return "Admin{" + "firstName='" + getFirstName() + '\'' + ", lastName='" + getLastName() + '\'' + ", userId='"
				+ getUserId() + '\'' + +'}';
	}
}