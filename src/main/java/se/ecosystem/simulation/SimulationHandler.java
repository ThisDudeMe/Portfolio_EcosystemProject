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
    private final boolean[][] grass;
    private final Random random = new Random();

    private int tickCounter = 0;

    public record Point(int x, int y) {}

    public SimulationHandler(int width, int height) {
        this.width = width;
        this.height = height;
        this.grass = new boolean[width][height];
        spawnGrass();
        spawnAnimals();
    }

    private void spawnGrass() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextDouble() < 0.2) {
                    grass[x][y] = true;
                }
            }
        }
    }

    private void spawnAnimals() {
        int cornerW = Math.max(1, width / 4);
        int cornerH = Math.max(1, height / 4);

        // Herbivores: Top-Left
        for (int i = 0; i < 8; i++) {
            spawnAnimalInBounds(new AnimalHerbivore(100, 100, 0), 0, cornerW, 0, cornerH);
        }
        // Carnivores: Top-Right
        for (int i = 0; i < 2; i++) {
            spawnAnimalInBounds(new AnimalCarnivore(120, 150, 0), width - cornerW, width, 0, cornerH);
        }
        // Omnivores: Bottom-Right
        for (int i = 0; i < 4; i++) {
            spawnAnimalInBounds(new AnimalOmnivore(110, 120, 0), width - cornerW, width, height - cornerH, height);
        }
    }

    private void spawnAnimal(Animal animal) {
        spawnAnimalInBounds(animal, 0, width, 0, height);
    }

    private void spawnAnimalInBounds(Animal animal, int minX, int maxX, int minY, int maxY) {
        animals.add(animal);
        int x = random.nextInt(maxX - minX) + minX;
        int y = random.nextInt(maxY - minY) + minY;
        animalPositions.put(animal, new Point(x, y));
    }

    public void updateSimulation() {
        tickCounter++;
        growGrass();

        List<Animal> babies = new ArrayList<>();
        Iterator<Animal> iterator = animals.iterator();

        while (iterator.hasNext()) {
            Animal animal = iterator.next();

            if (tickCounter % 10 == 0) {
                animal.setAge(animal.getAge() + 1);
            }

            if (animal.getHunger() >= 100) {
                animal.setHealth(animal.getHealth() - 10);
            }

            if (animal.getHealth() <= 0 || animal.getAge() > 100) {
                animalPositions.remove(animal);
                iterator.remove();
                continue;
            }

            handleStateTransition(animal);
            handleAction(animal);

            if (animal.getHealth() > 50 && animal.getEnergy() > 60 &&
                    animal.getHunger() < 50 && animal.getAge() > 20) {

                if (random.nextDouble() < 0.02) {
                    Animal baby = createOffspring(animal);
                    if (baby != null) {
                        babies.add(baby);
                        animal.setEnergy(animal.getEnergy() - 40);
                        animal.setHunger(animal.getHunger() + 20);
                    }
                }
            }
        }

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
        if (random.nextDouble() < 0.5) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            grass[x][y] = true;
        }
    }

    private void handleStateTransition(Animal animal) {
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
                animal.setEnergy(Math.max(0, animal.getEnergy() - 1));
                animal.setHunger(Math.min(100, animal.getHunger() + 1));
            }
            case RESTING -> {
                animal.setEnergy(Math.min(100, animal.getEnergy() + 2));
            }
            case EATING -> {
                Point pos = animalPositions.get(animal);
                if (pos == null) return;

                if (animal.getType() == AnimalType.HERBIVORE || animal.getType() == AnimalType.OMNIVORE) {
                    if (grass[pos.x()][pos.y()]) {
                        grass[pos.x()][pos.y()] = false;
                        animal.setHunger(Math.max(0, animal.getHunger() - 20));
                    } else {
                        moveAnimalRandomly(animal);
                        animal.setEnergy(Math.max(0, animal.getEnergy() - 1));
                    }
                }

                if (animal.getType() == AnimalType.CARNIVORE) {
                    attemptToHunt(animal, pos);
                }
            }
        }
    }

    private void attemptToHunt(Animal predator, Point pos) {
        Animal prey = null;
        for (Animal candidate : animals) {
            if (candidate != predator && candidate.getHealth() > 0) {
                Point candidatePos = animalPositions.get(candidate);
                if (candidatePos != null && candidatePos.equals(pos)) {
                    if (candidate.getType() == AnimalType.HERBIVORE || candidate.getType() == AnimalType.OMNIVORE) {
                        prey = candidate;
                        break;
                    }
                }
            }
        }

        if (prey != null) {
            prey.setHealth(0);
            predator.setHunger(0);
            predator.setEnergy(Math.min(100, predator.getEnergy() + 30));
        } else {
            moveAnimalRandomly(predator);
            predator.setEnergy(Math.max(0, predator.getEnergy() - 1));
        }
    }

    private void moveAnimalRandomly(Animal animal) {
        Point currentPos = animalPositions.get(animal);
        if (currentPos == null) return;

        int dx = 0;
        int dy = 0;

        while (dx == 0 && dy == 0) {
            dx = random.nextInt(3) - 1;
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