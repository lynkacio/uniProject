package model;

public class OtherPet extends Pet {
	private String description; // Free-form description of the pet
	private String careInstructions; // Special care requirements

	public OtherPet(String name, int age, String exactType, String description, String careInstructions, User owner) {
		super(name, exactType, age, owner); // Uses Pet's constructor for OTHERS type
		this.description = description;
		this.careInstructions = careInstructions;
	}

	// Getters and Setters
	public String getExactType() {
		return getOtherType(); // Inherited from Pet
	}

	public void setExactType(String exactType) {
		setOtherType(exactType); // Uses Pet's setter with validation
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCareInstructions() {
		return careInstructions;
	}

	public void setCareInstructions(String careInstructions) {
		this.careInstructions = careInstructions;
	}

	@Override
	public String getTypeDescription() {
		return "Exotic: " + super.getTypeDescription(); // Adds "Exotic" prefix
	}

	@Override
	public String toString() {
		return "OtherPet{" + "name='" + getName() + '\'' + ", type=" + getTypeDescription() + ", age=" + getAge()
				+ ", description='" + truncateText(description, 30) + '\'' + ", careInstructions='"
				+ truncateText(careInstructions, 30) + '\'' + '}';
	}

	// Helper method to truncate long text for display
	private String truncateText(String text, int maxLength) {
		if (text == null)
			return "none";
		return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
	}
}