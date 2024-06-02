
	function calenderViewNew(input) {
		var today = new Date().toISOString().split('T')[0];
		$(input).attr('min', today);
	};