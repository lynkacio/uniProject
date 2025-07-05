package utils;

import model.*;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import java.io.*;

public class DataManagerForUser {
	private static final String PETS_FILE = "pets.txt";
	private static final String TASKS_FILE = "tasks.txt";

	// Load only pets belonging to the current user
	public static ArrayList<Pet> loadPets(User owner) {
		ArrayList<Pet> pets = new ArrayList<>();
		File file = new File(PETS_FILE);
		if (!file.exists()) {
			return pets;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean headerSkipped = false;
			while ((line = reader.readLine()) != null) {
				if (!headerSkipped) {
					headerSkipped = true;
					continue; // skip header
				}

				String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				if (parts.length < 5)
					continue;

				String ownerId = parts[3].trim();
				if (!ownerId.equals(owner.getUserId())) {
					continue; // Skip pets not belonging to current user
				}

				Pet.PetType type = Pet.PetType.valueOf(parts[0]);
				String name = parts[1];
				int age = Integer.parseInt(parts[2]);

				switch (type) {
				case DOG:
					if (parts.length >= 6) {
						Dog.Size size = Dog.Size.valueOf(parts[4]);
						boolean isAggressive = Boolean.parseBoolean(parts[5]);
						pets.add(new Dog(name, age, size, parts[4], isAggressive, owner));
					}
					break;
				case CAT:
					if (parts.length >= 6) {
						boolean isCastrated = Boolean.parseBoolean(parts[5]);
						String medicalNeeds = parts.length > 6 ? parts[6] : "";
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
		} catch (IOException | NumberFormatException e) {
			System.err.println("Error loading pets: " + e.getMessage());
		}
		return pets;
	}

	// Load only tasks created by the current user
	public static ArrayList<Task> loadTasks(User owner) {
		ArrayList<Task> tasks = new ArrayList<>();
		File file = new File(TASKS_FILE);
		if (!file.exists()) {
			return tasks;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean headerSkipped = false;
			while ((line = reader.readLine()) != null) {
				if (!headerSkipped) {
					headerSkipped = true;
					continue; // skip header
				}

				String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				if (parts.length < 5)
					continue;

				String creatorId = parts[4].trim();
				if (!creatorId.equals(owner.getUserId())) {
					continue; // Skip tasks not created by current user
				}

				try {
					String description = parts[0].replaceAll("^\"|\"$", "");
					int workingHours = Integer.parseInt(parts[1].trim());
					double hourlyWage = Double.parseDouble(parts[2].trim());
					boolean isTaken = Boolean.parseBoolean(parts[3].trim());

					Task task = new Task(owner, description, workingHours, hourlyWage);
					if (isTaken) {
						task.setTaken(); // Only set taken if the flag is true
					}
					tasks.add(task);
				} catch (NumberFormatException e) {
					System.err.println("Skipping malformed task line: " + line);
				}
			}
		} catch (IOException e) {
			System.err.println("Error loading tasks: " + e.getMessage());
		}
		return tasks;
	}

	// Save user's pets
	public static void savePets(User owner, ArrayList<Pet> userPets) {
	    // Load all users first to properly reconstruct pet owners
	    ArrayList<Person> allUsers = DataManager.loadUsersFromFile("users.txt");
	    
	    // Load all pets from file
	    ArrayList<Pet> allPets = new ArrayList<>();
	    File file = new File(PETS_FILE);
	    if (file.exists()) {
	        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	            String line;
	            boolean headerSkipped = false;
	            while ((line = reader.readLine()) != null) {
	                if (!headerSkipped) {
	                    headerSkipped = true;
	                    continue;
	                }

	                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                if (parts.length < 5)
	                    continue;

	                String ownerId = parts[3].trim();
	                if (ownerId.equals(owner.getUserId())) {
	                    continue; // Skip current user's pets (will be replaced)
	                }

	                // Find the owner in allUsers
	                Person petOwner = allUsers.stream()
	                    .filter(u -> u.getUserId().equals(ownerId))
	                    .findFirst()
	                    .orElse(null);

	                if (!(petOwner instanceof User)) continue;

	                // Reconstruct pet with proper owner
	                Pet.PetType type = Pet.PetType.valueOf(parts[0]);
	                String name = parts[1];
	                int age = Integer.parseInt(parts[2]);

	                switch (type) {
	                    case DOG:
	                        if (parts.length >= 6) {
	                            Dog.Size size = Dog.Size.valueOf(parts[4]);
	                            boolean isAggressive = Boolean.parseBoolean(parts[5]);
	                            allPets.add(new Dog(name, age, size, parts[4], isAggressive, (User) petOwner));
	                        }
	                        break;
	                    case CAT:
	                        if (parts.length >= 6) {
	                            boolean isCastrated = Boolean.parseBoolean(parts[5]);
	                            String medicalNeeds = parts.length > 6 ? parts[6] : "";
	                            allPets.add(new Cat(name, age, parts[4], isCastrated, medicalNeeds, (User) petOwner));
	                        }
	                        break;
	                    case OTHERS:
	                        if (parts.length >= 5) {
	                            String description = parts.length > 5 ? parts[5] : "";
	                            String careInstructions = parts.length > 6 ? parts[6] : "";
	                            allPets.add(new OtherPet(name, age, parts[4], description, careInstructions, (User) petOwner));
	                        }
	                        break;
	                }
	            }
	        } catch (IOException | NumberFormatException e) {
	            System.err.println("Error loading other pets: " + e.getMessage());
	        }
	    }

	    // Add current user's updated pets
	    allPets.addAll(userPets);

	    // Save all pets back to file
	    DataManager.saveUsersToFile("users.txt", allUsers);
	    DataManager.savePetsToFile(PETS_FILE, allPets);
	}

	public static void saveTasks(User owner, ArrayList<Task> userTasks) {
	    try (FileWriter fw = new FileWriter(TASKS_FILE, true);  // `true` enables append mode
	         PrintWriter writer = new PrintWriter(fw)) {

	        // If file is empty, write the header
	        if (new File(TASKS_FILE).length() == 0) {
	            writer.println("description,workingHours,hourlyWage,isTaken,creatorId");
	        }

	        // Append only the new/modified tasks
	        for (Task task : userTasks) {
	            writer.println(String.join(",",
	                "\"" + task.getDescription() + "\"",
	                String.valueOf(task.getWorkingHours()),
	                String.valueOf(task.getHourlyWage()),
	                String.valueOf(task.isTaken()),
	                owner.getUserId()
	            ));
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error saving tasks: " + e.getMessage(), 
	            "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
}