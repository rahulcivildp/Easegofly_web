 
$(document).ready(function(){
	var device = getDeviceName();
	var infoDevice = checkDetailedBrowser();
	
	$(".device").attr("value", device);
	$(".deviceInfo").attr("value", infoDevice);
	
	console.log(device);
	console.log(infoDevice);
});
	
	
function checkDetailedBrowser(){
	var nVer = navigator.appVersion;
	var nAgt = navigator.userAgent;
	var browserName  = navigator.appName;
	var fullVersion  = ''+parseFloat(navigator.appVersion); 
	var majorVersion = parseInt(navigator.appVersion,10);
	var nameOffset,verOffset,ix;
	
	// In Opera, the true version is after "OPR" or after "Version"
	if ((verOffset=nAgt.indexOf("OPR"))!=-1) {
	 browserName = "Opera";
	 fullVersion = nAgt.substring(verOffset+4);
	 if ((verOffset=nAgt.indexOf("Version"))!=-1) 
	   fullVersion = nAgt.substring(verOffset+8);
	}
	// In MS Edge, the true version is after "Edg" in userAgent
	else if ((verOffset=nAgt.indexOf("Edg"))!=-1) {
	 browserName = "Microsoft Edge";
	 fullVersion = nAgt.substring(verOffset+4);
	}
	// In MSIE, the true version is after "MSIE" in userAgent
	else if ((verOffset=nAgt.indexOf("MSIE"))!=-1) {
	 browserName = "Microsoft Internet Explorer";
	 fullVersion = nAgt.substring(verOffset+5);
	}
	// In Chrome, the true version is after "Chrome" 
	else if ((verOffset=nAgt.indexOf("Chrome"))!=-1) {
	 browserName = "Chrome";
	 fullVersion = nAgt.substring(verOffset+7);
	}
	// In Safari, the true version is after "Safari" or after "Version" 
	else if ((verOffset=nAgt.indexOf("Safari"))!=-1) {
	 browserName = "Safari";
	 fullVersion = nAgt.substring(verOffset+7);
	 if ((verOffset=nAgt.indexOf("Version"))!=-1) 
	   fullVersion = nAgt.substring(verOffset+8);
	}
	// In Firefox, the true version is after "Firefox" 
	else if ((verOffset=nAgt.indexOf("Firefox"))!=-1) {
	 browserName = "Firefox";
	 fullVersion = nAgt.substring(verOffset+8);
	}
	// In most other browsers, "name/version" is at the end of userAgent 
	else if ( (nameOffset=nAgt.lastIndexOf(' ')+1) < 
	          (verOffset=nAgt.lastIndexOf('/')) ) 
	{
	 browserName = nAgt.substring(nameOffset,verOffset);
	 fullVersion = nAgt.substring(verOffset+1);
	 if (browserName.toLowerCase()==browserName.toUpperCase()) {
	  browserName = navigator.appName;
	 }
	}
	// trim the fullVersion string at semicolon/space if present
	if ((ix=fullVersion.indexOf(";"))!=-1)
	   fullVersion=fullVersion.substring(0,ix);
	if ((ix=fullVersion.indexOf(" "))!=-1)
	   fullVersion=fullVersion.substring(0,ix);
	
	majorVersion = parseInt(''+fullVersion,10);
	if (isNaN(majorVersion)) {
	 fullVersion  = ''+parseFloat(navigator.appVersion); 
	 majorVersion = parseInt(navigator.appVersion,10);
	}
	
    let device = "Unknown";
    const ua = {
        "Generic Linux": /Linux/i,
        "Android": /Android/i,
        "BlackBerry": /BlackBerry/i,
        "Bluebird": /EF500/i,
        "Chrome OS": /CrOS/i,
        "Datalogic": /DL-AXIS/i,
        "Honeywell": /CT50/i,
        "iPad": /iPad/i,
        "iPhone": /iPhone/i,
        "iPod": /iPod/i,
        "macOS": /Macintosh/i,
        "Windows": /IEMobile|Windows/i,
        "Zebra": /TC70|TC55/i,
    }
    
    Object.keys(ua).map(v => navigator.userAgent.match(ua[v]) && (device = v));
    
    var modelNum = "Unknown";
	navigator.userAgentData
      .getHighEntropyValues([
        "architecture",
        "model",
        "platform",
        "platformVersion",
        "fullVersionList"
      ])
      .then((ua) => {
        const model = ua["model"];
        modelNum = model;
        if (model)  modelNum = model;
      });
      
      var data = "<p style='color: green; margin: 0;'>Device = " + device + ",<br>\r\n"
      		+ "Browser name = " + browserName + ",<br>\r\n"
			+ "Full version = " + fullVersion + ",<br>\r\n"
			+ "Major version = " + majorVersion + ",<br>\r\n"
			+ "navigator.appName = " + navigator.appName + ",<br>\r\n"
			+ "navigator.userAgent = " + navigator.userAgent + ",<br>\r\n"
			+ "Phone: " + modelNum +"</p>";
	
	return data;
	
}

function getDeviceName() {
    let device = "Unknown";
    const ua = {
        "Generic Linux": /Linux/i,
        "Android": /Android/i,
        "BlackBerry": /BlackBerry/i,
        "Bluebird": /EF500/i,
        "Chrome OS": /CrOS/i,
        "Datalogic": /DL-AXIS/i,
        "Honeywell": /CT50/i,
        "iPad": /iPad/i,
        "iPhone": /iPhone/i,
        "iPod": /iPod/i,
        "macOS": /Macintosh/i,
        "Windows": /IEMobile|Windows/i,
        "Zebra": /TC70|TC55/i,
    }
    
    Object.keys(ua).map(v => navigator.userAgent.match(ua[v]) && (device = v));
    
    return "<p style='color: green; margin: 0;'>" + device + "</p>";
}