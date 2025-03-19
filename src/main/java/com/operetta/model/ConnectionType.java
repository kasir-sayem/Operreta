package com.operetta.model;

/**
 * Enum representing the three types of connections in the database
 */
public enum ConnectionType {
    MUSIC("music"),
    LIBRETTO("libretto"),
    TRANSLATION("translation");
    
    private final String value;
    
    ConnectionType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Convert a string value to its corresponding ConnectionType
     * @param value The string value
     * @return The ConnectionType enum value
     */
    public static ConnectionType fromString(String value) {
        for (ConnectionType type : ConnectionType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown connection type: " + value);
    }
}