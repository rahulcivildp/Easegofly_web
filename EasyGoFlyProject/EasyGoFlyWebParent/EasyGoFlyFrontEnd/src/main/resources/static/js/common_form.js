	$(document).ready(function(){
		$("#buttonCancel").click(function(){
			window.location = moduleURL;
		});
		
		$("#fileImage").change(function() {
			fileSize = this.files[0].size;
			imageSizeShort = imageSize * 1024;
			
			if (fileSize > imageSizeShort) {
				this.setCustomValidity("You must choose an image less than "+imageSize+" "+sizeScale+"!");
				this.reportValidity();
			} else {
				this.setCustomValidity("");
				showImageThumbnail(this);
			}
		})
		
		checkEmailUnique();
	});
	
	function showImageThumbnail(fileInput) {
		var file = fileInput.files[0];
		var reader = new FileReader();
		reader.onload = function(e) {
			$("#thumbnail").attr("src", e.target.result);
		}
		
		reader.readAsDataURL(file);
	}
	
	function checkPasswordMatch(confirmPassword) {
		if(confirmPassword.value != $("#password").val()) {
			confirmPassword.setCustomValidity("Passwords do not match!");
		} else {
			confirmPassword.setCustomValidity("");
		}
	}
	
	function checkEmailUnique(form) {
		userEmail = $("#email").val();
		userId = $("#id").val();
		csrfVal = $("input[name='_csrf']").val();
		params = {id: userId, email: userEmail, _csrf: csrfVal};
			
		$.post(url, params, function(response) {
			if (response == "OK") {
				showModelDialogue("Success", "User is Created");
				form.submit();
			} else if (response == "Duplicate Email") {
				showModelDialogue("Warning", response + " is provided: " + userEmail);
			} else {
				showModelDialogue("Error", "Unknown response from server.");
			}
		}).fail(function(){
			showModelDialogue("Error", "Could not connect to the server.");
		});
		
		return false;
	};
	
	function showModelDialogue(title, message) {
		if (message != null) {
			$("#modalTitle").text(message);
			$("#modalBody").text(message);
			$("#modalDialogue").modal();
		};
	};