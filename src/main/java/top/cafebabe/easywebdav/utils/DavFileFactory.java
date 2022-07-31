package top.cafebabe.easywebdav.utils;

import top.cafebabe.easywebdav.webdav.DavFile;
import top.cafebabe.easywebdav.webdav.DavRequest;

public class DavFileFactory {
    private DavRequest request;
    private String path;
    private String name;
    private long size;
    private String lastModified;
    private boolean exist;

    public DavFileFactory(){
        this.size = -1;
    }

    public void setRequest(DavRequest request) {
        this.request = request;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public DavFile create() {
        return new DavFile(this.request, this.path, this.name, this.size, this.lastModified, this.exist);
    }

    public DavRequest getRequest() {
        return request;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public boolean isExist() {
        return exist;
    }
}
