package cn.edu.sustech.cs209.chatting.client;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private TextField hostnameField;
  @FXML
  private TextField portField;
  Controller controller;

  public void handleRegister() throws IOException {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String hostname = hostnameField.getText();
    String port = portField.getText();
    // TODO: Register the user with the given username, password, hostname, and port
    // and show a message indicating success or failure
    controller.client(username, password, hostname, port);
    controller.register(username, password, hostname, port);

  }

  public void setCtrl(Controller controller) {
    this.controller = controller;
  }


  public void handleLogin() throws IOException {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String hostname = hostnameField.getText();
    String port = portField.getText();
    // TODO: Check if there is a user with the given username, password, hostname, and port
    // among the currently registered users, and show a message indicating success or failure
    controller.client(username, password, hostname, port);
    controller.login(username, password, hostname, port);
  }
}