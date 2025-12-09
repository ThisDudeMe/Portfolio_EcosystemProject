package se.ecosystem.model.enums;

public enum AnimalState {

    RESTING(0),
    ROAMING(1),
    EATING(2);

    private final int value;

    AnimalState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
