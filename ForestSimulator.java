import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.io.File;

/**
 * Entry point of the program.
 */
public class ForestSimulator {

    // Constants
    private static final float MIN_DAYS = Constants.MIN_DAYS.getValue();
    private static final float MAX_DAYS = Constants.MAX_DAYS.getValue();
    private static final float MIN_NO_ANIMALS = Constants.MIN_NO_ANIMALS.getValue();
    private static final float MAX_NO_ANIMALS = Constants.MAX_NO_ANIMALS.getValue();
    private static final float MIN_AMOUNT_GRASS = Constants.MIN_AMOUNT_GRASS.getValue();
    private static final float MAX_AMOUNT_GRASS = Constants.MAX_AMOUNT_GRASS.getValue();
    private static final float MIN_WEIGHT = Constants.MIN_WEIGHT.getValue();
    private static final float MAX_WEIGHT = Constants.MAX_WEIGHT.getValue();
    private static final float MIN_SPEED = Constants.MIN_SPEED.getValue();
    private static final float MAX_SPEED = Constants.MAX_SPEED.getValue();
    private static final float MAX_ENERGY = Constants.MAX_ENERGY.getValue();
    private static final float MIN_ENERGY = Constants.MIN_ENERGY.getValue();

    // Encapsulated Variables
    private static int days;
    private static float grassAmount;
    private static int noAnimals;

    /**
     * Gets the number of simulation days.
     * @return the number of days.
     */
    public static int getDays() {
        return days;
    }

    /**
     * Sets the number of simulation days.
     * @param days the number of days.
     */
    public static void setDays(int days) {
        Main.days = days;
    }

    /**
     * Gets the grass amount.
     * @return the grass amount.
     */
    public static float getGrassAmount() {
        return grassAmount;
    }

    /**
     * Sets the grass amount.
     * @param grassAmount the grass amount.
     */
    public static void setGrassAmount(float grassAmount) throws InvalidInputsException {
        // Throw invalid input if not positive float
        if (grassAmount < 0) {
            throw new InvalidInputsException();
        }
        Main.grassAmount = grassAmount;
    }

    /**
     * Gets the number of animals.
     * @return the number of animals.
     */
    public static int getNoAnimals() {
        return noAnimals;
    }

    /**
     * Sets the number of animals.
     * @param noAnimals the number of animals.
     */
    public static void setNoAnimals(int noAnimals) throws InvalidInputsException {
        // Throw invalid input if not positive integer
        if (noAnimals < 1) {
            throw new InvalidInputsException();
        }
        Main.noAnimals = noAnimals;
    }

    /**
     * Main method to run the simulation.
     * @param args command-line arguments.
     * @throws Exception for various simulation errors.
     */
    public static void main(String[] args) throws InvalidInputsException, InvalidNumberOfAnimalParametersException, Exception {
        ArrayList<Animal> animals = readAnimals();
        removeDeadAnimals(animals);
        runSimulation(days, grassAmount, animals);
        printAnimals(animals);
    }

