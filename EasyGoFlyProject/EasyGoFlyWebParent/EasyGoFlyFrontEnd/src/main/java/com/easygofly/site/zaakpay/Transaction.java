package com.easygofly.site.zaakpay;

public class Transaction {

    //************************************************* INITIATE ONE-WAY PAYMENT ***************************************************//

    public ZaakpayApiRequestParameters processPayment(String orderId, String amount) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        //You can pass these values dynamically through function arguments
        //String orderId = "ZPLive"+System.currentTimeMillis();
       // String amount = "100" ; //In Paisa
        
//        System.out.println(amount + "   --   " + orderId);
        RequestParameters order = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,Config.RETURN_URL,orderId,amount);
        zaakpayApiRequestParameters.setRequestParameters(order.getPaymentRequestParameters());

        String checksumString = ChecksumGenerator.getChecksumString(order.getPaymentRequestParameters());
        System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        //System.out.println(checksum);
        zaakpayApiRequestParameters.setChecksum(checksum);
        
//        System.out.println(checksumString + "checksum=" + zaakpayApiRequestParameters.getChecksum());

        return zaakpayApiRequestParameters ;

    }

    //***********************************************************************************************************************//

    //************************************************* INITIATE ANOTHER PAYMENT ***************************************************//

    public ZaakpayApiRequestParameters processPaymentRecharge(String orderId, String amount) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        //You can pass these values dynamically through function arguments
        //String orderId = "ZPLive"+System.currentTimeMillis();
       // String amount = "100" ; //In Paisa
        
//        System.out.println(amount + "   --   " + orderId);
        RequestParameters order = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,Config.RETURN_URL_SECOND,orderId,amount);
        zaakpayApiRequestParameters.setRequestParameters(order.getPaymentRequestParameters());

        String checksumString = ChecksumGenerator.getChecksumString(order.getPaymentRequestParameters());
        System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        //System.out.println(checksum);
        zaakpayApiRequestParameters.setChecksum(checksum);
        
//        System.out.println(checksumString + "checksum=" + zaakpayApiRequestParameters.getChecksum());

        return zaakpayApiRequestParameters ;

    }

    //***********************************************************************************************************************//

    //************************************************* INITIATE RETURN PAYMENT ***************************************************//

    public ZaakpayApiRequestParameters processPaymentReturn(String orderId, String amount) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        //You can pass these values dynamically through function arguments
        //String orderId = "ZPLive"+System.currentTimeMillis();
       // String amount = "100" ; //In Paisa
        
//        System.out.println(amount + "   --   " + orderId);
        RequestParameters order = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,Config.RETURN_URL_RETURN,orderId,amount);
        zaakpayApiRequestParameters.setRequestParameters(order.getPaymentRequestParameters());

        String checksumString = ChecksumGenerator.getChecksumString(order.getPaymentRequestParameters());
        System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        //System.out.println(checksum);
        zaakpayApiRequestParameters.setChecksum(checksum);
        
//        System.out.println(checksumString + "checksum=" + zaakpayApiRequestParameters.getChecksum());

        return zaakpayApiRequestParameters ;

    }

    //***********************************************************************************************************************//
    
    //************************************************* INITIATE INTERNATIONAL ONE-WAY PAYMENT ***************************************************//

    public ZaakpayApiRequestParameters processPaymentInternational(String orderId, String amount) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        //You can pass these values dynamically through function arguments
        //String orderId = "ZPLive"+System.currentTimeMillis();
       // String amount = "100" ; //In Paisa
        
//        System.out.println(amount + "   --   " + orderId);
        RequestParameters order = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,Config.RETURN_URL_INTERNATIONAL,orderId,amount);
        zaakpayApiRequestParameters.setRequestParameters(order.getPaymentRequestParameters());

        String checksumString = ChecksumGenerator.getChecksumString(order.getPaymentRequestParameters());
        System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        //System.out.println(checksum);
        zaakpayApiRequestParameters.setChecksum(checksum);
        
