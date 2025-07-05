package model;

public class Cat extends Pet {
    private String breed;
    private boolean isCastrated;
    private String medicalNeeds;  // Long text description

    public Cat(String name, int age, String breed, boolean isCastrated, String medicalNeeds, User owner) {
        super(name, PetType.CAT, age, owner);
        this.breed = breed;
        this.isCastrated = isCastrated;
        this.medicalNeeds = medicalNeeds;
    }

    // Getters and Setters
    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public boolean isCastrated() {
        return isCastrated;
    }

    public void setCastrated(boolean castrated) {
        isCastrated = castrated;
    }

    public String getMedicalNeeds() {
        return medicalNeeds;
    }

    public void setMedicalNeeds(String medicalNeeds) {
        this.medicalNeeds = medicalNeeds;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "name='" + getName() + '\'' +
                ", type=" + getTypeDescription() +
                ", age=" + getAge() +
                ", breed='" + breed + '\'' +
                ", isCastrated=" + isCastrated +
                ", medicalNeeds='" + (medicalNeeds != null ? medicalNeeds.substring(0, Math.min(20, medicalNeeds.length())) + "..." : "none") + '\'' +
                '}';
    }
}