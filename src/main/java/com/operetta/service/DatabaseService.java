package com.operetta.service;

import com.operetta.model.Connection;
import com.operetta.model.ConnectionType;
import com.operetta.model.Creator;
import com.operetta.model.Work;
import com.operetta.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for database operations
 */
public class DatabaseService {
    
    /**
     * Get all works from the database
     */
    public List<Work> getAllWorks() {
        List<Work> works = new ArrayList<>();
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM works ORDER BY title")) {
            
            while (rs.next()) {
                Work work = new Work(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("original"),
                        rs.getString("theatre"),
                        rs.getObject("pyear", Integer.class),
                        rs.getObject("acts", Integer.class),
                        rs.getObject("scenes", Integer.class)
                );
                works.add(work);
            }
        } catch (SQLException e) {
            System.err.println("Error getting works: " + e.getMessage());
            e.printStackTrace();
        }
        
        return works;
    }
    
    /**
     * Get a work by ID
     */
    public Optional<Work> getWorkById(int id) {
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM works WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Work work = new Work(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("original"),
                        rs.getString("theatre"),
                        rs.getObject("pyear", Integer.class),
                        rs.getObject("acts", Integer.class),
                        rs.getObject("scenes", Integer.class)
                );
                return Optional.of(work);
            }
        } catch (SQLException e) {
            System.err.println("Error getting work by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all creators from the database
     */
    public List<Creator> getAllCreators() {
        List<Creator> creators = new ArrayList<>();
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM creators ORDER BY cname")) {
            
            while (rs.next()) {
                Creator creator = new Creator(
                        rs.getInt("id"),
                        rs.getString("cname")
                );
                creators.add(creator);
            }
        } catch (SQLException e) {
            System.err.println("Error getting creators: " + e.getMessage());
            e.printStackTrace();
        }
        
        return creators;
    }
    
    /**
     * Get a creator by ID
     */
    public Optional<Creator> getCreatorById(int id) {
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM creators WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Creator creator = new Creator(
                        rs.getInt("id"),
                        rs.getString("cname")
                );
                return Optional.of(creator);
            }
        } catch (SQLException e) {
            System.err.println("Error getting creator by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    /**
     * Get all connections with joined work and creator info
     */
    public List<Connection> getAllConnectionsWithDetails() {
        List<Connection> connections = new ArrayList<>();
        
        String sql = "SELECT c.id, c.workid, c.ctype, c.creatorid, w.title, cr.cname " +
                     "FROM connections c " +
                     "JOIN works w ON c.workid = w.id " +
                     "JOIN creators cr ON c.creatorid = cr.id " +
                     "ORDER BY w.title";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Connection connection = new Connection(
                        rs.getInt("id"),
                        rs.getInt("workid"),
                        ConnectionType.fromString(rs.getString("ctype")),
                        rs.getInt("creatorid"),
                        rs.getString("title"),
                        rs.getString("cname")
                );
                connections.add(connection);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connections with details: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connections;
    }
    
    /**
     * Get connections with details, filtered by various criteria
     */
    public List<Connection> getFilteredConnections(String workTitle, String creatorName, 
                                                  ConnectionType connectionType) {
        List<Connection> connections = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
                "SELECT c.id, c.workid, c.ctype, c.creatorid, w.title, cr.cname " +
                "FROM connections c " +
                "JOIN works w ON c.workid = w.id " +
                "JOIN creators cr ON c.creatorid = cr.id " +
                "WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        
        if (workTitle != null && !workTitle.isEmpty()) {
            sql.append("AND w.title LIKE ? ");
            params.add("%" + workTitle + "%");
        }
        
        if (creatorName != null && !creatorName.isEmpty()) {
            sql.append("AND cr.cname LIKE ? ");
            params.add("%" + creatorName + "%");
        }
        
        if (connectionType != null) {
            sql.append("AND c.ctype = ? ");
            params.add(connectionType.getValue());
        }
        
        sql.append("ORDER BY w.title");
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Connection connection = new Connection(
                        rs.getInt("id"),
                        rs.getInt("workid"),
                        ConnectionType.fromString(rs.getString("ctype")),
                        rs.getInt("creatorid"),
                        rs.getString("title"),
                        rs.getString("cname")
                );
                connections.add(connection);
            }
        } catch (SQLException e) {
            System.err.println("Error getting filtered connections: " + e.getMessage());
            e.printStackTrace();
        }
        
        return connections;
    }
    
    /**
     * Add a new work to the database
     */
    public boolean addWork(Work work) {
        String sql = "INSERT INTO works (id, title, original, theatre, pyear, acts, scenes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, work.getId());
            stmt.setString(2, work.getTitle());
            stmt.setString(3, work.getOriginal());
            stmt.setString(4, work.getTheatre());
            
            if (work.getPremiereYear() != null) {
                stmt.setInt(5, work.getPremiereYear());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            if (work.getActs() != null) {
                stmt.setInt(6, work.getActs());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            if (work.getScenes() != null) {
                stmt.setInt(7, work.getScenes());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding work: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing work in the database
     */
    public boolean updateWork(Work work) {
        String sql = "UPDATE works SET title = ?, original = ?, theatre = ?, " +
                     "pyear = ?, acts = ?, scenes = ? WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, work.getTitle());
            stmt.setString(2, work.getOriginal());
            stmt.setString(3, work.getTheatre());
            
            if (work.getPremiereYear() != null) {
                stmt.setInt(4, work.getPremiereYear());
            } else {
                stmt.setNull(4, java.sql.Types.INTEGER);
            }
            
            if (work.getActs() != null) {
                stmt.setInt(5, work.getActs());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            
            if (work.getScenes() != null) {
                stmt.setInt(6, work.getScenes());
            } else {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmt.setInt(7, work.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating work: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a work from the database
     */
    public boolean deleteWork(int id) {
        // First, delete any connections for this work
        String deleteConnectionsSql = "DELETE FROM connections WHERE workid = ?";
        String deleteWorkSql = "DELETE FROM works WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmtConnections = conn.prepareStatement(deleteConnectionsSql);
                 PreparedStatement stmtWork = conn.prepareStatement(deleteWorkSql)) {
                
                stmtConnections.setInt(1, id);
                stmtConnections.executeUpdate();
                
                stmtWork.setInt(1, id);
                int rowsAffected = stmtWork.executeUpdate();
                
                conn.commit();
                return rowsAffected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting work: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Add a new connection to the database
     */
    public boolean addConnection(Connection connection) {
        String sql = "INSERT INTO connections (workid, ctype, creatorid) VALUES (?, ?, ?)";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, connection.getWorkId());
            stmt.setString(2, connection.getConnectionType().getValue());
            stmt.setInt(3, connection.getCreatorId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding connection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing connection in the database
     */
    public boolean updateConnection(Connection connection) {
        String sql = "UPDATE connections SET workid = ?, ctype = ?, creatorid = ? WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, connection.getWorkId());
            stmt.setString(2, connection.getConnectionType().getValue());
            stmt.setInt(3, connection.getCreatorId());
            stmt.setInt(4, connection.getId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating connection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a connection from the database
     */
    public boolean deleteConnection(int id) {
        String sql = "DELETE FROM connections WHERE id = ?";
        
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting connection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get the next available ID for a new work
     */
    public int getNextWorkId() {
        try (java.sql.Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM works")) {
            
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Error getting next work ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 1; // Default to 1 if the table is empty or there's an error
    }
}