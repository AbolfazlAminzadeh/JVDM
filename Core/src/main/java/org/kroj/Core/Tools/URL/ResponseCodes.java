package org.kroj.Core.Tools.URL;

public class ResponseCodes {

    public static boolean isRedirection(int code) {
        return code == 301 || code == 302 || code == 303 || code == 307 || code == 308;
    }

}
