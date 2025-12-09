package se.ecosystem.model;

import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;

public class AnimalOmnivore extends Animal {

    public AnimalOmnivore(double health, double energy, double hunger) {
        super(AnimalState.ROAMING, AnimalType.OMNIVORE, health, energy, hunger, true);
    }
}