package top.cafebabe.easywebdav.webdav;

import java.io.IOException;
import java.net.UnknownHostException;
import top.cafebabe.easywebdav.utils.StringUtils;

/**
 * 该类表示一个账户对一个 webDav 服务的链接，可以通过该类的对象获取任意一个 webDav 目录所表示的 DavFile对象。
 * 并且你可以通过该类对 webDav 的链接进行一些配置，例如是否保持链接活跃或者是否启用 SSL 加密。
 * 注意：通过一个 webDav 对象获得的所有 DavFile 类的实体将公用同一个 socket 套接字，如果你想实现多线程的并发操作请为每一线程都创建个
 * webDav 对象来保证不会出现未知的错误。
 */
public class WebDav {
    private Account account;
    private DavRequest request;

    /**
     * 用于创建一个 webDav 的链接，该链接表示一个账户对一个服务器的链接。默认配置的链接是不会保持链接活跃的，你可以将 isKeepAlive 设置为
     * true 来是的每次进行请求完都不会关闭链接以方便下一次请求。
     * 
     * @param account 用于登录 webdav 的账户信息，包括用户命、密码和根目录所在的位置
     * @param url     服务器地址
     * @param port    服务所使用的端口号
     * @param isSSL   是否启用 ssl 加密
     * @throws UnknownHostException 如果不能解析服务器地址将抛出此异常
     * @throws IOException          如果过套接字连接时将抛出此异常
     */
    public WebDav(Account account, String url, int port, boolean isSSL) throws UnknownHostException, IOException {
        this.account = account;
        this.request = new DavRequest(url, port, isSSL, false);
        this.request.auth(account.getUsername(), this.account.getPassword());
    }

    /**
     * 如果将 isKeepAlive 设置为 true，每次请求完将不会关闭链接，如果你想要频繁的设置进行请求操作，最好将次设置为 true。
     * 
     * @param isKeepAlive 是否请求结束后保持链接
     * @return 返回自身，方便进行方法链的调用
     */
    public WebDav setIsKeepAlive(boolean isKeepAlive) {
        this.request.keepAlive(isKeepAlive);
        return this;
    }

    /**
     * 查看是否保持链接
     * 
     * @return 是否保持链接
     */
    public boolean isKeepAlive() {
        return this.request.isKeepAlive();
    }

    /**
     * 通过此方法你可获得任意一个位置表示的 DavFile 对象，即使该位置是不存在的。
     * 
     * @param path 文件的路径
     * @return 表示为路径的 DavFile 对象
     */
    public DavFile getDictitionary(String path) {
        DavFile davFile = new DavFile(
                this.request,
                StringUtils.pathConcat(this.account.getRoot(), path),
                "/",
                -1,
                "1900-01-01",
                true);
        return davFile;
    }

    /**
     * 获取该连接表示的根目录。
     * 
     * @return 根目录所代表的 DavFile 对象
     */
    public DavFile getRoot() {
        return this.getDictitionary("/");
    }
}