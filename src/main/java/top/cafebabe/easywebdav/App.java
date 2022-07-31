package top.cafebabe.easywebdav;

import java.util.List;

import top.cafebabe.easywebdav.webdav.Account;
import top.cafebabe.easywebdav.webdav.DavFile;
import top.cafebabe.easywebdav.webdav.WebDav;

public class App {
    public static void main(String[] args) throws Exception {
        Account account = new Account("admin", "passwd", "/dav");
        WebDav webDav = new WebDav(account, "127.0.0.1", 5244, false);
        DavFile loacl = webDav.getDictitionary("/local/Telegram Desktop/");
        System.out.println(loacl.refresh());
        List<DavFile> list = loacl.getSubFile();
        if (list != null)
            for (DavFile file : list) {
                System.out.println(file);
            }
        else
            System.out.println("NULL");
    }
}
