package top.cafebabe.easywebdav.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import top.cafebabe.easywebdav.utils.DavFileFactory;
import top.cafebabe.easywebdav.utils.StringUtils;
import top.cafebabe.easywebdav.utils.XMLParser;
import top.cafebabe.easywebdav.webdav.io.DavInputStream;

public class DavFile {
    private DavRequest request;
    private String path;
    private String name;
    private long size;
    private String lastModified;
    private boolean exist;
    private List<DavFile> subFile;

    public DavFile(DavRequest request, String path, String name, long size, String lastModified, boolean exist) {
        this.request = request;
        this.path = path;
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
        this.exist = exist;
    }

    public boolean upload(String name, InputStream is, long len) throws IOException {
        if (!this.exist || this.size > 0)
            return false;
        String path = StringUtils.urlEncode(StringUtils.pathConcat(this.path, name));
        DavResponse response = this.request.put(path, is, len);
        System.out.println(response);
        return response.getCode() / 100 == 2;
    }

    public boolean delete() {
        DavResponse response = this.request.deleteOrNull(this.path);
        return response != null && response.getCode() / 100 == 2;
    }

    public boolean rename(String newName) {
        StringJoiner sj = new StringJoiner("/", "/", "");
        String[] strs = this.path.split("/");
        for (int i = 0; i < strs.length; i++)
            sj.add(i == strs.length - 1 ? newName : strs[i]);
        String newPath = sj.toString();
        DavResponse response = this.request.move(this.path, newPath);
        boolean res = response != null && response.getCode() / 100 == 2;
        if (res) {
            this.name = newName;
            this.path = newPath;
        }
        return res;
    }

    public boolean move(String path) {
        return false;
    }

    public boolean exist() {
        return this.exist;
    }

    public List<DavFile> getSubFile() {
        if (this.subFile == null)
            this.refresh();
        return this.subFile;
    }

    public DavInputStream download() throws IOException{
        return this.request.getOrNull(this.path);
    }

    public boolean refresh() {
        DavResponse response;
        List<DavFileFactory> factories;
        if (this.size > 0 || (response = this.request.propfindOrNull(this.path)) == null || response.getCode() / 100 != 2
                || (factories = XMLParser.propFindResult(response.getBody())) == null)
            return false;

        if (response.getCode() == 404)
            this.exist = false;

        Iterator<DavFileFactory> iterator = factories.iterator();
        this.subFile = new ArrayList<>(factories.size());
        while (iterator.hasNext()) {
            DavFileFactory davFileFactory = iterator.next();
            if (!this.path.equals(StringUtils.pathConcat(davFileFactory.getPath(), "/"))) {
                davFileFactory.setRequest(this.request);
                this.subFile.add(davFileFactory.create());
            }
        }
        return true;
    }

    public boolean isFile() {
        return this.size > 0;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getLastModified() {
        return this.lastModified;
    }

    public long getSize() {
        return this.size;
    }

    @Override
    public String toString() {
        return "DavFile [lastModified=" + lastModified + ", name=" + name + ", path=" + path + ", size=" + size
                + ", subFile=" + subFile + "]";
    }
}
