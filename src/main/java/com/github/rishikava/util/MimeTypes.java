package com.github.rishikava.util;

import java.util.Map;

public class MimeTypes {
    public static final Map<String, String> TYPES = Map.of(
        ".html", "text/html",
        ".css", "text/css",
        ".png", "image/png",
        ".jpg", "image/jpeg",
        ".jpeg", "image/jpeg",
        ".js", "application/javascript",
        ".json", "application/json"
    );

    // Can return null
    public static String getMimeType(String extension) {
        return TYPES.get(extension.toLowerCase());
    }
}
