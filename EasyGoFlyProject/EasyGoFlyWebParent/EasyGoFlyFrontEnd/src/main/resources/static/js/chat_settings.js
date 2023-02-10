var addConversation;
var chatBodyDiv;
var request_id;

$(document).ready(function(){
	addConversation = $("#addConversation");
	chatBodyDiv = $("#chatBodyDiv");
	request_id = $("#request_id");
	

	
});

function loadConversation() {
	url = contextPath + "request/conversation_list";
	
	
	$.ajax({
		type: 'GET',
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		},
		data: JSON.stringify(jsonData),
		contentType: 'application/json'
	}).done(function() {
		alert("Successful");
	}).fail(function() {
		alert("Failed to load!");
	});
};