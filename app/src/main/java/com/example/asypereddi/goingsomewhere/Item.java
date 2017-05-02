package com.example.asypereddi.goingsomewhere;

/**
 * Item objects which make up all the lists, consist of name, importance,
 * and whether they've been packed or not. Importance is denoted by an integer
 * from 1 - 3. 1 being extraneous and 3 being essential.
 */

public class Item {
    private String name;
    private int importance;
    private boolean packed;

    public Item(String name, int importance) {
        this.name = name;
        this.importance = importance;
        this.packed = false;
    }

    public Item() {
    }

    public String getName() {
        return name;
    }

    public int getImportance() {
        return importance;
    }

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }
}
