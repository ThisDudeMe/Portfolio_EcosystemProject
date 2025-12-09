package se.ecosystem.model.enums;

public enum AnimalType {

    HERBIVORE(0),
    CARNIVORE(1),
    OMNIVORE(2);

    private final int value;

    AnimalType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
