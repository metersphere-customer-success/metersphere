package io.metersphere.constants;

public enum DataStatus {
    UNDERWAY("Underway"),
    TRASH("Trash"),
    PREPARE("Prepare"),
    COMPLETED("Completed");

    private String value;

    DataStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
