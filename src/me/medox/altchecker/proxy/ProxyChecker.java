package me.medox.altchecker.proxy;

import com.jfoenix.controls.JFXSlider;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by v3v on 17.12.16.
 */
public class ProxyChecker {

    private File file;
    private File savePath;
    private ProgressIndicator progressIndicator;
    private JFXSlider slider;

    private static ExecutorService threadPool;

    public ProxyChecker(File file, File savePath, ProgressIndicator progressIndicator, JFXSlider slider) {
        this.file = file;
        this.progressIndicator = progressIndicator;
        this.slider = slider;
    }

    public ArrayList<String> getWorkingProxies() throws FileNotFoundException {
        double all = getAllProxys(file).size();
        System.out.println("All Proxies: " + all);
        threadPool = Executors.newFixedThreadPool((int)slider.getValue());
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String l = line;
                threadPool.execute(() -> {
                    if (l.contains(":") && l.contains(".")) {
                        if (checkProxy(l, 2000)) {
                            System.out.println("Found working Proxy : " + l);
                            list.add(l);
                            Platform.runLater(() -> {
                                progressIndicator.setProgress(list.size() / all);
                            });
                        }
                    }
                });
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e3) {
            e3.printStackTrace();
        }

        return list;
    }

    private ArrayList<String> getAllProxys(File file) {
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":") && line.contains(".")) {
                    list.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static boolean checkProxy(final String proxy, final int timeout) {
        if (!proxy.contains(":")) {
            return false;
        }
        final String[] split = proxy.split(":");
        try {
            Proxy test = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
            Socket sock = new Socket(test);
            sock.connect(new InetSocketAddress("alpha-centauri.tk", 80), timeout);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
