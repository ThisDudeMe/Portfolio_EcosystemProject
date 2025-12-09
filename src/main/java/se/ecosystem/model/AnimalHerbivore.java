package se.ecosystem.model;


import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;


public class AnimalHerbivore extends Animal{

    public AnimalHerbivore(double health, double energy, double hunger){

        super(AnimalState.ROAMING, AnimalType.HERBIVORE,
                health,
                energy,
                hunger,
                false          );

    }
}