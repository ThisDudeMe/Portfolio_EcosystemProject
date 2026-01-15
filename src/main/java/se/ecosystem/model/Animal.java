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
    protected int age = 0;

    public Animal(AnimalState state, AnimalType type, double health, double energy, double hunger, boolean canAttack) {

        this.state = state;
        this.type = type;
        this.health = health;
        this.energy = energy;
        this.hunger = hunger;
        this.canAttack = canAttack;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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

    public void setState(AnimalState state) {
        this.state = state;
    }
    public void setType(AnimalType type) {
        this.type = type;
    }
    public void setHealth(double health) {
        this.health = health;
    }
    public void setEnergy(double energy) {
        this.energy = energy;
    }
    public void setHunger(double hunger) {
        this.hunger = hunger;
    }
    public void setCanAttack(boolean canAttack){
        this.canAttack = canAttack;
    }
}
