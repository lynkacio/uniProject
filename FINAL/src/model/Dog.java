package model;

public class Dog extends Pet {
    public enum Size {
        SMALL, MEDIUM, LARGE, GIANT
    }

    private Size size;
    private String breed;
    private boolean isAggressive;

    public Dog(String name, int age, Size size, String breed, boolean isAggressive, User owner) {
        super(name, PetType.DOG, age, owner);
        this.size = size;
        this.breed = breed;
        this.isAggressive = isAggressive;
    }

    // Getters and Setters
    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public boolean isAggressive() {
        return isAggressive;
    }

    public void setAggressive(boolean aggressive) {
        isAggressive = aggressive;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "name='" + getName() + '\'' +
                ", type=" + getTypeDescription() +
                ", age=" + getAge() +
                ", size=" + size +
                ", breed='" + breed + '\'' +
                ", isAggressive=" + isAggressive +
                '}';
    }
}