package top.cafebabe.easywebdav.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class StringUtils {
    public static String pathConcat(String path1, String path2) {
        String res = "/" + path1 + "/" + path2 + "/";
        res = res.replaceAll("/+", "/");
        return res;
    }

    public static String urlDecode(String url) {
        try {
            return java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String urlEncode(String url) {
        String[] str = url.split("/+");
        StringJoiner sj = new StringJoiner("/", "", "");
        for (String s : str) {
            try {
                sj.add(java.net.URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replace("+", "%20"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sj.toString();
    }

    public static String getPathQuery(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getPath() + "?" + url.getQuery();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
