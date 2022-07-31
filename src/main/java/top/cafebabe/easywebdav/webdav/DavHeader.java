package top.cafebabe.easywebdav.webdav;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class DavHeader {
    private Map<String, String> map;

    public DavHeader() {
        this.map = new HashMap<String, String>();
    }

    public DavHeader add(String feild, String value) {
        feild = feild.trim();
        value = value.trim();
        this.map.put(feild, value);
        return this;
    }

    public String get(String feild) {
        return this.map.get(feild);
    }

    public DavHeader remove(String feild) {
        this.map.remove(feild);
        return this;
    }

    @Override
    public DavHeader clone() {
        DavHeader head = new DavHeader();
        for (String key : this.map.keySet()) {
            head.add(key, this.map.get(key));
        }
        return head;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\r\n");
        for (String key : this.map.keySet()) {
            sj.add(key + ": " + this.map.get(key));
        }
        return sj.toString();
    }
}