//        System.out.println(checksumString + "checksum=" + zaakpayApiRequestParameters.getChecksum());

        return zaakpayApiRequestParameters ;

    }

    //***********************************************************************************************************************//

    //************************************************* INITIATE INTERNATIONAL RETURN PAYMENT ***************************************************//

    public ZaakpayApiRequestParameters processPaymentInternationalReturn(String orderId, String amount) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        //You can pass these values dynamically through function arguments
        //String orderId = "ZPLive"+System.currentTimeMillis();
       // String amount = "100" ; //In Paisa
        
//        System.out.println(amount + "   --   " + orderId);
        RequestParameters order = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,Config.RETURN_URL_INTERNATIONAL_RETURN,orderId,amount);
        zaakpayApiRequestParameters.setRequestParameters(order.getPaymentRequestParameters());

        String checksumString = ChecksumGenerator.getChecksumString(order.getPaymentRequestParameters());
        System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        //System.out.println(checksum);
        zaakpayApiRequestParameters.setChecksum(checksum);
        
//        System.out.println(checksumString + "checksum=" + zaakpayApiRequestParameters.getChecksum());

        return zaakpayApiRequestParameters ;

    }

    //***********************************************************************************************************************//
    
    //****************************************** HANDLE ZAAKPAY CALLBACK RESPONSE *******************************************//

    public String[] getResponseParameters ()
    {
        String[] responseParam = {"amount","bank","bankid","cardId",
                "cardScheme","cardToken","cardhashid","doRedirect","orderId",
                "paymentMethod","paymentMode","responseCode","responseDescription",
                "productDescription","product1Description","product2Description",
                "product3Description","product4Description","pgTransId","pgTransTime"} ;

        //These are the response parameters you will get in the final callback from Zaakpay.

        return responseParam;
    }

    //***********************************************************************************************************************//



    //************************************************ CHECK PAYMENT STATUS *************************************************//

    public ZaakpayApiRequestParameters checkStatus( String orderId) throws Exception {

        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();

        String requestUrl = Config.ENVIRONMENT+Config.TRANSACTION_STATUS_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        RequestParameters checkStatus = new RequestParameters(orderId,Config.ZAAKPAY_MERCHANT_IDENTIFIER);
        zaakpayApiRequestParameters.setRequestParameters(checkStatus.getTransactionStatusRequestParameters());

        String checksumString = checkStatus.getTransactionStatusRequestParameters().get("formRequestData");
        // System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        zaakpayApiRequestParameters.setChecksum(checksum);


        return zaakpayApiRequestParameters;
    }

    //***********************************************************************************************************************//



    //************************************************* INITIATE REFUND ***************************************************//

    public ZaakpayApiRequestParameters initiateFullRefund(String orderId) throws Exception {
        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();
        String requestUrl = Config.ENVIRONMENT+Config.UPDATE_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        String updateReason = "Test Reason";
        RequestParameters refund = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,null,orderId,"14",updateReason);
        zaakpayApiRequestParameters.setRequestParameters(refund.getFullRefundRequestParameters());

        String checksumString = ChecksumGenerator.getUpdateChecksumString(refund.getFullRefundRequestParameters());
        // System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        zaakpayApiRequestParameters.setChecksum(checksum);

        return zaakpayApiRequestParameters;
    }


    public ZaakpayApiRequestParameters initiatePartialRefund(String orderId , String amount) throws Exception {
        ZaakpayApiRequestParameters zaakpayApiRequestParameters = new ZaakpayApiRequestParameters();
        String requestUrl = Config.ENVIRONMENT+Config.UPDATE_API_URL;
        zaakpayApiRequestParameters.setRequestUrl(requestUrl);

        String updateReason = "Test Reason";
        RequestParameters refund = new RequestParameters(Config.ZAAKPAY_MERCHANT_IDENTIFIER,amount,orderId,"22",updateReason);
        zaakpayApiRequestParameters.setRequestParameters(refund.getPartialRefundRequestParameters());

        String checksumString = ChecksumGenerator.getUpdateChecksumString(refund.getPartialRefundRequestParameters());
        // System.out.println(checksumString);
        // You can check the checksum string generated here.
        String checksum = ChecksumGenerator.calculateChecksum(Config.ZAAKPAY_SECRET_KEY,checksumString);
        zaakpayApiRequestParameters.setChecksum(checksum);

        return zaakpayApiRequestParameters;
    }

    //***********************************************************************************************************************//

}
