package com.operetta.model;

/**
 * Model class representing a connection between a work and a creator
 */
public class Connection {
    private int id;
    private int workId;
    private ConnectionType connectionType;
    private int creatorId;
    
    // These fields are for joining with other tables
    private String workTitle;
    private String creatorName;
    
    /**
     * Default constructor
     */
    public Connection() {
    }
    
    /**
     * Constructor with all fields
     */
    public Connection(int id, int workId, ConnectionType connectionType, int creatorId) {
        this.id = id;
        this.workId = workId;
        this.connectionType = connectionType;
        this.creatorId = creatorId;
    }
    
    /**
     * Constructor with joined data
     */
    public Connection(int id, int workId, ConnectionType connectionType, int creatorId,
                     String workTitle, String creatorName) {
        this.id = id;
        this.workId = workId;
        this.connectionType = connectionType;
        this.creatorId = creatorId;
        this.workTitle = workTitle;
        this.creatorName = creatorName;
    }
    
    // Getters and setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getWorkId() {
        return workId;
    }
    
    public void setWorkId(int workId) {
        this.workId = workId;
    }
    
    public ConnectionType getConnectionType() {
        return connectionType;
    }
    
    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }
    
    public int getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getWorkTitle() {
        return workTitle;
    }
    
    public void setWorkTitle(String workTitle) {
        this.workTitle = workTitle;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Connection that = (Connection) o;
        
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return id;
    }
}