// HTML WYSIWYG Application - Copyright Ryan Washko - 2019

var sessionId = null;
var nextElementId = 1;

// Upon loading the DOM, add the necessary listeners to interactive elements
$(function(){
	$("#new-session").click(createNewSession);
	$("#load-session").click(loadUserSession);
	$("#add-button").click(addNewElement);
	$("#download-file").click(generateHTMLFile);
});

// Use jQuery ajax call to pull a new session from the REST service
function createNewSession() {
	$.ajax({
		type: "GET",
		url: ("/wysiwyg/nextSession"),
		dataType: "json",
		success: function(response){
			sessionId = response['sessionId'];
			$("#overlay").remove();
			log("Starting a new WYSIWYG session. Click \"Add Element\" to begin.<br>"
					+ "If you wish to continue working after closing this window, your session ID is " + sessionId);
        },
        error: function(e){
        	log("There was an error creating a new session, please try again later.");
        }
	});	
}

// Load the user-specified session into the WYSIWYG editor (if valid).
function loadUserSession() {
	sessionId = $("#session-id").val();
	
	// validate the input
	if(sessionId == "") {
		preemptivelyFailLoad();
		return;
	}
	
	$.ajax({
		type: "GET",
		url: ("/wysiwyg/checkSession/" + sessionId),
		dataType: "json",
		success: function(response){
			if(response) {
				$("#overlay").remove();
				log("Loading session " + sessionId);
				loadElementsFromSession(sessionId);
			}
			else {
				preemptivelyFailLoad();
			}
        }
	});	
}

// Before the overlay is removed, the console is not visible. Instead, display an error message in the instructions.
function preemptivelyFailLoad() {
	$("#instructions").html("Sorry, but the session ID you entered is not valid.<br><br>"
			+ "Please enter a different ID and try again, or click \"New Session\" to create a new session.");
}

// Use jQuery ajax call to pull the session and its elements, then load them into the WYSIYG editor.
function loadElementsFromSession(sessionNumber) {
	$.ajax({
		type: "GET",
		url: ("/wysiwyg/retrieveSession/" + sessionNumber),
		dataType: "json",
		success: function(response){
			var elements = response["elements"];
			
			for(var i = 0; i < elements.length; i++){
				var element = elements[i];
				
				// always keep track of the maximum element number so an identifier is not duplicated.
				if(element["elementId"] >= nextElementId) {
					nextElementId = element["elementId"] + 1;
				}
				
				addToControls(element["elementId"], element);
				addToCanvas(element["elementId"]);
			}
			log("Session " + sessionNumber + " has successfully loaded");
        },
        error: function(e){
        	log("There was an error loading session " + sessionNumber + ".");
        }
	});
}

// Use jQuery ajax call to request a new element, then add it to the WYSIWYG editor
function addNewElement() {
	log("Adding a new element to the canvas. You may use the controls to modify the new "
				+ "element. You may also drag and drop the element within the canvas.");
	
	$.ajax({
		type: "POST",
		url: "/wysiwyg/createElement/" + sessionId + "/" + nextElementId,
		dataType: 'json',
		success: function(response) {
			addToControls(nextElementId, response);
			addToCanvas(nextElementId);
			nextElementId++;
        },
        error: function(e){
        	log("There was a problem creating a new element.");
        }
	});
}

// Generate the HTML for a set of controls and set the appropriate values
function addToControls(elementId, element) {
	var contents = element['text'];
	var color = element['color'];
	var size = element['size'];
	var tilt = element['tilt'];
	var x = element['x'];
	var y = element['y'];
	
	var controlHTML = "";
	
	controlHTML += 	"<div id=\"control" + elementId + "\" class=\"control-wrapper round\">";
	controlHTML += 		"<div>Value:</div><input type=\"text\" id=\"text" + elementId + "\" class=\"round text-input\" value=\"" + contents + "\">"
	controlHTML += 		"<div>X:</div><input type=\"number\" id=\"x" + elementId + "\" class=\"round position-input\" value=\"" + x + "\">"
	controlHTML += 		"<div>Y:</div><input type=\"number\" id=\"y" + elementId + "\" class=\"round position-input\" value=\"" + y + "\">"
	controlHTML +=		"<div>Size:</div><select id=\"size" + elementId + "\" class=\"round select-input\">"
	controlHTML +=			generateSizeOptions(size);
	controlHTML +=		"</select>"
	controlHTML +=		"<div>Color:</div><select id=\"color" + elementId + "\" class=\"round select-input\" value=\"" + color + "\">"
	controlHTML +=			generateColorOptions(color);
	controlHTML +=		"</select>"
	controlHTML +=		"<div>Tilt:</div><div id=\"tilt" + elementId + "\" class=\"tilt-input\"></div>"
	controlHTML += 		"<div id=\"remove" + elementId + "\" class=\"remove-input round button\">Remove</div>";
	controlHTML += 	"</div>";
	
	$("#controls").append(controlHTML);
	
	generateListeners(elementId, tilt);
}

// Programmatically generate all the dropdown options for font size.
function generateSizeOptions(selectedSize) {
	var maxSize = 100;
	
	var options = "";
	
	for(var i = 10; i <= maxSize; i += 2) {
		var selected = "";
		if(selectedSize == i) {
			selected = " selected ";
		}
		
		options += "<option" + selected + " value=\"" + i + "\">" + i + "</option>";
	}
	
	return options;
}

