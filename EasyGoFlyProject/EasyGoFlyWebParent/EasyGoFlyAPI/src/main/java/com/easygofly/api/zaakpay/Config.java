package com.easygofly.api.zaakpay;


public class Config {

//    public static String ZAAKPAY_SECRET_KEY = "5eca8d6445324ba4b37c345bb7c94d82" ;

    public static String ENVIRONMENT = "https://api.zaakpay.com" ;

//    public static String ZAAKPAY_MERCHANT_IDENTIFIER = "74ea6846304c49559e823f018bbbc46a" ;

//    public static String TRANSACTION_API_URL = "/api/paymentTransact/V8" ;

    public static String UPDATE_API_URL = "/updatetransaction" ;

    public static String TRANSACTION_STATUS_API_URL = "/checkTxn?v=5" ;

    public static String RETURN_URL = "https://easegofly.com/zaakpay/response" ;

    public static String RETURN_URL_SECOND = "https://easegofly.com/zaakpay/recharge" ;

    public static String RETURN_URL_RETURN = "https://easegofly.com/zaakpay/return/response" ;

    public static String RETURN_URL_INTERNATIONAL = "https://easegofly.com/zaakpay/international/response" ;

    public static String RETURN_URL_INTERNATIONAL_RETURN = "https://easegofly.com/zaakpay/international/return/response" ;

    public static String RETURN_URL_HOTEL = "https://easegofly.com/zaakpay/hotel/response" ;

    public static String RETURN_URL_BUS = "https://easegofly.com/zaakpay/bus/response" ;

}
