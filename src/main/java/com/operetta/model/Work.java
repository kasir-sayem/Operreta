package com.operetta.model;

/**
 * Model class representing an operetta work
 */
public class Work {
    private int id;
    private String title;
    private String original;
    private String theatre;
    private Integer premiereYear;
    private Integer acts;
    private Integer scenes;
    
    /**
     * Default constructor
     */
    public Work() {
    }
    
    /**
     * Constructor with all fields
     */
    public Work(int id, String title, String original, String theatre, 
               Integer premiereYear, Integer acts, Integer scenes) {
        this.id = id;
        this.title = title;
        this.original = original;
        this.theatre = theatre;
        this.premiereYear = premiereYear;
        this.acts = acts;
        this.scenes = scenes;
    }
    
    // Getters and setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getOriginal() {
        return original;
    }
    
    public void setOriginal(String original) {
        this.original = original;
    }
    
    public String getTheatre() {
        return theatre;
    }
    
    public void setTheatre(String theatre) {
        this.theatre = theatre;
    }
    
    public Integer getPremiereYear() {
        return premiereYear;
    }
    
    public void setPremiereYear(Integer premiereYear) {
        this.premiereYear = premiereYear;
    }
    
    public Integer getActs() {
        return acts;
    }
    
    public void setActs(Integer acts) {
        this.acts = acts;
    }
    
    public Integer getScenes() {
        return scenes;
    }
    
    public void setScenes(Integer scenes) {
        this.scenes = scenes;
    }
    
    @Override
    public String toString() {
        return title;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Work work = (Work) o;
        
        return id == work.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
}