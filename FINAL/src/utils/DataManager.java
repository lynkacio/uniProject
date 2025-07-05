package utils;

import model.*;
import GUI.*;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

public class DataManager {
    private static final String PETS_FILE = "pets.txt";
    private static final String TASKS_FILE = "tasks.txt";

    // Load users from "user.txt"
    public static ArrayList<Person> loadUsersFromFile(String filename) {
        ArrayList<Person> people = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists())
            return people;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue; // Skip header
                }

                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handles commas in quotes

                if (parts.length < 6)
                    continue;

                String firstName = parts[0];
                String lastName = parts[1];
                Person.Gender gender = Person.Gender.valueOf(parts[2]);
                String userId = parts[3];
                String password = parts[4];
                Person.Role role = Person.Role.valueOf(parts[5]);
                switch (role) {
                case ADMIN:
                    people.add(new Admin(firstName, lastName, gender, userId, password));
                    break;
                case HELPER:
                    double creditScore = parts.length > 6 && !parts[6].isEmpty() ? 
                        Double.parseDouble(parts[6]) : 5.0;
                    boolean hasCert = parts.length > 7 && 
                        Boolean.parseBoolean(parts[7]);
                    Helper helper = new Helper(firstName, lastName, gender, userId, password, role);
                    helper.updateCreditScore(creditScore - 5.0);
                    helper.setVolunteeringCertificate(hasCert);
                    people.add(helper);
                    break;
                case USER:
                    people.add(new User(firstName, lastName, gender, userId, password, role));
                    break;
                default:
                    people.add(new Person(firstName, lastName, gender, userId, password, role));
            }
        
            }
        } catch (IOException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return people;
    }

    // Save users to "user.txt"
    public static void saveUsersToFile(String filename, ArrayList<Person> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("firstName,lastName,gender,userId,password,role,creditScore,hasVolunteeringCertificate,pets");

            for (Person person : users) {
                double creditScore = (person instanceof Helper) ? ((Helper) person).getCreditScore() : 0;
                boolean hasCert = (person instanceof Helper) ? ((Helper) person).hasVolunteeringCertificate() : false;
                
                writer.println(String.join(",",
                    person.getFirstName(),
                    person.getLastName(),
                    person.getGender().name(),
                    person.getUserId(),
                    person.getPassword(),
                    person.getRole().name(),
                    String.valueOf(creditScore),
                    String.valueOf(hasCert),
                    "" // pets field
                ));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving users: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

	// Read "pets.txt" and return an array list
	public static ArrayList<Pet> loadPetsFromFile(String filename, User owner) {
		ArrayList<Pet> pets = new ArrayList<>();
		File file = new File(PETS_FILE);
		if (!file.exists()) {
			return null;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean headerSkipped = false;
			while ((line = reader.readLine()) != null) {
				if (!headerSkipped) {
					headerSkipped = true;
					continue; // skip header
				}

				String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle quoted commas
				if (parts.length < 5)
					continue;

				Pet.PetType type = Pet.PetType.valueOf(parts[0]);
				String name = parts[1];
				int age = Integer.parseInt(parts[2]);
				String ownerId = parts[3];

				// Only load pets for current user
				if (!ownerId.equals(owner.getUserId()))
					continue;

				switch (type) {
				case DOG:
					if (parts.length >= 6) {
						Dog.Size size = Dog.Size.valueOf(parts[4]);
						boolean isAggressive = Boolean.parseBoolean(parts[5]);
						String medicalNeeds = parts.length > 6 ? parts[6] : "";
						String careInstructions = parts.length > 7 ? parts[7] : "";
						pets.add(new Dog(name, age, size, parts[4], isAggressive, owner));
					}
					break;
				case CAT:
					if (parts.length >= 6) {
						boolean isCastrated = Boolean.parseBoolean(parts[5]);
						String medicalNeeds = parts.length > 6 ? parts[6] : "";
						String careInstructions = parts.length > 7 ? parts[7] : "";
						pets.add(new Cat(name, age, parts[4], isCastrated, medicalNeeds, owner));
					}
					break;
				case OTHERS:
					if (parts.length >= 5) {
						String description = parts.length > 5 ? parts[5] : "";
						String careInstructions = parts.length > 6 ? parts[6] : "";
						pets.add(new OtherPet(name, age, parts[4], description, careInstructions, owner));
					}
					break;
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error loading pets: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		return pets;
	}

	// Save new pets in the file while remaining the old data
	public static void savePetsToFile(String filename, ArrayList<Pet> pets) {
		try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
			// Write header
			writer.println(
					"type,name,age,ownerId,breed/size,isCastrated/isAggressive,medicalNeeds/description,careInstructions");

			for (Pet pet : pets) {
				if (pet instanceof Dog) {
					Dog dog = (Dog) pet;
					writer.println(String.format("%s,%s,%d,%s,%s,%s,%s,%s", Pet.PetType.DOG.name(), dog.getName(),
							dog.getAge(), dog.getOwner().getUserId(), dog.getSize().name(), dog.isAggressive(), "", // medicalNeeds
																													// for
																													// dogs
																													// (empty
																													// in
																													// your
																													// data)
							"" // careInstructions for dogs (empty in your data)
					));
				} else if (pet instanceof Cat) {
					Cat cat = (Cat) pet;
					writer.println(String.format("%s,%s,%d,%s,%s,%s,%s,%s", Pet.PetType.CAT.name(), cat.getName(),
							cat.getAge(), cat.getOwner().getUserId(), cat.getBreed(), cat.isCastrated(),
							cat.getMedicalNeeds(), "" // careInstructions for cats (empty in your data)
					));
				} else if (pet instanceof OtherPet) {
					OtherPet other = (OtherPet) pet;
					writer.println(String.format("%s,%s,%d,%s,%s,%s,%s,%s", Pet.PetType.OTHERS.name(), other.getName(),
							other.getAge(), other.getOwner().getUserId(), other.getExactType(), "", // isCastrated/isAggressive
																									// (not applicable)
							other.getDescription(), other.getCareInstructions()));
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error saving pets: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	// Load tasks from "tasks.txt"
	public static ArrayList<Task> loadTasksFromFile(String filename, User owner) {
	    ArrayList<Task> tasks = new ArrayList<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader(TASKS_FILE))) {
	        String line;
	        boolean headerSkipped = false;
	        while ((line = reader.readLine()) != null) {
	            if (!headerSkipped) {
	                headerSkipped = true;
	                continue; // skip header
	            }

	            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	            if (parts.length < 4) continue;
	            
	            try {
	                String description = parts[0].replaceAll("^\"|\"$", "");
	                int workingHours = Integer.parseInt(parts[1].trim());
	                double hourlyWage = Double.parseDouble(parts[2].trim());
	                boolean isTaken = Boolean.parseBoolean(parts[3].trim());
	                
	                // for old files that does not have creatorID
	                String creatorId = (parts.length > 4) ? parts[4].trim() : owner.getUserId();
	                
	                Task task = new Task(owner, description, workingHours, hourlyWage);
	                task.setTaken();
	                tasks.add(task);
	            } catch (NumberFormatException e) {
	                System.err.println("Skipping malformed task line: " + line);
	            }
	        }
	    } catch (IOException e) {
	        System.out.println("No existing task file found, will create new one.");
	    }
	    return tasks;
	}

	// Save task info to "tasks.txt"
	public static void saveTasksToFile(String filename, ArrayList<Task> tasks) {
	    try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
	        writer.println("description,workingHours,hourlyWage,isTaken,creatorId");
	        
	        for (Task task : tasks) {
	            String creatorId = (task.getCreator() != null) ? task.getCreator().getUserId() : "Unknown";
	            writer.println(String.join(",",
	                "\"" + task.getDescription() + "\"", 
	                String.valueOf(task.getWorkingHours()),
	                String.valueOf(task.getHourlyWage()),
	                String.valueOf(task.isTaken()),
	                creatorId 
	            ));
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error saving tasks: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	}

}