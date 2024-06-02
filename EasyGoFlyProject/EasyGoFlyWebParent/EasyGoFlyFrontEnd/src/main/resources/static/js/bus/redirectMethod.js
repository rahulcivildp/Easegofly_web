
	function redirectToBus (input) {
		urlParse = window.location.href;
		newUrl = urlParse + input;
		alert(newUrl);
		//window.location.replace(newUrl);
	}