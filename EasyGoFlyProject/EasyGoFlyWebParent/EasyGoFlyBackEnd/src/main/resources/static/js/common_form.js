$(document).ready(function(){
		$("#buttonCancel").click(function(){
			window.location = moduleURL;
		});
		
		$("textarea[name='FAQ']").richText();
		$("textarea[name='ABOUT_US']").richText();
		$("textarea[name='TERMS']").richText();
		$("textarea[name='PRIVACY']").richText();
		$("textarea[name='AGREEMENT']").richText();
		$("textarea[name='SECURITY']").richText();
		$("textarea[name='SERVICE']").richText();
		$("textarea[name='BANK']").richText();
		$("textarea[name='CONTACT_US']").richText();
		$("textarea[name='ADMIN_ADDRESS']").richText();
		$("#customerVerificationContent").richText();
		$("#ORDER_CONFIRMATION_CONTENT").richText();
		$("#HOTEL_SUCCESS_CONTENT").richText();
		$("#BUS_SUCCESS_CONTENT").richText();
		$("#HOLIDAY_SUCCESS_CONTENT").richText();
		$("#WALLET_RECHARGE_CONTENT").richText();
		$("textarea[name='REFUND_CANCELLATION']").richText();
		
		var length = $("textarea[name='FAQ']").val().length;
		$("#no-1").text(length);
		var length = $("textarea[name='ABOUT_US']").val().length;
		$("#no-2").text(length);
		var length = $("textarea[name='TERMS']").val().length;
		$("#no-3").text(length);
		var length = $("textarea[name='PRIVACY']").val().length;
		$("#no-4").text(length);
		var length = $("textarea[name='AGREEMENT']").val().length;
		$("#no-5").text(length);
		var length = $("textarea[name='SECURITY']").val().length;
		$("#no-6").text(length);
		var length = $("textarea[name='SERVICE']").val().length;
		$("#no-7").text(length);
		var length = $("textarea[name='BANK']").val().length;
		$("#no-8").text(length);
		var length = $("textarea[name='CONTACT_US']").val().length;
		$("#no-9").text("length");
		
		
		
		
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
	});
	
	function onChangeImage(imageId, imageSize) {
	
		$(imageId).change(function() {
			var fileSize = this.files[0].size;
			var imageSizeShort = imageSize * 1024;
			
			if (fileSize > imageSizeShort) {
				this.setCustomValidity("You must choose an image less than "+imageSize+" mb!");
				this.reportValidity();
			} else {
				this.setCustomValidity("");
				showImageThumbnail(this);
			}
		});
	}
	
	function onUploadImage(inputId, imgId) {
        let inputElement = document.getElementById(inputId);
        let imgElement = document.getElementById(imgId);

        inputElement.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                let reader = new FileReader();

                reader.onload = function (event) {
                    imgElement.setAttribute('src', event.target.result);
                };

                reader.readAsDataURL(this.files[0]);
            }
        });
    }
	
	function showImageThumbnail(fileInput) {
		var file = fileInput.files[0];
		var reader = new FileReader();
		reader.onload = function(e) {
			$("#thumbnail").attr("src", e.target.result);
		}
		
		reader.readAsDataURL(file);
	}
	
	function checkEmailUnique(form) {
		userEmail = $("#email").val();
		userId = $("#id").val();
		csrfVal = $("input[name='_csrf']").val();
		params = {id: userId, email: userEmail, _csrf: csrfVal};
			
		$.post(emailUrl, params, function(response) {
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
	
