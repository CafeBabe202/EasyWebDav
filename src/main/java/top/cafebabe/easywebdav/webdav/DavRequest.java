package top.cafebabe.easywebdav.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import top.cafebabe.easywebdav.utils.StringUtils;
import top.cafebabe.easywebdav.webdav.io.DavInputStream;

/**
 * 主要的请求执行的类，所有的请求逻辑都是本类执行的，对于同一个 WebDav 对象衍生出来的 DavFile 对象将共享一个 DavRequest
 * 对象。除上传和下载之外的所有请求操作将在同一个 Socket 内完成（如果是 KeepAlive 为 true）,上传和下载的流将在新的 socket
 * 内完成，不影响正常的请求操作。 本类不是线程安全的。
 */
public class DavRequest {
    private String host;
    private int port;
    private boolean ssl;
    private boolean keepAlive;
    private DavHeader globalHeader;
    private Socket globalSocket;
    private InputStream globalInputStream;
    private OutputStream globalOutputStream;

    public DavRequest(String host, int port, boolean ssl, boolean keepAlive) throws IOException {
        this.host = host;
        this.port = port;
        this.ssl = ssl;
        this.keepAlive = keepAlive;
        this.initSocket();
        this.globalHeader = new DavHeader().add("Host", host + ":" + port)
                .add("User-Agent", "EasyWebDav/1.0 openJDK-11").add("Connection", keepAlive ? "keep_alive" : "colse");
    }

    public DavRequest(String host, int port) throws IOException {
        this(host, port, port == 443, false);
    }

