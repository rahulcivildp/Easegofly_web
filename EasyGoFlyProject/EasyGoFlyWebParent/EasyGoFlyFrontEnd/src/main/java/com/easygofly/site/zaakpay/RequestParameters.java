package com.easygofly.site.zaakpay;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RequestParameters {

    //You can use Getter/Setter function to set these values dynamically
    String amount ; //In Paisa
    String buyerEmail = "info.aaladin@gmail.com" ;
    String currency = "INR" ;
    String mode = "0";

    String orderId ;
    String merchantIdentifier ;
    String returnUrl ;

    //Required for updating the transaction
    String updateDesired ; //14 for Full Refund and 22 For Partial
    String updateReason ;


    //For Initiating Payment
    public RequestParameters(String merchantIdentifier, String returnUrl,String orderId,String amount) {
        this.merchantIdentifier = merchantIdentifier;
        this.returnUrl = returnUrl;
        this.orderId = orderId;
        this.amount = amount ;
    }

    //For Checking Payment Status
    public RequestParameters(String orderId, String merchantIdentifier) {
        this.orderId = orderId;
        this.merchantIdentifier = merchantIdentifier;
    }

    //For Refund
    public RequestParameters(String merchantIdentifier,String amount, String orderId, String updateDesired,String updateReason) {
        this.merchantIdentifier = merchantIdentifier;
        this.amount = amount;
        this.orderId = orderId;
        this.updateDesired = updateDesired;
        this.updateReason = updateReason ;
    }

    public TreeMap<String,String> getPaymentRequestParameters() {
        TreeMap<String,String> requestParams = new TreeMap<String, String>();
        //Using TreeMap to pass the parameters in a sorted way to ensure the checksum sequence

        //***********Mandatory Parameters************//
        requestParams.put("orderId",orderId);
        requestParams.put("amount",amount);
        requestParams.put("buyerEmail",buyerEmail);
        requestParams.put("currency",currency);
        requestParams.put("merchantIdentifier",merchantIdentifier);
        requestParams.put("returnUrl",returnUrl);
        //*******************************************//

        //You can add optional parameters in a similar way

        return requestParams;
    }


    public Map<String,String> getTransactionStatusRequestParameters()
    {
        Map<String ,String > requestParams = new HashMap<String, String>();
        requestParams.put("merchantIdentifier",merchantIdentifier);
        requestParams.put("orderId",orderId);

        //You can also pass JSON object here to submit in final form request or you can use the below format string
        requestParams.put("formRequestData","{merchantIdentifier:"+merchantIdentifier+",mode:"+mode+",orderDetail:{orderId:"+orderId+"}}");

        return requestParams;
    }

    public Map<String,String> getFullRefundRequestParameters()
    {
        Map<String,String> requestParams = new HashMap<String, String>();
        requestParams.put("merchantIdentifier",merchantIdentifier);
        requestParams.put("orderId",orderId);
        requestParams.put("mode",mode);
        requestParams.put("updateDesired",updateDesired);
        requestParams.put("updateReason",updateReason);
        return requestParams;
    }

    public Map<String,String> getPartialRefundRequestParameters()
    {
        Map<String,String> requestParams = new HashMap<String, String>();
        requestParams.put("merchantIdentifier",merchantIdentifier);
        requestParams.put("orderId",orderId);
        requestParams.put("mode",mode);
        requestParams.put("updateDesired",updateDesired);
        requestParams.put("updateReason",updateReason);
        requestParams.put("amount",amount); //Mandatory for partial refund
        return requestParams;
    }


}
