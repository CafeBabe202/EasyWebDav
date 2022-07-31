package top.cafebabe.easywebdav.webdav;

import lombok.Data;
import top.cafebabe.easywebdav.utils.StringUtils;

/**
 * Account 代表一个 WebDav 用户，需要注意的是，如果通过本类的一个实例对象创建了一个 WebDav
 * 实例，在更新本类中的用户名、密码、根目录信息后，将不能自动更新 WebDav 实例配置，你需要重新生成 WebDav 对象
 */
@Data
public class Account {
    private String username;
    private String password;
    private String root;

    public Account(String username, String password, String root) {
        this.username = username;
        this.password = password;
        this.root = root;
    }

    public Account(String username, String password) {
        this(username, password, "/");
    }

    public void setRoot(String root) {
        this.root = StringUtils.pathConcat(root, "/");
    }
}