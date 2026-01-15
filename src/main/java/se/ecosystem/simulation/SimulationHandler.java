package se.ecosystem.simulation;

import se.ecosystem.model.Animal;
import se.ecosystem.model.AnimalCarnivore;
import se.ecosystem.model.AnimalHerbivore;
import se.ecosystem.model.AnimalOmnivore;
import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;

import java.util.*;

public class SimulationHandler {

    private final int width;
    private final int height;
    private final List<Animal> animals = new ArrayList<>();
    private final Map<Animal, Point> animalPositions = new HashMap<>();
    // true = grass exists, false = empty dirt
    private final boolean[][] grass;
    private final Random random = new Random();

    private int tickCounter = 0;

    // Shared Point record for coordinates
    public record Point(int x, int y) {}

    public SimulationHandler(int width, int height) {
        this.width = width;
        this.height = height;
        this.grass = new boolean[width][height];
        spawnGrass();
        spawnAnimals();
    }

    private void spawnGrass() {
        // Start with ~20% coverage
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextDouble() < 0.2) {
                    grass[x][y] = true;
                }
            }
        }
    }

    private void spawnAnimals() {
        // Spawn 4 of each type
        for (int i = 0; i < 4; i++) {
            spawnAnimal(new AnimalHerbivore(100, 100, 0));
            spawnAnimal(new AnimalCarnivore(120, 150, 0));
            spawnAnimal(new AnimalOmnivore(110, 120, 0));
        }
    }

    private void spawnAnimal(Animal animal) {
        animals.add(animal);
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        animalPositions.put(animal, new Point(x, y));
    }

    public void updateSimulation() {
        tickCounter++;
        growGrass();

        List<Animal> babies = new ArrayList<>();
        // Use iterator so we can remove dead animals safely while looping
        Iterator<Animal> iterator = animals.iterator();

        while (iterator.hasNext()) {
            Animal animal = iterator.next();

            // 1. Aging and Starvation
            // Slow down aging: Only increase age every 10 ticks
            if (tickCounter % 10 == 0) {
                animal.setAge(animal.getAge() + 1);
            }

            if (animal.getHunger() >= 100) {
                animal.setHealth(animal.getHealth() - 10);
            }

            // 2. Death Check (Health <= 0 or Old Age > 100)
            if (animal.getHealth() <= 0 || animal.getAge() > 100) {
                animalPositions.remove(animal);
                iterator.remove();
                continue; // Animal is dead, skip processing
            }

            // 3. Logic
            handleStateTransition(animal);
            handleAction(animal);

            // 4. Reproduction Check
            // Condition: Healthy, energetic, not too hungry, and mature enough
            if (animal.getHealth() > 50 && animal.getEnergy() > 60 &&
                    animal.getHunger() < 50 && animal.getAge() > 20) {

                // Small random chance to reproduce (2% per tick)
                if (random.nextDouble() < 0.02) {
                    Animal baby = createOffspring(animal);
                    if (baby != null) {
                        babies.add(baby);
                        // Parenting cost
                        animal.setEnergy(animal.getEnergy() - 40);
                        animal.setHunger(animal.getHunger() + 20);
                    }
                }
            }
        }

        // Add new babies to the simulation
        for (Animal baby : babies) {
            spawnAnimal(baby);
        }
    }

    private Animal createOffspring(Animal parent) {
        return switch (parent.getType()) {
            case HERBIVORE -> new AnimalHerbivore(100, 60, 10);
            case CARNIVORE -> new AnimalCarnivore(120, 60, 10);
            case OMNIVORE -> new AnimalOmnivore(110, 60, 10);
        };
    }

    private void growGrass() {
        // Slow grass growth: 50% chance to grow 1 tile per frame
        if (random.nextDouble() < 0.5) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            grass[x][y] = true;
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
                Point pos = animalPositions.get(animal);
                if (pos == null) return;

                // HERBIVORE / OMNIVORE (Eating Grass)
                if (animal.getType() == AnimalType.HERBIVORE || animal.getType() == AnimalType.OMNIVORE) {
                    if (grass[pos.x()][pos.y()]) {
                        grass[pos.x()][pos.y()] = false; // Grass consumed
                        animal.setHunger(Math.max(0, animal.getHunger() - 20));
                    } else {
                        // Search for food if none here
                        moveAnimalRandomly(animal);
                        animal.setEnergy(Math.max(0, animal.getEnergy() - 1));
                    }
                }

                // CARNIVORE (Eating Meat)
                if (animal.getType() == AnimalType.CARNIVORE) {
                    attemptToHunt(animal, pos);
                }
            }
        }
    }

    private void attemptToHunt(Animal predator, Point pos) {
        // Find a prey at the same location
        Animal prey = null;
        for (Animal candidate : animals) {
            // Must be alive, different animal, at same position
            if (candidate != predator && candidate.getHealth() > 0) {
                Point candidatePos = animalPositions.get(candidate);
                if (candidatePos != null && candidatePos.equals(pos)) {
                    // Check if it's valid prey (Carnivores eat Herbivores/Omnivores)
                    if (candidate.getType() == AnimalType.HERBIVORE || candidate.getType() == AnimalType.OMNIVORE) {
                        prey = candidate;
                        break;
                    }
                }
            }
        }

        if (prey != null) {
            // Eat the prey
            prey.setHealth(0); // Mark as dead (will be removed in next update loop)
            predator.setHunger(0); // Full
            predator.setEnergy(Math.min(100, predator.getEnergy() + 30));
        } else {
            // Failed to find food, wander to look for it
            moveAnimalRandomly(predator);
            predator.setEnergy(Math.max(0, predator.getEnergy() - 1));
        }
    }

    private void moveAnimalRandomly(Animal animal) {
        Point currentPos = animalPositions.get(animal);
        if (currentPos == null) return;

        // Ensure we don't just stay in place (0,0) if we are trying to move
        int dx = 0;
        int dy = 0;

        // Keep generating direction until it's not (0,0) or valid
        while (dx == 0 && dy == 0) {
            dx = random.nextInt(3) - 1; // -1, 0, or 1
            dy = random.nextInt(3) - 1;
        }

        int newX = Math.max(0, Math.min(width - 1, currentPos.x() + dx));
        int newY = Math.max(0, Math.min(height - 1, currentPos.y() + dy));

        animalPositions.put(animal, new Point(newX, newY));
    }

    public Map<Animal, Point> getAnimalPositions() {
        return Collections.unmodifiableMap(animalPositions);
    }

    public boolean[][] getGrass() {
        return grass;
    }
}