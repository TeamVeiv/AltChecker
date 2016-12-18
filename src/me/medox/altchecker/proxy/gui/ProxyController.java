package me.medox.altchecker.proxy.gui;

import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import me.medox.altchecker.other.Util;
import me.medox.altchecker.proxy.ProxyChecker;
import me.medox.altchecker.proxy.ProxyGui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by v3v on 17.12.16.
 */
public class ProxyController {

    public static ProxyController instance = new ProxyController();
    private static boolean checking = false;

    @FXML
    public JFXTextField inFile;
    @FXML
    public JFXTextField outFile;
    @FXML
    public JFXSlider threadSlider;
    @FXML
    public ProgressIndicator progressBar;

    public static Thread checkingThread;

    public void check(ActionEvent event) {
        Runnable runnable = () -> {
            if (checking) {
                Util.showPopUp("Already Checking!");
                return;
            }
            try {
                if (!inFile.getText().equals("") && !outFile.getText().equals("")) {
                    checking = true;
                    progressBar.setProgress(0);
                    ArrayList<String> proxies;
                    ProxyChecker proxyChecker = new ProxyChecker(new File(inFile.getText()), new File(outFile.getText()), progressBar, threadSlider);
                    proxies = proxyChecker.getWorkingProxies();
                    PrintWriter writer = new PrintWriter(outFile.getText());
                    for (int i = 0; i < proxies.size(); i++) {
                        writer.println(proxies.get(i));
                    }
                    writer.flush();
                    writer.close();
                    Util.showPopUp("Finished!");
                    checking = false;
                } else {
                    Util.showPopUp("Select a Proxy List and an Output Directory");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        checkingThread = new Thread(runnable);
        checkingThread.start();
    }


    public void addInFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(new Panel(), "Select Proxy List");
        if (chooser.getSelectedFile() != null) {
            inFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void addOutFile(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(new Panel(), "Select Output");
        if (chooser.getSelectedFile() != null) {
            outFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

}
