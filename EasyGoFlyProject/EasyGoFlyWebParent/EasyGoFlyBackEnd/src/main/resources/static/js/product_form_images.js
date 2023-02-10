	var extraImagesCount = 0;

	$(document).ready(function(){
	
		$("input[name='extraImage']").each(function(index){
			extraImagesCount++;
			
			$(this).change(function(){
				showImageExtraThumbnail(this, index);
			});
		});
		
		$("a[name='linkRemoveExtraImages']").each(function(index) {
			$(this).click(function(){
				removeExtraImage(index);
			});
		});
		
	});
	
	
	function showImageExtraThumbnail(fileInput, index) {
		var file = fileInput.files[0];
		
		fileName = file.name;
		
		imageNameHiddenField = $("#imageName-" + index);
		if(imageNameHiddenField.length) {
			imageNameHiddenField.val(fileName);
		}
		
		var reader = new FileReader();
		reader.onload = function(e) {
			$("#extraThumbnail-" + (index + 1)).attr("src", e.target.result);
		};
		
		reader.readAsDataURL(file);
		if (index >= extraImagesCount - 1) {
			addExtraImageSection(index + 1);
		};
	};
	
	function addExtraImageSection(index) {
		
		htmlAdd = `<div class="col border m-3 p-2" id="divExtraImage-${index + 1}">
				<div id="extraImageHeader-${index + 1}"><label class="mb-3">Extra Images: #${index + 1}</label></div>
				<div>
					<img class="img-thumbnail img-fluid" id="extraThumbnail-${index + 1}" src="${noImageURL}" alt="Photo ${index + 1} Preview" style="max-width: 25% !important;"/>
				</div>
				<div class="mt-3">
					<input type="file" name="extraImage" id="extraImage" accept="image/png, image/jpeg" class="mb-2" onchange="showImageExtraThumbnail(this, ${index})"/>
				</div>
			</div>`;	
			
		htmlLinkRemove = `<a class="btn fa fa-solid fa-circle-xmark fa-2x float-right" href="javascript:removeExtraImage(${index})" title="Remove this image"></a>`;
		
		$("#divProductImages").append(htmlAdd);
		//alert(index);
		$("#extraImageHeader-" + (index)).append(htmlLinkRemove);
		
		extraImagesCount++;
	};
	
	function removeExtraImage(index) {
		$("#divExtraImage-" + index).remove();
	};
	
	
	/**
 * 
 */