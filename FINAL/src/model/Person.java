package model;

public class Person {
	// Fields
	private String firstName;
	private String lastName;
	private Gender gender;
	private String userId;
	private String password;
	private Role role;

	// Gender enum to restrict possible values
	public enum Gender {
		MALE, FEMALE, NOT_SPECIFIED
	}

	// Role enum - Added
	public enum Role {
		USER, HELPER, ADMIN
	}

	// Constructors
	public Person(String firstName, String lastName, Gender gender, String userId, String password, Role role) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.userId = userId;
		this.password = password;
		this.role = role;
	}

	// Default constructor
	public Person() {
		this("", "", Gender.NOT_SPECIFIED, "", "", Role.USER);
	}

	// Getters and Setters
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// Methods
	public String fullName() {
		return firstName + " " + lastName;
	}

	public boolean authenticate(String inputPassword) {
		return password.equals(inputPassword);
	}

	// Override methods
	@Override
	public String toString() {
		return "Person{" + "firstName='" + firstName + '\'' + ", lastName='" + lastName + '\'' + ", gender=" + gender
				+ ", userId='" + userId + '\'' + '}';
	}
}