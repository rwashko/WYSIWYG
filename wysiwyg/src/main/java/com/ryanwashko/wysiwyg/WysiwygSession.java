package com.ryanwashko.wysiwyg;

import java.util.ArrayList;

//DTO representative of a session in the WYSIWYG editor and its children.
//Copyright Ryan Washko - 2019
public class WysiwygSession {
	private int sessionId;
	private ArrayList<WysiwygElement> elements;
	
	public WysiwygSession() {
		this.elements = new ArrayList<WysiwygElement>();
	}

	public int getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
	
	public ArrayList<WysiwygElement> getElements() {
		return this.elements;
	}
	
	public void addElement(WysiwygElement newElement) {
		this.elements.add(newElement);
	}
}
