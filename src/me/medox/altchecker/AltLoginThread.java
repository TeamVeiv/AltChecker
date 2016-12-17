package me.medox.altchecker;

import ch.darknight.Check;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.sun.deploy.net.proxy.ProxyType;
import javafx.application.Platform;
import javafx.scene.control.Label;
import net.minecraft.util.Session;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;

/**
 * Created by v3v on 16.12.16.
 */
public class AltLoginThread implements Runnable {

    private ArrayList<String> allAlts = new ArrayList<>();
    private ArrayList<String> allProxyIPs = new ArrayList<>();
    private ArrayList<Integer> allProxyPorts = new ArrayList<>();
    private int currentAlt;
    private boolean useProxy;
    private int currentProxy;
    private String serverIP;
    private JFXTextArea altField;
    private static int workingAlts;
    private static int mojangBannedAlts;
    private static int bannedAlts;
    private Label workingAltsTextField;
    private Label mojangBannedAltsTextField;
    private Label bannedAltsTextField;

    public AltLoginThread(String serverIP, Label workingAltsTextField, Label mojangBannedAltsTextField,
                          Label bannedAltsTextField, boolean useProxy,
                          ArrayList<String> allAlts, ArrayList<String> allProxyIPs, ArrayList<Integer> allProxyPorts,
                          JFXTextArea altField, int currentAlt, int currentProxy) {
        this.useProxy = useProxy;
        this.allAlts = allAlts;
        this.allProxyIPs = allProxyIPs;
        this.allProxyPorts = allProxyPorts;
        this.altField = altField;
        this.currentAlt = currentAlt;
        this.currentProxy = currentProxy;
        this.workingAltsTextField = workingAltsTextField;
        this.mojangBannedAltsTextField = mojangBannedAltsTextField;
        this.bannedAltsTextField = bannedAltsTextField;
        this.serverIP = serverIP;
    }

    @Override
    public void run() {
        String[] alt = allAlts.get(currentAlt).split(":");
        Proxy proxy = Proxy.NO_PROXY;
        InetSocketAddress address = new InetSocketAddress(allProxyIPs.get(currentProxy), allProxyPorts.get(currentProxy));
        if (useProxy) {
            proxy = new Proxy(Proxy.Type.SOCKS, address);
        }
        System.out.println("Checking " + alt[0] + " ,with Proxy " + address.getAddress() + ":" + address.getPort());
        Session session = Check.login(alt[0], alt[1], proxy);
        if (session != null) {
            try {
                String ip = serverIP.split(":")[0];
                int port = serverIP.contains(":") ? Integer.valueOf(serverIP.split(":")[1]) : 25565;
                if (Check.connect(ip, port, session, proxy)) {
                    addAlt(alt[0] + ":" + alt[1]);
                    workingAlts++;
                } else {
                    bannedAlts++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                bannedAlts++;
            }
        } else {
            mojangBannedAlts++;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                workingAltsTextField.setText(String.valueOf(workingAlts));
                mojangBannedAltsTextField.setText(String.valueOf(mojangBannedAlts));
                bannedAltsTextField.setText(String.valueOf(bannedAlts));
            }
        });
    }

    public void setAllAlts(ArrayList<String> allAlts) {
        this.allAlts = allAlts;
    }

    public void setCurrentAlt(int currentAlt) {
        this.currentAlt = currentAlt;
    }

    public void setCurrentProxy(int currentProxy) {
        this.currentProxy = currentProxy;
    }

    public void setJFXTextArea(JFXTextArea area) {
        this.altField = area;
    }

    private void addAlt(String text) {
        altField.setText(altField.getText() + "\n" + text);
    }

}
