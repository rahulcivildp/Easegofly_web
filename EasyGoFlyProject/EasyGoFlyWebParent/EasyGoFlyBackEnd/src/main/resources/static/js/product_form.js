	dropDownBrands = $("#brands");
	dropDownCategories = $("#categories");
	
	$(document).ready(function(){
		
		$("#shortDescription").richText();
		
		dropDownBrands.change(function(){
			dropDownCategories.empty();
			getCategories();
		});
		
		getCategoriesForNewForm();
		
	});
	
	
	
	
	function getCategoriesForNewForm(){
		catIdField = $("#categoryId").val();
		editMode = false;
		
		if(catIdField.length){
			editMode = true;
		}
		
		if(!editMode && catIdField == 0){
			getCategories();
		}
	}
	
	function getCategories() {
		brandId = dropDownBrands.val();
		url = brandModuleURL + "/" + brandId + "/categories";
		
		alert(url);
		
		$.get(url, function(responseJson) {
			$.each(responseJson, function(index, category) {
				$("<option>").val(category.id).text(category.name).appendTo(dropDownCategories);
			});
		})
	};
	
	function checkNameUnique(form) {
		
		productName = $("#name").val();
		productId = $("#id").val();
		csrfVal = $("input[name='_csrf']").val();
		params = {id: productId, name: productName, _csrf: csrfVal};
			
		$.post(checkedURL, params, function(response) {
			if (response == "OK") {
				form.submit();
			} else if (response == "Duplicate") {
				showModelDialogue("Warning! ", response + " is provided: " + productName);
			} else {
				showModelDialogue("Error", "Unknown response from server...");
			}
		}).fail(function(){
			showModelDialogue("Warning! ", "Unknown response from the server!!!");
		});
		
		return false;
	};
	
	function showModelDialogue(title, message) {
		$("#modalTitle").text(title);
		$("#modalBody").text(message);
		$("#modalDialogue").modal();
	
	};/**
 * 
 */