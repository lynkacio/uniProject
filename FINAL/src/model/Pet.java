package model;

public class Pet {
	private String name;
	private PetType type;
	private String otherType; // Only used when type is OTHERS
	private int age;
	public User owner;

	public User getOwner() {
		return owner;
	}

	public void setOwnerUser(User ownerUser) {
		this.owner = ownerUser;
	}

	// Enum for pet types
	public enum PetType {
		DOG, CAT, OTHERS
	}

	// Constructor for dog or cat
	public Pet(String name, PetType type, int age, User owner) {
		if (type == PetType.OTHERS) {
			throw new IllegalArgumentException("Use the other constructor for OTHERS pet type");
		}
		this.name = name;
		this.type = type;
		this.age = age;
		this.owner = owner;
	}

	// Constructor for other types
	public Pet(String name, String otherType, int age, User owner) {
		this.name = name;
		this.type = PetType.OTHERS;
		this.otherType = otherType;
		this.age = age;
		this.owner = owner;
	}

	// Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PetType getType() {
		return type;
	}

	public String getTypeDescription() {
		return type == PetType.OTHERS ? "Others: " + otherType : type.toString();
	}

	public void setType(PetType type) {
		if (type == PetType.OTHERS && this.otherType == null) {
			throw new IllegalStateException("Must specify otherType when setting type to OTHERS");
		}
		this.type = type;
	}

	public String getOtherType() {
		return otherType;
	}

	public void setOtherType(String otherType) {
		if (this.type == PetType.OTHERS) {
			this.otherType = otherType;
		}
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Pet{" + "name='" + name + '\'' + ", type=" + getTypeDescription() + ", age=" + age + '}';
	}
}