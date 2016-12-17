package me.medox.altchecker;

import ch.darknight.Check;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.medox.altchecker.me.medox.altchecker.assets.Util;
import net.minecraft.util.Session;
import org.controlsfx.control.textfield.TextFields;

import javax.swing.*;
import java.applet.Applet;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by v3v on 16.12.16.
 */
public class Main extends Application implements Initializable {

    @FXML
    private JFXTextField altListTextField;
    @FXML
    private JFXTextField proxyListTextField;
    @FXML
    private JFXTextField serverTextField;
    @FXML
    public JFXCheckBox useProxyList;
    @FXML
    public JFXTextArea altArea;
    @FXML
    public Label workingAltsTextField;
    @FXML
    public Label mojangBannedAltsTextField;
    @FXML
    public Label bannedAltsTextField;
    @FXML
    public JFXTextField threadsTextField;

    private static int currentAlt;
    private static int currentProxy;
    private static ArrayList<String> allAlts = new ArrayList<>();
    private static ArrayList<String> allProxyIPs = new ArrayList<>();
    private static ArrayList<Integer> allProxyPorts = new ArrayList<>();

    public static void main(String[] args) {
        launch();
    }


    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Gui.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("AltChecker");
        stage.setResizable(false);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        stage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream inputStream = getClass().getResourceAsStream("serverIPs.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        ArrayList<String> allIPs = new ArrayList<>();
        try {
            while ((line = reader.readLine()) != null) {
                allIPs.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextFields.bindAutoCompletion(serverTextField, allIPs);
    }

    public void openAltFileChooser(ActionEvent event) {
        JFileChooser chooser = new JFileChooser("Select Alt-List");
        chooser.showOpenDialog(new JFrame());
        if (chooser.getSelectedFile() != null) {
            altListTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void openProxyFileChooser(ActionEvent event) {
        JFileChooser chooser = new JFileChooser("Select Alt-List");
        chooser.showOpenDialog(new JFrame());
        if (chooser.getSelectedFile() != null) {
            proxyListTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void check(ActionEvent event) {
        if (canCheck()) {
            workingAltsTextField.setText("0");
            mojangBannedAltsTextField.setText("0");
            bannedAltsTextField.setText("0");
            allAlts.clear();
            addAllAlts();
            if (useProxyList.isSelected()) {
                addAllProxys();
            }
            currentAlt = 0;
            for (int i = 0; i < Integer.valueOf(threadsTextField.getText()); i++) {
                new Thread(new AltLoginThread(serverTextField.getText(), workingAltsTextField, mojangBannedAltsTextField, bannedAltsTextField, useProxyList.isSelected(), allAlts, allProxyIPs, allProxyPorts, altArea, currentAlt, currentProxy)).start();
                if (currentAlt <= allAlts.size()) {
                    currentAlt++;
                }
                if (currentProxy <= allProxyIPs.size()) {
                    currentProxy++;
                } else {
                    currentProxy = 0;
                }
                if (i == allAlts.size()) {
                    System.out.println("Finsished!");
                }
            }
        }
    }

    // boolean useProxy, ArrayList<String> allAlts, JFXTextField altField,int currentAlt, int currentProxy, int plusAlt, int plusProxy

    public void addAllAlts() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(altListTextField.getText()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(":")) {
                    allAlts.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAllProxys() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(proxyListTextField.getText()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(".") && line.contains(":")) {
                    allProxyIPs.add(line.split(":")[0]);
                    allProxyPorts.add(Integer.valueOf(line.split(":")[1]));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean canCheck() {
        if (altListTextField.getText().equals("")) {
            Util.showPopUp("Please add an Alt-List");
            return false;
        }
        if (useProxyList.isSelected() && proxyListTextField.getText().equals("")) {
            Util.showPopUp("Add a Proxy-List or uncheck 'Use Proxy List'");
            return false;
        }
        return true;
    }

}
