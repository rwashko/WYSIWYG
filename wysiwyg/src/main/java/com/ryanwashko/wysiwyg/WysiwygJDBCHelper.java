/*
 * This class is responsible for all direct database access.
 * Copyright Ryan Washko - 2019
 */

package com.ryanwashko.wysiwyg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WysiwygJDBCHelper {
	static WysiwygJDBCHelper instance = null;
	
	private WysiwygJDBCHelper() {}
	
	/**
	 * Singleton implementation
	 * 
	 * @return				The singleton instance
	 */
	public static WysiwygJDBCHelper getInstance() {
		if(null == instance) {
			instance = new WysiwygJDBCHelper();
		}
		return instance;
	}
	
	/**
	 * Open the database connection. This is mainly just so the url only has to be updated in one place.
	 * 
	 * @return				The connection object
	 */
	private Connection connect() {
        Connection conn = null;
        String url = "jdbc:sqlite:C:\\Users\\Ryan\\eclipse-workspace\\wysiwyg\\src\\main\\resources\\Database.db";
        
        try {
			conn = DriverManager.getConnection(url);
		} 
        catch (SQLException e) {
			System.out.println(e.getMessage());
		}
        
        return conn;
	}
	
	/**
	 * Pulls all elements from the database that share the session Id. Then populate the session
	 * with its child elements. The DTO population should be handled in WysiwygHelper, but that
	 * would require leaving the connection open while the Helper iterates over the ResultSet.
	 * 
	 * @param sessionId		The id of the session that is to be pulled
	 * @param session		The session object that will be populated
	 */
    public void populateSession(int sessionId, WysiwygSession session) {
    	String sql = "";
    	
    	sql +=	"select ";
    	sql +=		"sess_id, ";
    	sql +=		"ent_id, ";
    	sql +=		"text, ";
    	sql +=		"x, ";
    	sql +=		"y, ";
    	sql +=		"size, ";
    	sql +=		"color, ";
    	sql +=		"tilt ";
    	
    	sql +=	"from ";
    	sql +=		"entries ";
    	
    	sql +=	"where ";
    	sql +=		"sess_id = ? ";
    	
    	Connection connection = this.connect();
    	PreparedStatement prep;
    	
    	ResultSet results = null;
    	
		try {
			prep = connection.prepareStatement(sql);
			prep.setInt(1, sessionId);
	    	
	    	results = prep.executeQuery();
	    	
	    	// Iterate over element SQL results and populate the element objects
			while(results.next()) {
				WysiwygElement element = new WysiwygElement();
					
				element.setElementId(results.getInt("ent_id"));
				element.setText(results.getString("text"));
				element.setColor(results.getString("color"));
				element.setSize(results.getInt("size"));
				element.setX(results.getInt("x"));
				element.setY(results.getInt("y"));
				element.setTilt(results.getInt("tilt"));
					
				session.addElement(element);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		// The connection is closed in its own try so that it is not left open if another error is caught
		try {
			connection.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
    }
    
    /**
     * Returns the next available session Id for use on a new session.
     * 
     * @return				The next available session Id
     */
    public Integer selectMaxSess() {
    	String sql = "";
    	
    	sql +=	"select ";
    	sql +=		"max(sess_id) + 1 nextSess ";
    	
    	sql +=	"from ";
    	sql +=		"entries ";
    	
    	Connection connection = this.connect();
    	PreparedStatement prep;
    	
    	Integer max = null;
    	
		try {
			prep = connection.prepareStatement(sql);
	    	max = prep.executeQuery().getInt("nextSess");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		// The connection is closed in its own try so that it is not left open if another error is caught
		try {
			connection.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return max;
    }
	
    /**
     * Insert the values into the elements table.
     * 
     * @param sessionId		The id of the session that is parent to the new element
     * @param elementId		The id of the new entry
     * @param text			The text value of the new entry
     * @param x				The x-location value of the new entry
     * @param y				The y-location value of the new entry
     * @param size			The font size of the new entry
     * @param color			The font color of the new entry
     * @param tilt			The rotation transform of the new entry
     * @return				The new element Id (for sanity check)
     */
	public Integer insert(int sessionId, int elementId, String text, int x, int y, int size, String color, int tilt) {
		String sql = "";
		
		sql += 	"insert into entries(";
		sql += 		"sess_id, ent_id, text, x, y, size, color, tilt";
		sql +=	") ";
		sql +=	"values (";
		sql +=		"?, ?, ?, ?, ?, ?, ?, ?";
		sql +=	")";
		
		Integer result = null;
		
		Connection connection = this.connect();
		PreparedStatement prep;
		try {
			prep = connection.prepareStatement(sql);
			
			prep.setInt(1, sessionId);
			prep.setInt(2, elementId);
			prep.setString(3, text);
			prep.setInt(4, x);
			prep.setInt(5, y);
			prep.setInt(6, size);
			prep.setString(7, color);
			prep.setInt(8, tilt);
			
			prep.executeUpdate();
			
			result = elementId;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			result = null;
		}
		
		// The connection is closed in its own try so that it is not left open if another error is caught
		try {
			connection.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Updates the existing element with new values.
	 * 
	 * @param sessionId		The id of the session that is the parent of the element
	 * @param elementId		The Id of the elemnt to be updated
	 * @param text			The new text value of the element to be updated
	 * @param x				The x-location of the element to be updated
	 * @param y				The y-location of the element to be updated
	 * @param size			The font size of the element to be updated
	 * @param color			The font color of the element to be updated
	 * @param tilt			The rotation transform of the element to be updated
	 * @return				The element Id of the updated element (for sanity check)
	 */
	public Integer update(int sessionId, int elementId, String text, int x, int y, int size, String color, int tilt) {
		String sql = "";
		
		sql +=	"update ";
		sql +=		"entries ";
		
		sql +=	"set ";
		sql +=		"text = ?, ";
		sql +=		"x = ?, ";
		sql +=		"y = ?, ";
		sql +=		"size = ?, ";
		sql +=		"color = ?, ";
		sql +=		"tilt = ? ";
		
		sql +=	"where ";
		sql +=		"sess_id = ? ";
		sql +=		"and ent_id = ?";
		
		Connection connection = this.connect();
		PreparedStatement prep;
		
		Integer result = null;
		
		try {
			prep = connection.prepareStatement(sql);
			
			prep.setString(1, text);
			prep.setInt(2, x);
			prep.setInt(3, y);
			prep.setInt(4, size);
			prep.setString(5, color);
			prep.setInt(6, tilt);
			prep.setInt(7, sessionId);
			prep.setInt(8, elementId);
			
			prep.executeUpdate();
			
			result = elementId;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		// The connection is closed in its own try so that it is not left open if another error is caught
		try {
			connection.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Delete the specified element from the database
	 * 
	 * @param sessionId		The parent session of the element to be deleted
	 * @param elementId		The element id of the element to be deleted
	 * @return				Boolean indicator for whether the delete was successful
	 */
	public Boolean delete(int sessionId, int elementId) {
		String sql = "";
		
		sql +=	"delete from ";
		sql +=		"entries ";
		
		sql +=	"where ";
		sql +=		"sess_id = ? ";
		sql +=		"and ent_id = ?";
		
		Connection connection = this.connect();
		PreparedStatement prep;
		
		Boolean result = true;
		
		try {
			prep = connection.prepareStatement(sql);
			
			prep.setInt(1, sessionId);
			prep.setInt(2, elementId);
			
			prep.executeUpdate();
		} catch (SQLException e) {
			result = false;
			System.out.println(e.getMessage());
		}
		
		// The connection is closed in its own try so that it is not left open if another error is caught
		try {
			connection.close();
		} 
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		return result;
	}
}
