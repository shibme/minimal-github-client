package me.shib.lib.github.client.requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

final class ResourceUtil {

    static String readResource(String resourceFileName) {
        try {
            StringBuilder content = new StringBuilder();
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream(resourceFileName);
            assert is != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            br.close();
            return content.toString();
        } catch (IOException ignored) {
            return null;
        }
    }

}
