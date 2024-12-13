$(document).ready(function() {
	$("#logoutLink").click(function(e) {
		e.preventDefault();
		document.logoutForm.submit();
	});
});


