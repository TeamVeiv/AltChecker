package me.medox.altchecker;

import ch.darknight.Check;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import me.medox.altchecker.other.Util;
import me.medox.altchecker.proxy.ProxyChecker;
import me.medox.altchecker.proxy.ProxyGui;
import net.minecraft.util.Session;
import org.controlsfx.control.textfield.TextFields;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public JFXTextArea altArea;
    @FXML
    public JFXTextArea bannedAltArea;
    @FXML
    public Label workingAltsTextField;
    @FXML
    public Label mojangBannedAltsTextField;
    @FXML
    public Label bannedAltsTextField;
    @FXML
    public JFXTextField threadsTextField;
    @FXML
    public JFXSlider slider;
    @FXML
    JFXToggleButton useProxy;

    private static int currentAlt;
    private static int currentProxy;
    private static ArrayList<String> allAlts = new ArrayList<>();
    private static ArrayList<String> allProxyIPs = new ArrayList<>();
    private static ArrayList<Integer> allProxyPorts = new ArrayList<>();
    private static boolean isChecking;

    private static ExecutorService threadPool;


    /**
     * Window
     */
    private static Group group;
    private static Stage stage;
    private static Scene scene;
    private static Parent root;


    /**
     * drag
     */
    private static double initX = 0;
    private static double initY = 0;

    public static void main(String[] args) {
        launch();
    }


    @Override
    public void start(Stage stage_null) throws Exception {
        stage = new Stage(StageStyle.UNDECORATED);
        root = FXMLLoader.load(getClass().getResource("assets/Gui.fxml"));
        group = new Group(root);
        scene = new Scene(group);
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
        setDraggable();
    }

    private void setDraggable() {

        group.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                initX = event.getScreenX() - stage.getX();
                initY = event.getScreenY() - stage.getY();
            }
        });
        group.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - initX);
                stage.setY(event.getScreenY() - initY);
            }
        });
        group.setVisible(true);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InputStream inputStream = getClass().getResourceAsStream("assets/serverIPs.txt");
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

    public void checkProxys(ActionEvent event) {
        ProxyGui.getInstance().openGui();
    }

    private static int working;
    private static int banned;
    private static int mojangBanned;

    public static Thread thread;

    public void check(ActionEvent event) {
       Runnable runnable = () ->{
           if (canCheck()) {
               isChecking = true;
               if(useProxy.isSelected()) {
                   addAllProxys();
               }
               workingAltsTextField.setText("0");
               mojangBannedAltsTextField.setText("0");
               bannedAltsTextField.setText("0");
               mojangBanned = 0;
               working = 0;
               banned = 0;
               threadPool = Executors.newFixedThreadPool((int) slider.getValue());
               String ip = serverTextField.getText().split(":")[0];
               int port = serverTextField.getText().contains(":") ?
                       Integer.valueOf(serverTextField.getText().split(":")[0]) : 25565;
               try (BufferedReader reader = new BufferedReader(new FileReader(altListTextField.getText()))) {
                   String line;
                   while ((line = reader.readLine()) != null) {
                       final String l = line;
                       threadPool.execute(() -> {
                           if (l.contains(":")) {
                               final String alt[] = l.split(":");
                               if(currentProxy >= allProxyIPs.size()){
                                   currentProxy = 0;
                               }
                               Proxy proxy = Proxy.NO_PROXY;
                               if(useProxy.isSelected()){
                                   proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(allProxyIPs.get(currentProxy), allProxyPorts.get(currentProxy)));
                               }
                               System.out.println("Checking " + l +" with Proxy " + proxy.address());
                               if(useProxy.isSelected()) {
                                   currentProxy++;
                               }
                               Session session = Check.login(alt[0], alt[1], proxy);
                               if (session != null) {
                                   try {
                                       if (Check.connect(ip, port, session, proxy)) {
                                           working++;
                                           altArea.setText(altArea.getText() + "\n" + l);
                                       } else {
                                           banned++;
                                           bannedAltArea.setText(bannedAltArea.getText() + "\n" + l);
                                       }
                                   } catch (Exception e) {
                                       banned++;
                                       bannedAltArea.setText(bannedAltArea.getText() + "\n" + l);
                                   }
                               } else {
                                   mojangBanned++;
                               }
                               Platform.runLater(() -> {
                                   workingAltsTextField.setText(String.valueOf(working));
                                   bannedAltsTextField.setText(String.valueOf(banned));
                                   mojangBannedAltsTextField.setText(String.valueOf(mojangBanned));
                               });
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
               isChecking = false;
           }
       };
       thread = new Thread(runnable);
       thread.start();
    }

    public void stopChecking(ActionEvent event){
        thread.stop();
    }

    public void howToCheckWithoutProxies(ActionEvent event){
        Runnable run = () ->{
            try {
                Desktop.getDesktop().browse(new URI("http://veiv.de/checkWithoutProxy.html"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        };
        new Thread(run).start();
    }

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

    public void close(ActionEvent event) {
        System.exit(0);
    }

    public boolean canCheck() {
        if (altListTextField.getText().equals("")) {
            Util.showPopUp("Please add an Alt-List");
            return false;
        }
        if (proxyListTextField.getText().equals("") && useProxy.isSelected()) {
            Util.showPopUp("Add a Proxy-List or uncheck 'Use Proxy List'");
            return false;
        }
        if (serverTextField.getText().equals("")) {
            Util.showPopUp("Select a Server!");
            return false;
        }
        if (!isChecking) {
            return true;
        } else {
            Util.showPopUp("Already Checking!");
            return false;
        }
    }

}
