var buttonLoadStates;
var dropdownListCountries;
var dropdownStates;
var buttonAddState;
var buttonUpdateState;
var buttonDeleteState;
var labelStateName;
var fieldStateName;

$(document).ready(function(){
	buttonLoadStates = $("#buttonLoadStates");
	dropdownListCountries = $("#dropdownListCountries");
	dropdownStates = $("#dropdownStates");
	buttonAddState = $("#buttonAddState");
	buttonUpdateState = $("#buttonUpdateState");
	buttonDeleteState = $("#buttonDeleteState");
	labelStateName = $("#labelStateName");
	fieldStateName = $("#fieldStateName");
	
	
	buttonLoadStates.click(function() {
		loadStates();
	});
	
	dropdownListCountries.on("change", function() {
		loadStatesForCountry();
	});
	
	dropdownStates.on("change", function() {
		changeFormStatetoSelectedState();
	});
	
	buttonAddState.click(function() {
		if (buttonAddState.val() == "Add") {
			addState();
		} else {
			buttonAddState.val("Add");
			labelStateName.text("State/Province Name: ");
			
			buttonUpdateState.prop("disabled", true);
			buttonDeleteState.prop("disabled", true);
			
			fieldStateName.val("").focus();
			changeFormStatetoNew();
		}
	});
	
	buttonUpdateState.click(function() {
		updateState();
	});
	
	buttonDeleteState.click(function() {
		deleteState();
	});
});



function loadStatesForCountry() {
	selectedCountry = $("#dropdownListCountries option:selected");
	countryId = selectedCountry.val();
	url = contextPath + "states/list_by_country/" + countryId;
	
	$.get(url, function(responseJSON) {
		dropdownStates.empty();
		
		$.each(responseJSON, function(index, state) {
			$("<option>").val(state.id).text(state.name).appendTo(dropdownStates);
		});
	}).done(function() {
		buttonAddState.val("Add");
		labelStateName.text("State/Province Name: ");
		
		buttonUpdateState.prop("disabled", true);
		buttonDeleteState.prop("disabled", true);
		
		fieldStateName.val("").focus();
		
		changeFormStatetoNew();
		showToastMessage("All States have been loaded for the country: " + selectedCountry.text());
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function deleteState() {
	stateId = dropdownStates.val();
	
	url = contextPath + "states/delete/" + stateId;
	
	$.get(url, function() {
		$("#dropdownStates option[value='" + stateId + "']").remove();
		
	
		buttonAddState.val("Add");
		labelStateName.text("State/Province Name: ");
		
		buttonUpdateState.prop("disabled", true);
		buttonDeleteState.prop("disabled", true);
		
		fieldStateName.val("").focus();
		changeFormStatetoNew();
	}).done(function() {
		showToastMessage("The state is Deleted.");
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function updateState() {
	url = contextPath + "states/save";
	stateName = fieldStateName.val();
	
	selectedCountry = $("#dropdownListCountries option:selected");
	countryId = selectedCountry.val();
	countryName = selectedCountry.text();
	
	jsonData = {name: stateName, country: {id: countryId, name: countryName}};
	
	$.ajax({
		type: 'POST',
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		},
		data: JSON.stringify(jsonData),
		contentType: 'application/json'
	}).done(function(countryId) {
		$("#dropdownStates option:selected").text(stateName);
		showToastMessage("Updated state name: " + stateName);
		
		
		buttonAddState.val("Add");
		labelStateName.text("State/Province Name: ");
		
		buttonUpdateState.prop("disabled", true);
		buttonDeleteState.prop("disabled", true);
		
		fieldStateName.val("").focus();
		
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function addState() {
	url = contextPath + "states/save";
	stateName = fieldStateName.val();
	
	selectedCountry = $("#dropdownListCountries option:selected");
	countryId = selectedCountry.val();
	countryName = selectedCountry.text();
	
	if(stateName != ""){
		jsonData = {name: stateName, country: {id: countryId, name: countryName}};
		
		$.ajax({
			type: 'POST',
			url: url,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(csrfHeaderName, csrfValue);
			},
			data: JSON.stringify(jsonData),
			contentType: 'application/json'
		}).done(function(stateId) {
			selectNewlyAddedState(stateId, stateName);
			showToastMessage("Newly added State name: " + stateName);
		}).fail(function() {
			showToastMessage("ERROR: Could not connect to the server.");
		});
	}
};

function selectNewlyAddedState(stateId, stateName) {
	$("<option>").val(stateId).text(stateName).appendTo(dropdownStates);
	
	$("#dropdownStates option[value='" + stateId + "']").prop("selected", true);
	
	fieldStateName.val("").focus();
};

function changeFormStatetoNew() {
	buttonAddState.val("Add");
	labelStateName.text("State/Province Name: ");
	
	buttonUpdateState.prop("disabled", true);
	buttonDeleteState.prop("disabled", true);
	
	fieldStateName.val("").focus();
	
};

function changeFormStatetoSelectedState() {
	buttonAddState.prop("value", "New");
	buttonUpdateState.prop("disabled", false);
	buttonDeleteState.prop("disabled", false);
	
	labelStateName.text("Selected State/Province: ");
	selectedStateName = $("#dropdownStates option:selected").text();
	fieldStateName.val(selectedStateName);
};

function loadStates() {
	url = contextPath + "countries/list";
	$.get(url, function(responseJSON) {
		dropdownListCountries.empty();
		
		$.each(responseJSON, function(index, country) {
			$("<option>").val(country.id).text(country.name).appendTo(dropdownListCountries);
		});
	}).done(function() {
		buttonLoadStates.val("Refresh Country List");
		showToastMessage("All countries are loaded.");
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function showToastMessage(message) {
	$("#toastMessage").text(message);
	$(".toast").toast('show');
};