    /**
     * Reads animal data from the input file and creates a list of Animal objects.
     * @return a list of Animal objects.
     * @throws Exception for invalid inputs or data errors.
     */
    private static ArrayList<Animal> readAnimals() throws Exception {
        ArrayList<Animal> animals = new ArrayList<>();
        String filePath = "input.txt";
        final int maxLength = 4;

        // Check if the file is empty
        if (new File(filePath).length() == 0) {
            throw new InvalidInputsException();
        }

        try (Scanner scanner = new Scanner(new File(filePath))) {
            setDays(Integer.parseInt(scanner.nextLine().trim())); // Read first line
            if (getDays() < MIN_DAYS || getDays() > MAX_DAYS) {
                throw new InvalidInputsException();
            }

            setGrassAmount(Float.parseFloat(scanner.nextLine().replace("F", "").trim())); // Read second line
            if (getGrassAmount() < MIN_AMOUNT_GRASS || getGrassAmount() > MAX_AMOUNT_GRASS) {
                throw new GrassOutOfBoundsException();
            }

            setNoAnimals(Integer.parseInt(scanner.nextLine().trim())); // Read third line
            if (getNoAnimals() < MIN_NO_ANIMALS || getNoAnimals() > MAX_NO_ANIMALS) {
                throw new InvalidInputsException();
            }

            for (int i = 0; i < getNoAnimals(); i++) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    throw new InvalidNumberOfAnimalParametersException();
                }

                String[] animalProps = line.split("\\s+");
                if (animalProps.length != maxLength) {
                    throw new InvalidNumberOfAnimalParametersException();
                }

                String type = animalProps[0];
                // Check if the animal type is valid
                if (!type.equals("Lion") && !type.equals("Boar") && !type.equals("Zebra")) {
                    throw new InvalidInputsException();
                }

                float weight;
                float speed;
                float energy;
                try {
                weight = Float.parseFloat(animalProps[1].replace("F", "").trim());
                speed = Float.parseFloat(animalProps[2].replace("F", "").trim());
                energy = Float.parseFloat(animalProps[maxLength - 1].replace("F", "").trim());
                } catch (NumberFormatException e) {
                    throw new InvalidInputsException();
                }

                if (weight < MIN_WEIGHT || weight > MAX_WEIGHT) { // validate weight
                    throw new WeightOutOfBoundsException();
                }

                if (speed < MIN_SPEED || speed > MAX_SPEED) { // validate speed
                    throw new SpeedOutOfBoundsException();
                }

                if (energy < MIN_ENERGY || energy > MAX_ENERGY) {
                    throw new EnergyOutOfBoundsException();
                }
                switch (type) {
                    case "Lion":
                        animals.add(new Lion(weight, speed, energy));
                        break;
                    case "Boar":
                        animals.add(new Boar(weight, speed, energy));
                        break;
                    case "Zebra":
                        animals.add(new Zebra(weight, speed, energy));
                        break;
                    default:
                        throw new InvalidInputsException();
                }
            }
            // any extra line will throw an exception
            if (scanner.hasNextLine()) {
                throw new InvalidInputsException();
            }

        } catch (Exception e) {
            // check if the exception is a custom exception
            if (e instanceof InvalidNumberOfAnimalParametersException
                || e instanceof GrassOutOfBoundsException
                || e instanceof WeightOutOfBoundsException
                || e instanceof SpeedOutOfBoundsException
                || e instanceof EnergyOutOfBoundsException) {
                System.out.println(e.getMessage());
            } else {
                System.out.println("Invalid inputs");
            }
            System.exit(0);
        }
        return animals;
    }

    /**
     * Runs the simulation based on the validated input parameters.
     * @param days        number of simulation days.
     * @param grassAmount initial amount of grass in the field.
     * @param animals     list of animals participating in the simulation.
     */
    private static void runSimulation(int days, float grassAmount, ArrayList<Animal> animals) {
        Field field;
        try {
            field = new Field(grassAmount);
        } catch (GrassOutOfBoundsException e) {
            System.out.println(e.getMessage());
            return;
        }

        for (int currentDay = 1; currentDay <= days; currentDay++) {
            if (animals.isEmpty()) {
                break;
            }

            for (int i = 0; i < animals.size(); i++) {
                Animal currentAnimal = animals.get(i);
                Animal prey = animals.get((i + 1) % animals.size()); // Circular prey selection

                if (currentAnimal.getEnergy() <= 0) {
                    continue;
                }

                if (currentAnimal instanceof Herbivore) {
                    ((Herbivore) currentAnimal).grazeInTheField(currentAnimal, field);
                }

                if (currentAnimal instanceof Carnivore) {
                    Carnivore carnivore = (Carnivore) currentAnimal;
                    try {
                        Animal chosenPrey = carnivore
                        .choosePrey(Collections
                        .singletonList(prey), new T<>(currentAnimal));
                        if (chosenPrey != null) {
                            carnivore.huntPrey(currentAnimal, chosenPrey);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            field.makeGrassGrow(); // Grass grows at the end of each day
            animals.forEach(Animal::decrementEnergy); // Energy decreases by 1% at the end of each day
            removeDeadAnimals(animals); // Remove dead animals at the end of each day
        }
    }

    /**
     * Removes animals with zero or negative energy from the list.
     * @param animals the list of animals.
     */
    private static void removeDeadAnimals(ArrayList<Animal> animals) {
        animals.removeIf(animal -> animal.getEnergy() <= 0);
    }

    /**
     * Prints the sounds of all animals that survived the simulation.
     * @param animals the list of animals.
     */
    private static void printAnimals(ArrayList<Animal> animals) {
        animals.stream()
                .filter(animal -> animal.getEnergy() > 0)
                .forEach(Animal::makeSound);
    }
}

/**
 * Program constants used for simulation parameters.
 */
enum Constants {
    MIN_DAYS(1),
    MAX_DAYS(30),
    MIN_NO_ANIMALS(1),
    MAX_NO_ANIMALS(20),
    MIN_AMOUNT_GRASS(0),
    MAX_AMOUNT_GRASS(100),
    MIN_ENERGY(0),
    MAX_ENERGY(100),
    MIN_WEIGHT(5),
    MAX_WEIGHT(200),
    MIN_SPEED(5),
    MAX_SPEED(60),
    GRAZE_ENERGY_DIVISOR(10);

    private final float value;

    Constants(float value) {
        this.value = value;
    }

    /**
     * @return the constant's value.
     */
    public float getValue() {
        return value;
    }
}

/**
 * Abstract class for Animal objects.
 */
abstract class Animal {

    private final float weight;
    private final float speed;
    private float energy;

    /**
     * Constructor to create Animal objects.
     *
     * @param weight the weight of the animal.
     * @param speed  the speed of the animal.
     * @param energy the energy of the animal.
     * @throws WeightOutOfBoundsException if weight is out of bounds.
     * @throws SpeedOutOfBoundsException  if speed is out of bounds.
     * @throws EnergyOutOfBoundsException if energy is out of bounds.
     */
    protected Animal(float weight, float speed, float energy)
            throws WeightOutOfBoundsException, SpeedOutOfBoundsException, EnergyOutOfBoundsException {

        if (weight < Constants.MIN_WEIGHT.getValue() || weight > Constants.MAX_WEIGHT.getValue()) {
            throw new WeightOutOfBoundsException();
        }
        if (speed < Constants.MIN_SPEED.getValue() || speed > Constants.MAX_SPEED.getValue()) {
            throw new SpeedOutOfBoundsException();
        }
        if (energy < Constants.MIN_ENERGY.getValue() || energy > Constants.MAX_ENERGY.getValue()) {
            throw new EnergyOutOfBoundsException();
        }

        this.weight = weight;
        this.speed = speed;
        this.energy = energy;
    }

    /**
     * @return the animal's weight.
     */
    public float getWeight() {
        return weight;
    }

    /**
     * @return the animal's speed.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * @return the animal's energy.
     */
    public float getEnergy() {
        return energy;
    }

    /**
     * Sets the animal's energy, capped by the maximum energy.
     *
     * @param energy the energy to set.
     */
    public void setEnergy(float energy) {
        this.energy = Math.min(this.energy + energy, Constants.MAX_ENERGY.getValue());
    }

    /**
     * Reduces the animal's energy by 1%, ensuring it does not drop below zero.
     */
    public void decrementEnergy() {
        this.energy = Math.max(this.energy - 1, 0);
    }

    /**
     * Kills the animal by setting its energy to zero.
     */
    public void die() {
        this.energy = 0;
    }

    /**s
     * Makes the animal's sound. Implementation is species-specific.
     */
    public abstract void makeSound();

    /**
     * Defines species-specific feeding behavior.
     *
     * @param animals a list of animals.
     * @param field   the field environment.
     */
    public abstract void eat(List<Animal> animals, Field field);
}


/**
 * Interface defining carnivorous behavior.
 */
interface Carnivore {

    /**
     * Chooses prey to attack.
     *
     * @param animals list of potential prey.
     * @param hunter  the predator animal.
     * @return the chosen prey.
     * @throws SelfHuntingException   if the predator tries to hunt itself.
     * @throws CannibalismException   if the predator tries to hunt its own kind.
     * @throws TooStrongPreyException if the prey is too strong to attack.
     */
    default Animal choosePrey(List<Animal> animals, T<?> hunter)
            throws SelfHuntingException, CannibalismException, TooStrongPreyException {

        Animal prey = animals.get(0);

        if (prey.getEnergy() <= 0) { // Skip dead animals
            return null;
        }
        if (prey == hunter.getAnimal()) { // Self-hunting is not allowed
            throw new SelfHuntingException();
        }
        // Cannibalism is not allowed
        if (prey.getClass().getSimpleName()
                .equals(hunter.getAnimal().getClass().getSimpleName())) {
            throw new CannibalismException();
        }
        if (prey.getSpeed() >= hunter.getAnimal().getSpeed()
            && prey.getEnergy() >= hunter.getAnimal().getEnergy()) { // Prey is too strong
            throw new TooStrongPreyException();
        }
        return prey;
    }

    /**
     * Attacks prey to increase energy.
     *
     * @param hunter the predator animal.
     * @param prey   the prey animal.
     */
    default void huntPrey(Animal hunter, Animal prey) {
        prey.die(); // Prey dies
        hunter.setEnergy(prey.getWeight()); // Hunter gains energy
    }
}

/**
 * Interface defining herbivorous behavior.
 */
interface Herbivore {

    /**
     * Grazes in the field to gain energy.
     *
     * @param grazer the grazing animal.
     * @param field  the field environment.
     */
    default void grazeInTheField(Animal grazer, Field field) {
        float grassAmount = field.getGrassAmount();
        float grazerWeight = grazer.getWeight();
        float energyGained = grazerWeight / Constants.GRAZE_ENERGY_DIVISOR.getValue();

        if (grassAmount >= grazerWeight / Constants.GRAZE_ENERGY_DIVISOR.getValue()) {
            grazer.setEnergy(energyGained); // Grazer gains energy
            field.decreaseGrassAmount(energyGained); // Grass amount decreases
        }
    }
}

/**
 * Interface representing animals that are both carnivorous and herbivorous.
 */
interface Omnivore extends Carnivore, Herbivore {
}

/**
 * Represents the state of the environment for animals, including grass availability.
 */
class Field {

    private float grassAmount;

    /**
     * Initializes the field with a specific amount of grass.
     *
     * @param grassAmount the initial amount of grass.
     * @throws GrassOutOfBoundsException if the grass amount is out of bounds.
     */
    public Field(float grassAmount) throws GrassOutOfBoundsException {
        if (grassAmount < Constants.MIN_AMOUNT_GRASS.getValue()
                || grassAmount > Constants.MAX_AMOUNT_GRASS.getValue()) {
            throw new GrassOutOfBoundsException();
        }
        this.grassAmount = grassAmount;
    }

    /**
     * @return the current amount of grass in the field.
     */
    public float getGrassAmount() {
        return grassAmount;
    }

    /**
     * Decreases the amount of grass in the field by a specific amount.
     *
     * @param amount the amount to decrease.
     */
    public void decreaseGrassAmount(float amount) {
        grassAmount = Math.max(grassAmount - amount, Constants.MIN_AMOUNT_GRASS.getValue());
    }

    /**
     * Doubles the current grass amount, capped at the maximum allowed value.
     */
    public void makeGrassGrow() {
        grassAmount = Math.min(grassAmount * 2, Constants.MAX_AMOUNT_GRASS.getValue());
    }
}

/**
 * Represents a Lion, which is a carnivorous animal.
 */
class Lion extends Animal implements Carnivore {

    /**
     * Creates a new Lion instance.
     *
     * @param weight the weight of the lion.
     * @param speed  the speed of the lion.
     * @param energy the energy of the lion.
     * @throws WeightOutOfBoundsException if weight is out of bounds.
     * @throws SpeedOutOfBoundsException  if speed is out of bounds.
     * @throws EnergyOutOfBoundsException if energy is out of bounds.
     */
    public Lion(float weight, float speed, float energy)
            throws WeightOutOfBoundsException, SpeedOutOfBoundsException, EnergyOutOfBoundsException {
        super(weight, speed, energy);
    }

    @Override
    public void makeSound() {
        System.out.println(AnimalSound.LION.getSound());
    }

    @Override
    public void eat(List<Animal> animals, Field field) {
        try {
            Animal prey = choosePrey(animals, new T<>(this));
            if (prey != null) {
                huntPrey(this, prey);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

/**
 * Represents a Boar, which is an omnivorous animal.
 */
class Boar extends Animal implements Omnivore {

    /**
     * Creates a new Boar instance.
     *
     * @param weight the weight of the boar.
     * @param speed  the speed of the boar.
     * @param energy the energy of the boar.
     * @throws WeightOutOfBoundsException if weight is out of bounds.
     * @throws SpeedOutOfBoundsException  if speed is out of bounds.
     * @throws EnergyOutOfBoundsException if energy is out of bounds.
     */
    public Boar(float weight, float speed, float energy)
            throws WeightOutOfBoundsException, SpeedOutOfBoundsException, EnergyOutOfBoundsException {
        super(weight, speed, energy);
    }

    @Override
    public void makeSound() {
        System.out.println(AnimalSound.BOAR.getSound());
    }

    @Override
    public void eat(List<Animal> animals, Field field) {
        grazeInTheField(this, field);
        try {
            Animal prey = choosePrey(animals, new T<Boar>(this));
            if (prey != null) {
                huntPrey(this, prey);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

/**
 * Represents a Zebra, which is a herbivorous animal.
 */
class Zebra extends Animal implements Herbivore {

    /**
     * Creates a new Zebra instance.
     *
     * @param weight the weight of the zebra.
     * @param speed  the speed of the zebra.
     * @param energy the energy of the zebra.
     * @throws WeightOutOfBoundsException if weight is out of bounds.
     * @throws SpeedOutOfBoundsException  if speed is out of bounds.
     * @throws EnergyOutOfBoundsException if energy is out of bounds.
     */
    public Zebra(float weight, float speed, float energy)
            throws WeightOutOfBoundsException, SpeedOutOfBoundsException, EnergyOutOfBoundsException {
        super(weight, speed, energy);
    }

    @Override
    public void makeSound() {
        System.out.println(AnimalSound.ZEBRA.getSound());
    }

    @Override
    public void eat(List<Animal> animals, Field field) {
        grazeInTheField(this, field);
    }
}

/**
 * Exception thrown when an animal's energy is out of bounds.
 */
class EnergyOutOfBoundsException extends Exception {

    /**
     * @return the exception message indicating the energy is out of bounds.
     */
    @Override
    public String getMessage() {
        return "The energy is out of bounds";
    }
}

/**
 * Exception thrown when an animal's speed is out of bounds.
 */
class SpeedOutOfBoundsException extends Exception {

    /**
     * @return the exception message indicating the speed is out of bounds.
     */
    @Override
    public String getMessage() {
        return "The speed is out of bounds";
    }
}

/**
 * Exception thrown when an animal's weight is out of bounds.
 */
class WeightOutOfBoundsException extends Exception {

    /**
     * @return the exception message indicating the weight is out of bounds.
     */
    @Override
    public String getMessage() {
        return "The weight is out of bounds";
    }
}

/**
 * Exception thrown when the grass amount in the field is out of bounds.
 */
class GrassOutOfBoundsException extends Exception {

    /**
     * @return the exception message indicating the grass amount is out of bounds.
     */
    @Override
    public String getMessage() {
        return "The grass is out of bounds";
    }
}

/**
 * Exception thrown when an invalid number of parameters are provided for an animal.
 */
class InvalidNumberOfAnimalParametersException extends Exception {

    /**
     * @return the exception message indicating the number of parameters is invalid.
     */
    @Override
    public String getMessage() {
        return "Invalid number of animal parameters";
    }
}

/**
 * Exception thrown when the provided inputs are invalid.
 */
class InvalidInputsException extends Exception {

    /**
     * @return the exception message indicating the inputs are invalid.
     */
    @Override
    public String getMessage() {
        return "Invalid inputs";
    }
}

/**
 * Exception thrown when an animal attempts to hunt itself.
 */
class SelfHuntingException extends Exception {

    /**
     * @return the exception message indicating self-hunting is not allowed.
     */
    @Override
    public String getMessage() {
        return "Self-hunting is not allowed";
    }
}


/**
 * Exception thrown when an animal attempts to hunt its own kind.
 */
class CannibalismException extends Exception {

    /**
     * @return the exception message indicating cannibalism is not allowed.
     */
    @Override
    public String getMessage() {
        return "Cannibalism is not allowed";
    }
}

/**
 * Exception thrown when a predator attempts to hunt prey that is too strong or fast.
 */
class TooStrongPreyException extends Exception {

    /**
     * @return the exception message indicating the prey is too strong or fast.
     */
    @Override
    public String getMessage() {
        return "The prey is too strong or too fast to attack";
    }
}

/**
 * Represents the sounds made by different animal species.
 */
enum AnimalSound {
    LION("Roar"),
    ZEBRA("Ihoho"),
    BOAR("Oink");

    private final String sound;

    /**
     * Initializes the sound for the animal.
     *
     * @param sound the sound made by the animal.
     */
    AnimalSound(String sound) {
        this.sound = sound;
    }

    /**
     * @return the sound associated with the animal.
     */
    public String getSound() {
        return sound;
    }
}

/**
 * Encapsulates the hunter type (e.g., Lion or Boar).
 *
 * @param <E> A type that extends the Animal class.
 */
class T<E extends Animal> {

    private final E animal;

    /**
     * Creates a wrapper for an animal of type E.
     *
     * @param animal the animal instance.
     */
    public T(E animal) {
        this.animal = animal;
    }

    /**
     * @return the encapsulated animal instance.
     */
    public E getAnimal() {
        return animal;
    }
}
