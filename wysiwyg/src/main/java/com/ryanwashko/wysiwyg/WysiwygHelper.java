/*
 * This class contains all business logic data transformation between
 * the REST service and the WYSIWYG Application. It also serves as proxy
 * between the JDBC Singleton and the REST service.
 * Copyright Ryan Washko - 2019
 */

package com.ryanwashko.wysiwyg;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WysiwygHelper {
	/**
	 * Pulls the next available session Id and returns an empty session object
	 * with the returned value.
	 * 
	 * @return				WysiwygSession with the freshly reserved session Id 
	 */
	public static WysiwygSession getNextSession() {
		WysiwygSession nextSession = new WysiwygSession();
		
		Integer max = WysiwygJDBCHelper.getInstance().selectMaxSess();
		
		nextSession.setSessionId(max);
		
		return nextSession;
	}
	
	/**
	 * Pulls the session object and all child elements for the session with the given Id.
	 * 
	 * @param sessionId		Id of the desired session object
	 * @return				A session object with all child elements
	 */
	public static WysiwygSession getSession(int sessionId) {
		WysiwygSession session = new WysiwygSession();
		session.setSessionId(sessionId);
		
		WysiwygJDBCHelper.getInstance().populateSession(sessionId, session);
		
		return session;
	}
	
	/**
	 * This checks whether a user-entered session Id is valid and useable.
	 * 
	 * @param sessionId		Id of the session to check
	 * @return				Boolean indicator of whether the session is valid
	 */
	public static Boolean checkSession(int sessionId) {
		WysiwygSession session = WysiwygHelper.getSession(sessionId);
		
		if(null == session) {
			return false;
		}
		
		return !session.getElements().isEmpty();
	}
	
	/**
	 * Updates the element with the new values.
	 * 
	 * @param sessionId		Id of the session that is parent to the element
	 * @param element		Id of the element to be updated
	 * @return				The element with updated values
	 */
	public static WysiwygElement updateElement(int sessionId, WysiwygElement element) {
		Integer elementId = WysiwygJDBCHelper.getInstance().update(
				sessionId, element.getElementId(), element.getText(), element.getX(), element.getY(), 
				element.getSize(), element.getColor(), element.getTilt());
		
		if(null != elementId) {
			return element;
		}
		else {
			// a null elementId indicates an error on JDBC update. Return null so the javascript
			// knows not to update the element.
			return null;
		}
	}
	
	/**
	 * Creates a new element with the given Id for the given session parent. This also sets
	 * the initial values of the element.
	 * 
	 * @param sessionId		Id of the session to which the new element will belong
	 * @param elementId		Expected Id of the new element
	 * @return				The newly created element
	 */
	public static WysiwygElement createElement(int sessionId, int elementId) {
		WysiwygElement newElement = new WysiwygElement();
		newElement.setText("New Element");
		newElement.setX(100);
		newElement.setY(100);
		newElement.setTilt(45);
		newElement.setColor("green");
		newElement.setSize(48);
		
		Integer newElementId = WysiwygJDBCHelper.getInstance().insert(
				sessionId, elementId, newElement.getText(), newElement.getX(), newElement.getY(), 
				newElement.getSize(), newElement.getColor(), newElement.getTilt());
		
		if(null != newElementId) {
			// a null elementId indicates an error on JDBC update. Return null so the javascript
			// knows not to populate the element in the UI.
			return newElement;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Delete the element with the given session Id and element Id from the database.
	 * 
	 * @param sessionId		Session Id of the element to be deleted
	 * @param elementId		Element Id of the element to be deleted
	 * @return				Boolean indicator if the delete was successfull
	 */
	public static Boolean deleteElement(int sessionId, int elementId) {
		return WysiwygJDBCHelper.getInstance().delete(sessionId, elementId);
	}
}
