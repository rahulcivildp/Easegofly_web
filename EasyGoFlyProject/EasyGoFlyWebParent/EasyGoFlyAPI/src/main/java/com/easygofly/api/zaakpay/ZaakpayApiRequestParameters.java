package com.easygofly.api.zaakpay;

import java.util.HashMap;
import java.util.Map;

public class ZaakpayApiRequestParameters {

    private String requestUrl ;

    private String checksum ;

    private Map<String,String> requestParameters = new HashMap<String, String>();

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Map<String, String> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Map<String, String> requestParameters) {
        this.requestParameters = requestParameters;
    }
}
