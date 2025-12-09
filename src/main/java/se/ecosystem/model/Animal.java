package se.ecosystem.model;

import se.ecosystem.model.enums.AnimalState;
import se.ecosystem.model.enums.AnimalType;

public abstract class Animal {

    protected AnimalState state;
    protected AnimalType type;

    protected double health;
    protected double energy;
    protected double hunger;
    protected boolean canAttack;

    public Animal(AnimalState state, AnimalType type, double health, double energy, double hunger, boolean canAttack) {

        this.state = state;
        this.type = type;
        this.health = health;
        this.energy = energy;
        this.hunger = hunger;
        this.canAttack = canAttack;
    }

    public AnimalState getState() {
        return state;
    }

    public AnimalType getType() {
        return type;
    }
    public double getHealth() {
        return health;
    }
    public double getEnergy() {
        return energy;
    }
    public double getHunger() {
        return hunger;
    }
    public boolean getCanAttack(){
        return canAttack;
    }

    public AnimalState setState(AnimalState state) {
        return this.state = state;
    }
    public AnimalType setType(AnimalType type) {
        return this.type = type;
    }
    public double setHealth(double health) {
        return this.health = health;
    }
    public double setEnergy(double energy) {
        return this.energy = energy;
    }
    public double setHunger(double hunger) {
        return this.hunger = hunger;
    }
    public boolean setCanAttack(boolean canAttack){
        return this.canAttack = canAttack;
    }


}
