/*
 * Basic controller for the WYSIWYG Spring Boot Application
 * Copyright Ryan Washko - 2019
 */

package com.ryanwashko.wysiwyg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class WysiwygApplication {
    public static void main(String[] args) {
        SpringApplication.run(WysiwygApplication.class, args);
    }
}

@RestController
class WysiwygController {
	/**
	 * Verify that the requested session exists 
	 * 
	 * @param sessionId 	The id of the session being validated
	 * @return				boolean indicating the session validity
	 */
	@GetMapping("/wysiwyg/checkSession/{sessionId}")
    Boolean checkSession(@PathVariable int sessionId) {
    	return WysiwygHelper.checkSession(sessionId);
    }
	
	/**
	 * Pull a bare session object, including the next availably session Id.
	 * 
	 * @return				The fresh session object
	 */
    @GetMapping("/wysiwyg/nextSession")
    WysiwygSession getNextSession() {
    	return WysiwygHelper.getNextSession();
    }
    
    /**
     * Pull a session and all corresponding elements
     * 
     * @param sessionId		The id of the session being retrieved
     * @return				The session object and its child elements
     */
    @GetMapping("/wysiwyg/retrieveSession/{sessionId}")
    WysiwygSession retrieveSession(@PathVariable int sessionId) {
    	return WysiwygHelper.getSession(sessionId);
    }
    
    /**
     * Replace the element on the specified session with the new element contained in the request.
     * 
     * @param sessionId		The id of the session that is parent to the applicable element
     * @param element		The element object with its updated values
     * @return				The element object and its updated values - Sanity check
     */
    @PutMapping("/wysiwyg/updateElement/{sessionId}") 
    WysiwygElement updateElement(@PathVariable int sessionId, @RequestBody WysiwygElement element){
    	return WysiwygHelper.updateElement(sessionId, element);
    }
    
    /**
     * Create a new element for the session and give it starting values.
     * 
     * @param sessionId		The session that is getting a new element
     * @param elementId		The desired element id of the new element
     * @return				The newly created element object
     */
    @PostMapping("/wysiwyg/createElement/{sessionId}/{elementId}") 
    WysiwygElement createElement(@PathVariable int sessionId, @PathVariable int elementId){
    	return WysiwygHelper.createElement(sessionId, elementId);
    }
    
    /**
     * Delete the element with the given element id and session id.
     * 
     * @param sessionId		The session of the element being deleted
     * @param elementId		The id of the element being deleted
     */
    @DeleteMapping("/wysiwyg/deleteElement/{sessionId}/{elementId}") 
    void deleteElement(@PathVariable int sessionId, @PathVariable int elementId){
    	WysiwygHelper.deleteElement(sessionId, elementId);
    }
}