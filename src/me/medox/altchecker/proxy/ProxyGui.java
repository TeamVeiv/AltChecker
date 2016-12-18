package me.medox.altchecker.proxy;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.medox.altchecker.proxy.gui.ProxyController;

import java.io.IOException;

/**
 * Created by v3v on 17.12.16.
 */
public class ProxyGui {

    private static ProxyGui instance = new ProxyGui();

    private static Stage stage;
    private static Scene scene;
    private static Parent parent;

    public void openGui(){
        try {
            stage = new Stage();
            parent = FXMLLoader.load(getClass().getResource("gui/ProxyGui.fxml"));
            scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Proxy Checker");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProxyGui getInstance(){
        return instance;
    }

}
