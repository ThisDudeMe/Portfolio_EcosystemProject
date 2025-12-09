package se.ecosystem.simulation;

import se.ecosystem.model.Animal;
import se.ecosystem.model.AnimalCarnivore;
import se.ecosystem.model.AnimalHerbivore;
import se.ecosystem.model.AnimalOmnivore;
import se.ecosystem.model.enums.AnimalState;

import java.util.*;

public class SimulationHandler {

    private final int width;
    private final int height;
    private final List<Animal> animals = new ArrayList<>();
    private final Map<Animal, Point> animalPositions = new HashMap<>();
    private final Random random = new Random();

    // Shared Point record for coordinates
    public record Point(int x, int y) {}

    public SimulationHandler(int width, int height) {
        this.width = width;
        this.height = height;
        spawnAnimals();
    }

    private void spawnAnimals() {
        // Spawn 2 Herbivores
        spawnAnimal(new AnimalHerbivore(100, 100, 0));
        spawnAnimal(new AnimalHerbivore(100, 100, 0));

        // Spawn 2 Carnivores
        spawnAnimal(new AnimalCarnivore(120, 150, 0));
        spawnAnimal(new AnimalCarnivore(120, 150, 0));

        // Spawn 2 Omnivores
        spawnAnimal(new AnimalOmnivore(110, 120, 0));
        spawnAnimal(new AnimalOmnivore(110, 120, 0));
    }

    private void spawnAnimal(Animal animal) {
        animals.add(animal);
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        animalPositions.put(animal, new Point(x, y));
    }

    public void updateSimulation() {
        for (Animal animal : animals) {
            handleStateTransition(animal);
            handleAction(animal);
        }
    }

    private void handleStateTransition(Animal animal) {
        // Basic state machine logic
        switch (animal.getState()) {
            case ROAMING -> {
                if (animal.getEnergy() < 30) {
                    animal.setState(AnimalState.RESTING);
                } else if (animal.getHunger() > 70) {
                    animal.setState(AnimalState.EATING);
                }
            }
            case RESTING -> {
                if (animal.getEnergy() >= 100) {
                    animal.setState(AnimalState.ROAMING);
                }
            }
            case EATING -> {
                if (animal.getHunger() <= 0) {
                    animal.setState(AnimalState.ROAMING);
                }
            }
        }
    }

    private void handleAction(Animal animal) {
        switch (animal.getState()) {
            case ROAMING -> {
                moveAnimalRandomly(animal);
                // Moving costs energy and increases hunger
                animal.setEnergy(Math.max(0, animal.getEnergy() - 1));
                animal.setHunger(Math.min(100, animal.getHunger() + 1));
            }
            case RESTING -> {
                // Recover energy
                animal.setEnergy(Math.min(100, animal.getEnergy() + 2));
            }
            case EATING -> {
                // Recover hunger (decrease hunger value)
                animal.setHunger(Math.max(0, animal.getHunger() - 5));
            }
        }
    }

    private void moveAnimalRandomly(Animal animal) {
        Point currentPos = animalPositions.get(animal);
        if (currentPos == null) return;

        int dx = random.nextInt(3) - 1; // -1, 0, or 1
        int dy = random.nextInt(3) - 1;

        int newX = Math.max(0, Math.min(width - 1, currentPos.x() + dx));
        int newY = Math.max(0, Math.min(height - 1, currentPos.y() + dy));

        animalPositions.put(animal, new Point(newX, newY));
    }

    public Map<Animal, Point> getAnimalPositions() {
        return Collections.unmodifiableMap(animalPositions);
    }
}