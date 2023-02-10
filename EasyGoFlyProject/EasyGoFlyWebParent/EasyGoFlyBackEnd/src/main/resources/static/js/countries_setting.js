var buttonLoadCountries;
var dropdownCountries;
var buttonAddCountry;
var buttonUpdateCountry;
var buttonDeleteCountry;
var labelCountryName;
var fieldCountryName;
var fieldCountryCode;

$(document).ready(function(){
	buttonLoadCountries = $("#buttonLoadCountries");
	dropdownCountries = $("#dropdownCountries");
	buttonAddCountry = $("#buttonAddCountry");
	buttonUpdateCountry = $("#buttonUpdateCountry");
	buttonDeleteCountry = $("#buttonDeleteCountry");
	labelCountryName = $("#labelCountryName");
	fieldCountryName = $("#fieldCountryName");
	fieldCountryCode = $("#fieldCountryCode");
	
	
	buttonLoadCountries.click(function() {
		loadCountries();
	});
	
	dropdownCountries.on("change", function() {
		changeFormStatetoSelectedCountry();
	});
	
	buttonAddCountry.click(function() {
		if (buttonAddCountry.val() == "Add") {
			addCountry();
		} else {
			changeFormStatetoNew();
		}
	});
	
	buttonUpdateCountry.click(function() {
		updateCountry();
	});
	
	buttonDeleteCountry.click(function() {
		deleteCountry();
	});
	
	customizableTabs();
});



function deleteCountry() {
	optionValue = dropdownCountries.val();
	countryId = optionValue.split("-")[0];
	
	url = contextPath + "countries/delete/" + countryId;
	
	$.get(url, function() {
		$("#dropdownCountries option[value='" + optionValue + "']").remove();
		changeFormStatetoNew();
	}).done(function() {
		showToastMessage("All country is Deleted.");
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function updateCountry() {
	url = contextPath + "countries/save";
	countryName = fieldCountryName.val();
	countryCode = fieldCountryCode.val();
	
	dropdownCountries.val().split("-")[0];
	
	jsonData = {name: countryName, code: countryCode};
	
	$.ajax({
		type: 'POST',
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		},
		data: JSON.stringify(jsonData),
		contentType: 'application/json'
	}).done(function(countryId) {
		$("#dropdownCountries option:selected").text(countryId + "-" + countryCode);
		$("#dropdownCountries option:selected").text(countryName);
		showToastMessage("Updated country name: " + countryName);
		
		changeFormStatetoNew();
		
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function addCountry() {
	url = contextPath + "countries/save";
	countryName = fieldCountryName.val();
	countryCode = fieldCountryCode.val();
	
	if(countryName != ""){
		jsonData = {name: countryName, code: countryCode};
		
		$.ajax({
			type: 'POST',
			url: url,
			beforeSend: function(xhr) {
				xhr.setRequestHeader(csrfHeaderName, csrfValue);
			},
			data: JSON.stringify(jsonData),
			contentType: 'application/json'
		}).done(function(countryId) {
			selectNewlyAddedCountry(countryId, countryCode, countryName);
			showToastMessage("Newly added country name: " + countryName);
		}).fail(function() {
			showToastMessage("ERROR: Could not connect to the server.");
		});
	}
};

function selectNewlyAddedCountry(countryId, countryCode, countryName) {
	optionValue = countryId + "-" + countryCode;
	$("<option>").val(optionValue).text(countryName).appendTo(dropdownCountries);
	
	$("#dropdownCountries option[value='" + optionValue + "']").prop("selected", true);
	fieldCountryCode.val("");
	fieldCountryName.val("").focus();
};

function changeFormStatetoNew() {
	buttonAddCountry.val("Add");
	labelCountryName.text("Country Name: ");
	
	buttonUpdateCountry.prop("disabled", true);
	buttonDeleteCountry.prop("disabled", true);
	
	fieldCountryCode.val("");
	fieldCountryName.val("").focus();
	
};

function changeFormStatetoSelectedCountry() {
	buttonAddCountry.prop("value", "New");
	buttonUpdateCountry.prop("disabled", false);
	buttonDeleteCountry.prop("disabled", false);
	
	labelCountryName.text("Selected Country: ");
	selectedCountryName = $("#dropdownCountries option:selected").text();
	fieldCountryName.val(selectedCountryName);
	
	countryCode = dropdownCountries.val().split("-")[1];
	fieldCountryCode.val(countryCode);
};

function loadCountries() {
	url = contextPath + "countries/list";
	$.get(url, function(responseJSON) {
		dropdownCountries.empty();
		
		$.each(responseJSON, function(index, country) {
			optionValue = country.id + "-" + country.code;
			$("<option>").val(optionValue).text(country.name).appendTo(dropdownCountries);
		});
	}).done(function() {
		buttonLoadCountries.val("Refresh Country List");
		showToastMessage("All countries are loaded.");
	}).fail(function() {
		showToastMessage("ERROR: Could not connect to the server.");
	});
};

function showToastMessage(message) {
	$("#toastMessage").text(message);
	$(".toast").toast('show');
};

function customizableTabs(){
	//javascript to enable link to tab.
	var url = document.location.toString();
	if(url.match('#')){
		$('.nav-tabs a[href="#' + url.split('#')[1] + '"]').tab('show');
	}
	
	//change hash for page-reload
	$('.nav-tabs a').on('shown.bs.tab', function(e){
		window.location.hash = e.target.hash;
	})
}