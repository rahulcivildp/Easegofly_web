package com.easygofly.site.zaakpay;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.TreeMap;

public class ChecksumGenerator {

    private static String toHex(byte[] bytes) {
        StringBuilder buffer = new StringBuilder(bytes.length * 2);
        String str;
        for (Byte b : bytes) {
            str = Integer.toHexString(b);
            int len = str.length();
            if (len == 8) {
                buffer.append(str.substring(6));
            } else if (str.length() == 2) {
                buffer.append(str);
            } else {
                buffer.append("0" + str);
            }
        }
        return buffer.toString();
    }

    public Boolean verifyChecksum(String secretKey, String checksumString, String checksum) throws Exception {
        return calculateChecksum(secretKey, checksumString).equals(checksum);
    }

    public static String getChecksumString(TreeMap<String,String> requestParam) {
        String checksumString = "" ;
        for (Map.Entry<String,String> param: requestParam.entrySet()) {
            checksumString=checksumString+param.getKey()+"="+param.getValue();
            checksumString=checksumString+"&";
            //This will create the checksum string against every parameter.
        }
        return checksumString;
    }

    public static String calculateChecksum(String secretKey, String allParamValue) throws Exception {

        byte[] dataToEncryptByte = allParamValue.getBytes();
        byte[] keyBytes = secretKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] checksumByte = mac.doFinal(dataToEncryptByte);
        String checksum = toHex(checksumByte);
        return checksum;
    }


    public static String getUpdateChecksumString(Map<String,String> requestParam) {
        String checksumString = "" ;
        for (Map.Entry<String,String> param: requestParam.entrySet()) {
            checksumString=checksumString+"\'"+param.getValue()+"\'";
            //This will create the checksum string against every parameter.
        }
        return checksumString;
    }

}
