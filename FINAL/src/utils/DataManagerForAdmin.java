package utils;

import model.*;
import model.Person.Gender;
import model.Person.Role;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

public class DataManagerForAdmin {
	private static final String TASKS_FILE = "tasks.txt";
	private static final String USERS_FILE = "users.txt";
	private static final String PETS_FILE = "pets.txt";

	public static ArrayList<Task> loadTasks() {
	    ArrayList<Task> tasks = new ArrayList<>();
	    File file = new File(TASKS_FILE);
	    if (!file.exists()) return tasks;

	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String line;
	        boolean headerSkipped = false;
	        while ((line = reader.readLine()) != null) {
	            if (!headerSkipped) {
	                headerSkipped = true;
	                continue;
	            }

	            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	            if (tokens.length < 4) continue;

	            try {
	                String description = tokens[0].replace("\"", "").trim();
	                int workingHours = Integer.parseInt(tokens[1].trim());
	                double hourlyWage = Double.parseDouble(tokens[2].trim());
	                boolean isTaken = Boolean.parseBoolean(tokens[3].trim());
	                String creatorId = (tokens.length > 4) ? tokens[4].trim() : "Unknown";
	                
	                Task task = new Task(null, description, workingHours, hourlyWage);
	                if (isTaken) task.setTaken();
	                tasks.add(task);
	            } catch (Exception e) {
	                System.err.println("Error parsing task: " + line);
	            }
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error loading tasks: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	    return tasks;
	}

	public static ArrayList<Person> loadUsers() {
	    ArrayList<Person> users = new ArrayList<>();
	    File file = new File(USERS_FILE);
	    if (!file.exists()) {
	        return users;
	    }

	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String line;
	        boolean headerSkipped = false;
	        while ((line = reader.readLine()) != null) {
	            if (!headerSkipped) {
	                headerSkipped = true;
	                continue;
	            }

	            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	            if (parts.length < 6)
	                continue;

	            String firstName = parts[0];
	            String lastName = parts[1];
	            Person.Gender gender = Person.Gender.valueOf(parts[2]);
	            String userId = parts[3];
	            String password = parts[4];
	            Person.Role role = Person.Role.valueOf(parts[5]);

	            // Only create and add USER role persons
	            if (role == Person.Role.USER) {
	                User user = new User(firstName, lastName, gender, userId, password, role);
	                users.add(user);
	            }
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	    return users;
	}

	public static void loadPets(ArrayList<Person> users) {
	    File file = new File(PETS_FILE);
	    if (!file.exists()) {
	        return;
	    }

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

	            String type = parts[0];
	            String name = parts[1];
	            int age = Integer.parseInt(parts[2]);
	            String ownerId = parts[3];
	            String breedOrSize = parts[4];

	            // Find the owner (already guaranteed to be USER role from loadUsers)
	            Person owner = findUserById(ownerId, users);
	            if (owner == null || !(owner instanceof User))
	                continue;

	            Pet pet = null;
	            switch (type) {
	                case "CAT":
	                    boolean isCastrated = parts.length > 5 ? Boolean.parseBoolean(parts[5]) : false;
	                    String medicalNeeds = parts.length > 6 ? parts[6] : "";
	                    pet = new Cat(name, age, breedOrSize, isCastrated, medicalNeeds, (User) owner);
	                    break;
	                case "DOG":
	                    Dog.Size size = Dog.Size.valueOf(breedOrSize);
	                    boolean isAggressive = parts.length > 5 ? Boolean.parseBoolean(parts[5]) : false;
	                    pet = new Dog(name, age, size, breedOrSize, isAggressive, (User) owner);
	                    break;
	                case "OTHER":
	                    String description = parts.length > 5 ? parts[5] : "";
	                    String careInstructions = parts.length > 6 ? parts[6] : "";
	                    pet = new OtherPet(name, age, breedOrSize, description, careInstructions, (User) owner);
	                    break;
	            }

	            if (pet != null) {
	                ((User) owner).addPet(pet);
	            }
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error loading pets: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	}

	public static ArrayList<Helper> loadHelpers(ArrayList<Person> allUsers) {
	    ArrayList<Helper> helpers = new ArrayList<>();
	    for (Person person : allUsers) {
	        if (person.getRole() == Person.Role.HELPER && person instanceof Helper) {
	            helpers.add((Helper) person);
	        }
	    }
	    return helpers;
	}

	public static Person findUserById(String userId, ArrayList<Person> users) {
		for (Person user : users) {
			if (user.getUserId().equals(userId)) {
				return user;
			}
		}
		return null;
	}

	public static void saveTasks(ArrayList<Task> tasks) {
	    try (PrintWriter writer = new PrintWriter(new FileWriter(TASKS_FILE))) {
	        writer.println("description,workingHours,hourlyWage,isTaken,creatorId");  
	        for (Task task : tasks) {
	            String creatorId = (task.getCreator() != null) ? task.getCreator().getUserId() : "Unknown";
	            writer.println(String.format("\"%s\",%d,%.2f,%b,%s", 
	                task.getDescription(), 
	                task.getWorkingHours(),
	                task.getHourlyWage(), 
	                task.isTaken(),
	                creatorId));  
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error saving tasks: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	}

	public static void saveUsers(ArrayList<Person> allUsers) {
	    try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
	        writer.println("firstName,lastName,gender,userId,password,role,creditScore,hasVolunteeringCertificate");
	        
	        for (Person person : allUsers) {
	            double creditScore = (person instanceof Helper) ? 
	                ((Helper) person).getCreditScore() : 0;
	            boolean hasCert = (person instanceof Helper) ? 
	                ((Helper) person).hasVolunteeringCertificate() : false;
	                
	            writer.println(String.join(",",
	                person.getFirstName(),
	                person.getLastName(),
	                person.getGender().name(),
	                person.getUserId(),
	                person.getPassword(),
	                person.getRole().name(),
	                String.valueOf(creditScore),
	                String.valueOf(hasCert)
	            ));
	        }
	    } catch (IOException e) {
	    }
	}
	
	public static Map<String, Integer> countPetsByOwner() {
	    Map<String, Integer> petCounts = new HashMap<>();
	    File file = new File(PETS_FILE);
	    
	    if (!file.exists()) {
	        return petCounts;
	    }

	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        String line;
	        boolean headerSkipped = false;
	        
	        while ((line = reader.readLine()) != null) {
	            if (!headerSkipped) {
	                headerSkipped = true;
	                continue;
	            }

	            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	            if (parts.length < 4) continue;

	            String ownerId = parts[3];
	            petCounts.put(ownerId, petCounts.getOrDefault(ownerId, 0) + 1);
	        }
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(null, "Error counting pets: " + e.getMessage(), "Error",
	                JOptionPane.ERROR_MESSAGE);
	    }
	    
	    return petCounts;
	}
}