package utils;

import model.*;
import GUI.*;
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

public class DataManagerForHelper {
	private static final String PETS_FILE = "pets.txt";
	private static final String TASKS_FILE = "tasks.txt";
	
	public static ArrayList<Pet> loadPetsForHelpers(String filename) {
	    ArrayList<Pet> pets = new ArrayList<>();
	    File file = new File(filename);
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
	            if (parts.length < 5) continue;

	            Pet.PetType type = Pet.PetType.valueOf(parts[0]);
	            String name = parts[1];
	            int age = Integer.parseInt(parts[2]);
	            String ownerId = parts[3];

	            // For helpers, we load all pets (not filtering by owner)
	            switch (type) {
	                case DOG:
	                    if (parts.length >= 6) {
	                        Dog.Size size = Dog.Size.valueOf(parts[4]);
	                        boolean isAggressive = Boolean.parseBoolean(parts[5]);
	                        pets.add(new Dog(name, age, size, parts[4], isAggressive, null));
	                    }
	                    break;
	                case CAT:
	                    if (parts.length >= 6) {
	                        boolean isCastrated = Boolean.parseBoolean(parts[5]);
	                        String medicalNeeds = parts.length > 6 ? parts[6] : "";
	                        pets.add(new Cat(name, age, parts[4], isCastrated, medicalNeeds, null));
	                    }
	                    break;
	                case OTHERS:
	                    if (parts.length >= 5) {
	                        String description = parts.length > 5 ? parts[5] : "";
	                        String careInstructions = parts.length > 6 ? parts[6] : "";
	                        pets.add(new OtherPet(name, age, parts[4], description, careInstructions, null));
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

	public static ArrayList<Task> loadTasksFromFileForHelper(String filename) {
	    ArrayList<Task> tasks = new ArrayList<>();
	    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
	        String line;
	        boolean headerSkipped = false;
	        while ((line = reader.readLine()) != null) {
	            if (!headerSkipped) {
	                headerSkipped = true;
	                continue;
	            }

	            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	            if (parts.length < 4) continue;

	            try {
	                String description = parts[0].replaceAll("^\"|\"$", "").trim();
	                int workingHours = Integer.parseInt(parts[1].trim());
	                double hourlyWage = Double.parseDouble(parts[2].trim());
	                boolean isTaken = Boolean.parseBoolean(parts[3].trim());
	                String creatorId = (parts.length > 4) ? parts[4].trim() : "Unknown";
	                
	                Task task = new Task(null, description, workingHours, hourlyWage);
	                if (isTaken) task.setTaken();
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
}
