package com.ryanwashko.wysiwyg;

// DTO representative of a single element in the WYSIWYG editor.
// Copyright Ryan Washko - 2019
public class WysiwygElement {
	private int elementId;
	private String text;
	private int x;
	private int y;
	private int size;
	private String color;
	private int tilt;
	
	public int getElementId() {
		return this.elementId;
	}
	
	public void setElementId(int elementId) {
		this.elementId = elementId;
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getX() {
		return this.x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getColor() {
		return this.color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public int getTilt() {
		return this.tilt;
	}
	
	public void setTilt(int tilt) {
		this.tilt = tilt;
	}
}