//Programmatically generate all the dropdown options for font color.
function generateColorOptions(selectedColor) {
	var colors = ["black", "silver", "red", "orange", "yellow", "green", "blue", "cyan", "purple", "maroon", "olive"];
	var options = "";
	
	for(var i=0; i<colors.length; i++){
		var selected = "";
		if(selectedColor == colors[i]) {
			selected = " selected ";
		}
		
		options += "<option" + selected + " value=\"" + colors[i] + "\">" + colors[i] + "</option>";
	}
	
	return options;
}

// Once the new control HTML has been added to the DOM, add listeners to allow them to control the canvas.
function generateListeners(elementId, tiltVal) {
	$(function(){
		$("#tilt" + elementId).slider({
			max: 180,
			min: -180,
			value: tiltVal,
			slide: function() {transformElement(elementId);}
		});
		
		// use keyup listeners on text boxes. The overhead is not huge, and there's a
		// chance of data loss if text is changed and the window is closed without losing
		// focus. A change listener is good enough on drop-downs.
		$("#text" + elementId).keyup(function(){transformElement(elementId)});
		$("#x" + elementId).keyup(function(){transformElement(elementId)});
		$("#y" + elementId).keyup(function(){transformElement(elementId)});
		$("#size" + elementId).change(function(){transformElement(elementId)});
		$("#color" + elementId).change(function(){transformElement(elementId)});
		$("#remove" + elementId).click(function(){deleteElement(elementId)});
	});
}

// Generate the new element, add it to the canvas, and make it draggable via jQuery.
// Also make a drag and drop update the corresponding control.
function addToCanvas(elementId) {
	$(function(){
		var text = $("#text" + elementId).val();
		var x = $("#x" + elementId).val();
		var y = $("#y" + elementId).val();
		var size = $("#size" + elementId).val();
		var color = $("#color" + elementId).val();
		var tilt = $("#tilt" + elementId).slider("option", "value");
		
		// The new element with all applicable in line CSS.
		var newElement = "";
		newElement +=	"<div class=\"draggable\" id=\"" + elementId + "\" style=\"position:absolute; ";
		newElement += 		"color:" + color + "; ";
		newElement += 		"font-size:" + size + "px; ";
		newElement += 		"top:" + y + "px; ";
		newElement += 		"left:" + x + "px; ";
		newElement +=		"transform: rotate(" + tilt + "deg);\">"
		newElement += 		text + "</div>";
		
		$("#canvas").append(newElement);
		
		// Make the element draggable. Also add the stop listener to update the element's controls.
		$("#" + elementId).draggable({
			containment: 'parent',
			stop: function(){
				$("#x" + elementId).val(Math.round($("#" + elementId).css("left").replace("px","")));
				$("#y" + elementId).val(Math.round($("#" + elementId).css("top").replace("px","")));
				transformElement(elementId);
			}
		});
	});
}

// Use jQuery ajax call to the REST service to request an element be deleted. Once this is complete,
// remove it from the controls and canvas.
function deleteElement(elementId) {
	log("Deleting element: \"" + $("#text" + elementId).val() + "\" from the canvas...");

	$.ajax({
		type: "DELETE",
		url: ("/wysiwyg/deleteElement/" + sessionId + "/" + elementId),
		success: function() {
			$("#control" + elementId).remove();
			$("#" + elementId).remove();
			log("Delete successful.");
        },
        error: function(e){
        	log("There was a problem deleting the element");
        }
	});
}

// Pull an element's values from it's corresponding controls. Using jQuery ajax call, update the element using
// the REST service. Once this is complete, transform the canvas element to match the controls.
function transformElement(elementId) {
	var newText = $("#text" + elementId).val();
	var newX = $("#x" + elementId).val();
	var newY = $("#y" + elementId).val();
	var newSize = $("#size" + elementId).val();
	var newColor = $("#color" + elementId).val();
	var newTilt = $("#tilt" + elementId).slider("option", "value") + "deg";
	
	log("Updating element values...");
	log("Text: " + newText + ", X: " + newX + ", Y: " + newY + ", Size: " + newSize + ", Color: " 
			+ newColor + ", Tilt: " + newTilt);
	
	var updatedElement = {
		"text" : newText,
		"elementId" : elementId,
		"x" : newX,
		"y" : newY,
		"size" : newSize,
		"color" : newColor,
		"tilt" : newTilt.replace("deg","")
	}
	
	$.ajax({
		type: "PUT",
		url: "/wysiwyg/updateElement/" + sessionId,
		contentType: 'application/json',
		data: JSON.stringify(updatedElement),
		success: function(response) {
			console.log(response);
			var element = $("#" + elementId);
			element.html(newText);
			element.css("color", newColor);
			element.css("font-size", newSize);
			element.css("top", newY);
			element.css("left", newX);
			element.css("transform", "rotate(" + newTilt);
			
			log("Element sucessfully updated.");
        },
        error: function(e){
        	log("There was an error, the element was not updated.");
        }
	});
}

// Generate a file containing the canvas HTML. Then, generate a dummy link to download the file and mimic 
// a click of the invisible link.
function generateHTMLFile() {
	var fileContents = "";
	fileContents += "<html><body>";
	fileContents += $("#canvas").html();
	fileContents += "</body></html>";
	
	var tempLink = document.createElement('a');
    tempLink.setAttribute('href', 'data:text/html;charset=utf-8,' + encodeURIComponent(fileContents));
    tempLink.setAttribute('download', 'wysiwygHtml');

    tempLink.style.display = 'none';
    document.body.appendChild(tempLink);

    tempLink.click();

    document.body.removeChild(tempLink);
}

// Log the message to the console window and scroll to the bottom so the newest message is visible.
function log(message){
	$("#console").append(message + "<br>");
	$("#console").scrollTop($("#console")[0].scrollHeight);
}