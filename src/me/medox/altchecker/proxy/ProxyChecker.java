package me.medox.altchecker.proxy;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.scene.PointLight;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import me.medox.altchecker.Main;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by v3v on 17.12.16.
 */
public class ProxyChecker implements Runnable {

    private File file;
    private ProgressIndicator progressIndicator;
    private int allProxys;
    private JFXTextField toSave;
    private int toCheck;
    private String proxy;

    public ProxyChecker(File file, ProgressIndicator progressIndicator, int allProxys, JFXTextField toSave, int toCheck, String proxy) {
        this.file = file;
        this.progressIndicator = progressIndicator;
        this.allProxys = allProxys;
        this.toSave = toSave;
        this.toCheck = toCheck;
        this.proxy = proxy;
    }

    public void getWorkingProxies() {
        ArrayList<String> list = new ArrayList<>();
        if (proxy.contains(":") && proxy.contains(".")) {
            if (checkProxy(proxy, 3000)) {
                System.out.println(proxy);
                list.add(proxy);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        progressIndicator.setProgress(list.size() / allProxys);
                        try {
                            PrintWriter writer = new PrintWriter(new File(toSave.getText()));
                            writer.println(proxy);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private static boolean checkProxy(final String proxy, final int timeout) {
        if (!proxy.contains(":")) {
            return false;
        }
        final String[] split = proxy.split(":");
        try {
            Proxy test = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
            Socket sock = new Socket(test);
            sock.connect(new InetSocketAddress("http://veiv.de/", 80), timeout);
            System.out.println("Working: " + proxy);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void run() {
        getWorkingProxies();
    }
}
