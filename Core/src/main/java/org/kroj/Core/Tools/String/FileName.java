package org.kroj.Core.Tools.String;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class FileName {
    public static String getFileName(final URI uri, final String cd) {
        if (uri == null) return "";
        if (cd != null && !cd.isEmpty()) {
            int index = cd.toLowerCase().indexOf("filename=");
            if (index != -1) {
                String name = cd.substring(index+9).trim();
                if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 2) {
                    name = name.substring(1, name.length() - 1);
                }
                if (!name.isEmpty()) return URLDecoder.decode(name, StandardCharsets.UTF_8);
            }
        }
        String p = uri.getPath();
        if (p == null || p.isEmpty() || p.equals("/")) return "downloaded_file."+ UUID.randomUUID();
        int ls = p.lastIndexOf("/");
        return URLDecoder.decode(ls == -1 ? p : p.substring(ls + 1), StandardCharsets.UTF_8);
    }

}
