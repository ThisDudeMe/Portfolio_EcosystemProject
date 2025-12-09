package se.ecosystem.model;

import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;

public class AnimalCarnivore extends Animal {

    public AnimalCarnivore(double health, double energy, double hunger) {
        super(AnimalState.ROAMING, AnimalType.CARNIVORE, health, energy, hunger, true);
    }
}