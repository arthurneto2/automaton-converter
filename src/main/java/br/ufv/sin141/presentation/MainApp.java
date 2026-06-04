package br.ufv.sin141.presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/br/ufv/sin141/presentation/main-view.fxml"));
        Parent root = loader.load();

        stage.setTitle("AFD&GR");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
