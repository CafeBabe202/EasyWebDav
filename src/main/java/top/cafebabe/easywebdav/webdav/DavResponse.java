package top.cafebabe.easywebdav.webdav;

/**
 * 响应对象，DavRequest 请求的结果将被封装成本类返回。body 为 byte 类型，但是考虑到下载大文件时的内存占用问题，在下载文件时，将返回
 * DavInputStream 对象而不是将下载结果保存在 DavResponse 的 body 中。
 */
public class DavResponse {
    private int code;
    private String protocol;
    private String status;
    private DavHeader header;
    private byte[] body;

    public DavResponse() {
    }

    public DavResponse(String statusLine) {
        this.setStatusLine(statusLine);
    }

    public DavResponse setStatusLine(String statusLine) {
        String[] strs = statusLine.split(" ");
        this.protocol = strs[0];
        this.code = Integer.parseInt(strs[1]);
        this.status = strs[2];
        return this;
    }

    public int getCode() {
        return this.code;
    }

    public DavResponse setHeader(DavHeader header) {
        this.header = header;
        return this;
    }

    public DavHeader getHead() {
        return this.header;
    }

    public DavResponse setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public byte[] getBody() {
        return this.body;
    }

    public String toString() {
        return this.protocol + " " + this.code + " " + this.status + "\r\n" + this.header.toString() + "\r\n";
    }
}
