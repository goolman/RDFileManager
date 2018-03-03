package com.xdockalr.rdfilemanager;

import android.webkit.MimeTypeMap;

import java.util.Locale;

public class MimeType {

    public static String getTypeFromName(String name) {
        String type = null;
        int dotPosition = name.lastIndexOf('.');
        if (dotPosition >= 0) {
            String extension = name.substring(dotPosition + 1).toLowerCase();

            if (!extension.isEmpty()) {
                final String extensionLowerCase = extension.toLowerCase(Locale.getDefault());
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extensionLowerCase);
            }
        }
        return type;
    }
}
