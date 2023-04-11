package com.easygofly.site.zaakpay;

public class Config {

    public static String ZAAKPAY_SECRET_KEY = "0678056d96914a8583fb518caf42828a" ;

    public static String ENVIRONMENT = "https://zaakstaging.zaakpay.com" ;

    public static String ZAAKPAY_MERCHANT_IDENTIFIER = "b19e8f103bce406cbd3476431b6b7973" ;

    public static String TRANSACTION_API_URL = "/api/paymentTransact/V8" ;

    public static String UPDATE_API_URL = "/updatetransaction" ;

    public static String TRANSACTION_STATUS_API_URL = "/checkTxn?v=5" ;

    public static String RETURN_URL = "https://easegofly.com/zaakpay/response" ;

}
