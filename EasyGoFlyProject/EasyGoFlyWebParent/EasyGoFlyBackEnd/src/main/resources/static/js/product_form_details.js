$(document).ready(function(){
	
	$("a[name='linkRemoveDetail']").each(function(index) {
		$(this).click(function(){
			removeDetailByIndex(index);
		});
	});
		
});
	

function addNextDetails() {
	allDivDetails = $("[id^='divDetails-']");
	addCloseButton = $("[id^='addCloseButton-']");
	
	divDetailsCount = allDivDetails.length;
	addCloseButtonCount = addCloseButton.length;
	
	htmlDetailSection = `
		<div class="row w-75 m-3" id="divDetails-${divDetailsCount}">
			<div class="form-group col-sm-5 row">
				<input type="hidden" name="detailIDs" value="0">
				<label class="col-sm-2 col-form-label">Name:</label>
				<div class="col-sm-10">
					<input type="text" class="form-control" name="detailNames" maxlength="255">
				</div> 
			</div>
			<div class="form-group col-sm-5 row">
				<label class="col-sm-2 col-form-label">Value:</label>
				<div class="col-sm-10">
					<input type="text" class="form-control" name="detailValues" maxlength="255">
				</div> 
			</div>
			<div class="form-group col-sm-2 row" id="addCloseButton-${addCloseButtonCount}">
				
			</div>
		</div>
	`;
	
	$("#divProductDetails").append(htmlDetailSection);
	
	previousDivSelectionButton = addCloseButton.last();
	previousDivSelectionDiv = allDivDetails.last();
	previousDivSelectionDivId = previousDivSelectionDiv.attr("id")
	
	htmlLinkRemove = `<a class="btn fa fa-solid fa-circle-xmark fa-2x float-left" href="javascript:removeDetailsSectionById('${previousDivSelectionDivId}')" title="Remove this details"></a>`;
	
	previousDivSelectionButton.append(htmlLinkRemove);
	
	$("input[name='detailNames']").last().focus();
}

function removeDetailsSectionById(id){
	$("#" + id).remove();
}

function removeDetailByIndex(index) {
	$("#divDetails-" + index).remove();
}