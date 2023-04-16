package cn.edu.sustech.cs209.chatting.client;

import java.nio.charset.StandardCharsets;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) throws IOException {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
    }
}
