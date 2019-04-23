package com.example.stockmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private String name;
    private Double value;
    private List<State> states = new ArrayList<>();

    public Product(String productName, Double productValue) {
        name = productName;
        value = productValue;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> state) {
        this.states = state;
    }

    public void addState(State state) {
        states.add(state);
    }
}
