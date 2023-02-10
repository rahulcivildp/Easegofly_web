$(document).ready(function() {
	$("#logoutLink").click(function(e) {
		e.preventDefault();
		document.logoutForm.submit();
	});
	
	customizeDropdownMenu();
});

function customizeDropdownMenu() {
	$(".customDropdown > a").click(function(){
		location.href = this.href;
	});
};
