package com.hpe.adm.octane.services.util;

import com.hpe.adm.nga.sdk.exception.OctaneException;

public class SdkUtil {

    /**
     * Attempt to get the error message from and OctaneException, if it fails returns null instead
     * @param ex
     * @return
     */
    public static String getMessageFromOctaneException(OctaneException ex){
        String message = ex.getError().getDescription();

        if(message.contains("401")){
            message = "The username or the password is incorrect.";
        }else if(message.contains("404") || message.contains("500")){
            message = "The sharedspace or the workspace is incorrect.";
        }else {
            message = "General octane connection error.";
        }

        return message;
    }
}
