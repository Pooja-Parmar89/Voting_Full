package com.votingapp.voting.util;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestUtil {

    private RequestUtil() {}

    public static String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }

    /** Very small heuristic parser - good enough for audit/login logs without an extra dependency. */
    public static String[] parseBrowserAndOs(String userAgent) {
        if (userAgent == null) {
            return new String[]{"Unknown", "Unknown"};
        }
        String ua = userAgent.toLowerCase();
        String browser;
        if (ua.contains("edg/")) browser = "Edge";
        else if (ua.contains("chrome/") && !ua.contains("chromium")) browser = "Chrome";
        else if (ua.contains("firefox/")) browser = "Firefox";
        else if (ua.contains("safari/") && !ua.contains("chrome")) browser = "Safari";
        else if (ua.contains("opr/") || ua.contains("opera")) browser = "Opera";
        else browser = "Other";

        String os;
        if (ua.contains("windows")) os = "Windows";
        else if (ua.contains("mac os") || ua.contains("macintosh")) os = "macOS";
        else if (ua.contains("android")) os = "Android";
        else if (ua.contains("iphone") || ua.contains("ipad")) os = "iOS";
        else if (ua.contains("linux")) os = "Linux";
        else os = "Other";

        return new String[]{browser, os};
    }
}
