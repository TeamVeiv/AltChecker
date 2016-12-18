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

    public void check(ActionEvent event) {
        try {
            ArrayList<String> list = getAllProxys(new File(inFile.getText()));

            checking = true;
            if (!inFile.getText().equals("") && !outFile.getText().equals("")) {
                int threads = (int) threadSlider.getValue();
                for(int i = 1; i <= threads; i++){
                    if(i > list.size()){
                        return;
                    }
                    System.out.println("Checking(Thread-" + i + "): " + list.get(i));
                    new Thread(new ProxyChecker(new File(inFile.getText()), progressBar, 80 , outFile, i, list.get(i))).start();
                }
            }else{
                Util.showPopUp("Select a Proxy List and an Output Directory");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ArrayList<String> getAllProxys(File file){
        ArrayList<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = reader.readLine()) != null){
                if(line.contains(":") && line.contains(".")){
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

    public void addInFile(ActionEvent event){
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(new Panel(), "Select Proxy List");
        if(chooser.getSelectedFile() != null){
            inFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void addOutFile(ActionEvent event){
        JFileChooser chooser = new JFileChooser();
        chooser.showDialog(new Panel(), "Select Output");
        if(chooser.getSelectedFile() != null){
            outFile.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

}
