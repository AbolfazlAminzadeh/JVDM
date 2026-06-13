package Tools.URL;

import java.net.URI;
import java.net.URISyntaxException;

public class URL {

    public static URI getSafeURI(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            if (!url.matches("^\\w{4,5}://.*")) {
                url = "https://" + url;
            }

            return new URI(url);

        } catch (URISyntaxException e) {
            try {
                String encoded = url.replace(" ", "%20")
                        .replace("[", "%5B")
                        .replace("]", "%5D");
                return new URI(encoded);
            } catch (URISyntaxException ex) {
                return null;
            }
        }
    }
}