    public void auth(String username, String password) {
        Encoder base64 = Base64.getEncoder();
        String auth = base64.encodeToString((username + ":" + password).getBytes());
        this.globalHeader.add("Authorization", "Basic " + auth);
    }

    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    public void keepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        this.globalHeader.add("Connection", keepAlive ? "keep_alive" : "colse");
    }

    public void close() {
        if (this.globalInputStream != null)
            try {
                this.globalInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (this.globalOutputStream != null)
            try {
                this.globalOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        if (this.globalSocket != null)
            try {
                this.globalSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public DavResponse put(String path, InputStream is, long contentLength) throws IOException {
        path = StringUtils.urlEncode(path);
        DavHeader header = this.globalHeader.clone().add("Content-Length", String.valueOf(contentLength));
        String line = "PUT " + path + " HTTP/1.1";
        String out = line + "\r\n" + header.toString() + "\r\n\r\n";
        try (Socket socket = this.createSocket(this.host, this.port, this.ssl)) {
            OutputStream sos = socket.getOutputStream();
            InputStream sis = socket.getInputStream();
            sos.write(out.getBytes());
            long sumOut = 0;
            int len = 0;
            byte[] buffer = new byte[1024 * 1024];
            while ((len = is.read(buffer)) != -1) {
                sos.write(buffer, 0, len);
                sumOut += len;
                if (sumOut == contentLength)
                    break;
            }
            DavResponse response = new DavResponse();
            response.setStatusLine(this.readLine(sis));
            if (response.getCode() == 400)
                return response;
            if (response.getCode() / 100 == 2) {
                response.setHeader(this.readHeader(sis));
                this.readBody(sis, response);
            }
            return response;
        }
    }

    public DavResponse mkcolOrNull(String path) {
        path = StringUtils.urlEncode(path);
        String line = "MKCOL " + path + " HTTP/1.1";
        DavHeader header = this.globalHeader.clone().add("Content-Length", "0");
        String out = line + "\r\n" + header.toString() + "\r\n\r\n";
        try {
            DavResponse response = this.request(out);
            if (response.getCode() / 100 == 3) {
                path = response.getHead().get("Location");
                return mkcolOrNull(path);
            }
            return response;
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            this.closeSocket();
        }
        return null;
    }

    public DavResponse deleteOrNull(String path) {
        path = StringUtils.urlEncode(path);
        String line = "DELETE " + path + " HTTP/1.1";
        DavHeader header = this.globalHeader.clone().add("Content-Length", "0");
        String out = line + "\r\n" + header.toString() + "\r\n\r\n";
        try {
            this.initSocket();
            this.globalOutputStream.write(out.getBytes());
            DavResponse response = new DavResponse();
            response.setStatusLine(this.readLine(this.globalInputStream));
            if (response.getCode() == 400) {
                return response;
            }
            response.setHeader(this.readHeader(this.globalInputStream));
            readBody(this.globalInputStream, response);
            if (response.getCode() / 100 == 3) {
                path = response.getHead().get("Location");
                return deleteOrNull(path);
            } else {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeSocket();
        }
        return null;
    }

    public DavResponse move(String path, String newPath) {
        path = StringUtils.urlEncode(path);
        String line = "MOVE " + path + " HTTP/1.1";
        DavHeader header = this.globalHeader.clone().add("Content-Length", "0")
                .add("Destination", (this.ssl ? "https://" : "http://") + this.host + ":" + this.port + newPath)
                .add("Overwrite", "F");
        String out = line + "\r\n" + header.toString() + "\r\n\r\n";
        try {
            this.initSocket();
            this.globalOutputStream.write(out.getBytes());
            DavResponse response = new DavResponse();
            response.setStatusLine(this.readLine(this.globalInputStream));
            if (response.getCode() == 400)
                return response;
            response.setHeader(this.readHeader(this.globalInputStream));
            this.readBody(this.globalInputStream, response);
            if (response.getCode() / 100 == 3) {
                path = response.getHead().get("Loaction");
                return move(path, newPath);
            } else {
                return response;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeSocket();
        }
        return null;
    }

    public DavResponse propfindOrNull(String path) {
        path = StringUtils.urlEncode(path);
        try {
            this.initSocket();
            String line = "PROPFIND " + path + " HTTP/1.1";
            DavHeader header = this.globalHeader.clone().add("depth", "1");
            this.write(this.globalOutputStream, line, header);
            DavResponse response = new DavResponse();
            response.setStatusLine(this.readLine(this.globalInputStream));
            if (response.getCode() == 400)
                return response;
            response.setHeader(this.readHeader(this.globalInputStream));
            this.readBody(this.globalInputStream, response);
            if (response.getCode() / 100 == 3) {
                path = StringUtils.getPathQuery(response.getHead().get("Location"));
                return this.propfindOrNull(path);
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.closeSocket();
        }
        return null;
    }

    public DavInputStream getOrNull(String path) throws IOException {
        path = StringUtils.urlEncode(path);
        Socket socket = this.createSocket(this.host, this.port, this.ssl);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        String line = "GET " + path + " HTTP/1.1";
        DavHeader header = this.globalHeader.clone().add("Content-Length", "0");
        this.write(os, line, header);
        DavResponse response = new DavResponse();
        response.setStatusLine(this.readLine(is));
        if (response.getCode() == 400)
            return null;
        response.setHeader(this.readHeader(is));
        if (response.getCode() / 100 == 3) {
            path = StringUtils.getPathQuery(response.getHead().get("Location"));
            return this.getOrNull(path);
        }
        System.out.println(response);
        return null;
    }

    private DavResponse request(String out) throws IOException {
        this.initSocket();
        this.globalOutputStream.write(out.getBytes());
        DavResponse response = new DavResponse();
        response.setStatusLine(this.readLine(this.globalInputStream));
        if (response.getCode() == 400)
            return response;
        response.setHeader(this.readHeader(this.globalInputStream));
        this.readBody(this.globalInputStream, response);
        return response;
    }

    private void write(OutputStream os, String requestLine, DavHeader header) throws IOException {
        String out = requestLine + "\r\n" + header.toString() + "\r\n\r\n";
        os.write(out.getBytes());
    }

    private String readLine(InputStream is) throws IOException {
        List<Byte> str = new LinkedList<>();
        int codePoint = 0x00, last = 0x00;
        while ((codePoint = is.read()) != -1) {
            if (codePoint == 0x0A && last == 0x0D)
                break;
            if (codePoint != 0x0A && codePoint != 0x0D)
                str.add(Byte.valueOf((byte) codePoint));
            last = codePoint;
        }
        byte[] temp = new byte[str.size()];
        int i = 0;
        for (byte b : str)
            temp[i++] = b;
        return new String(temp);
    }

    private String readLines(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();
        String len, content;
        while (true) {
            len = this.readLine(is);
            content = this.readLine(is);
            if ("0".equals(len) && "".equals(content))
                break;
            builder.append(content);
        }
        return builder.toString();
    }

    private byte[] readBytes(InputStream is, int contentLength) throws IOException {
        byte[] res = new byte[contentLength];
        int resP = 0;
        int newByte = 0;
        while ((newByte = is.read()) != -1) {
            res[resP++] = (byte) newByte;
            if (resP == contentLength)
                break;
        }
        return res;
    }

    private DavHeader readHeader(InputStream is) throws IOException {
        DavHeader head = new DavHeader();
        String line;
        while (!"".equals(line = this.readLine(is))) {
            int position = line.indexOf(":");
            head.add(line.substring(0, position), line.substring(position + 1, line.length()));
        }
        return head;
    }

    private void readBody(InputStream is, DavResponse response) throws IOException {
        String len = response.getHead().get("Content-Length");
        if (len != null) {
            response.setBody(this.readBytes(is, Integer.valueOf(len)));
        } else if ("chunked".equals(response.getHead().get("Transfer-Encoding"))) {
            response.setBody(this.readLines(is).getBytes());
        }
    }

    private Socket createSocket(String host, int port, boolean ssl) throws IOException {
        return ssl ? SSLSocketFactory.getDefault().createSocket(host, port) : new Socket(host, port);
    }

    private void initSocket() throws IOException {
        if (this.globalSocket == null || this.globalSocket.isClosed()) {
            this.globalSocket = this.createSocket(this.host, this.port, this.ssl);
            this.globalInputStream = this.globalSocket.getInputStream();
            this.globalOutputStream = this.globalSocket.getOutputStream();
        }
    }

    private void closeSocket() {
        if (!this.keepAlive) {
            this.close();
        }
    }

}
