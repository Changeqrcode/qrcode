package com.change.qrcode.util;

import javax.servlet.http.HttpServletRequest;

public class Utilities {
    public static String getSiteUrl(HttpServletRequest httpServletRequest){
        String siteUrl = httpServletRequest.getRequestURL().toString();
        return siteUrl.replace(httpServletRequest.getServletPath(),"");
    }
}
