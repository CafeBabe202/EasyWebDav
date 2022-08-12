package top.cafebabe.easywebdav;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import top.cafebabe.easywebdav.webdav.Account;
import top.cafebabe.easywebdav.webdav.DavFile;
import top.cafebabe.easywebdav.webdav.WebDav;

public class App {
    public static void main(String[] args) throws Exception {
        Account account = new Account("admin", "passwd", "/dav");
        WebDav webDav = new WebDav(account, "192.168.2.250", 5244, false);
        upload(webDav);
        mkdirc(webDav);
        rename(webDav);
        delete(webDav);
    }

    public static void upload(WebDav webDav) throws Exception {
        DavFile local = webDav.getDictitionary("/local/");
        File file = new File("/home/zh/Downloads/alist-linux-amd64");
        InputStream is = new FileInputStream(file);
        boolean res = local.upload("alist", is, file.length());
        System.out.println("Upload:" + res);
    }

    public static void mkdirc(WebDav webDav) throws Exception {
        DavFile local = webDav.getDictitionary("/local/");
        boolean res = local.mkdir("新建测试");
        System.out.println("Mkdirc:" + res);
    }

    public static void rename(WebDav webDav) {
        DavFile local = webDav.getDictitionary("/local/");
        for (DavFile file : local.getSubFile()) {
            if ("alist".equals(file.getName())) {
                boolean res = file.rename("new_alist");
                System.out.println("Rename:" + res);
            }
        }
    }

    public static void delete(WebDav webDav) {
        DavFile local = webDav.getDictitionary("/local/");
        for (DavFile file : local.getSubFile()) {
            if ("新建测试".equals(file.getName())) {
                boolean res = file.delete();
                System.out.println("Delete:" + res);
            }
        }
    }
}
