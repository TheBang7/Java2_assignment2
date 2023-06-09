package cn.edu.sustech.cs209.chatting.client;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

  public static void main(String[] args) throws IOException {
    launch();
  }

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setScene(scene);
    stage.setTitle("Chatting Client");
    stage.setOnCloseRequest(event -> {
      Controller c = fxmlLoader.getController();
      try {
        c.quit();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.exit(0);
    });
    stage.show();
  }
}
