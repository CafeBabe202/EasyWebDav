package top.cafebabe.easywebdav.webdav.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * 实现了 InputStream 接口，本流可以统计已经读取的流的字节长度，方便下载进度的跟踪。
 */
public class DavInputStream extends InputStream {

    private Socket socket;
    private InputStream is;
    private long contentLength;
    private volatile long nowLength;

    public DavInputStream(InputStream is, long contentLength) {
        this(null, is, contentLength);
    }

    public DavInputStream(Socket socket, InputStream is, long contentLength) {
        this.socket = socket;
        this.is = is;
        this.contentLength = contentLength;
        this.nowLength = 0;
    }

    /**
     * 获得本流的总长度
     * 
     * @return 该流中可以包含的总字节数
     */
    public long getLength() {
        return this.contentLength;
    }

    /**
     * 获取目前已经读取的长度，如果想要显示进度，你可以在新的线程中通过轮训本方法来跟踪进度
     * 
     * @return 返回已经读取的长度
     */
    public long getNowLength() {
        return this.nowLength;
    }

    @Override
    public int read() throws IOException {
        if (this.nowLength == this.contentLength)
            return -1;
        this.nowLength++;
        return this.is.read();
    }

    @Override
    public void close() throws IOException {
        this.is.close();
        if (this.socket != null)
            this.socket.close();
    }

}
