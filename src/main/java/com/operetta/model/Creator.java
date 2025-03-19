package com.operetta.model;

/**
 * Model class representing a creator (composer, writer, etc.)
 */
public class Creator {
    private int id;
    private String name;
    
    /**
     * Default constructor
     */
    public Creator() {
    }
    
    /**
     * Constructor with all fields
     */
    public Creator(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters and setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Creator creator = (Creator) o;
        
        return id == creator.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
}