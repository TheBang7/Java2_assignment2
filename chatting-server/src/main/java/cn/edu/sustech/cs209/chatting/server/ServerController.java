package cn.edu.sustech.cs209.chatting.server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class ServerController {

  @FXML
  private Label serverStatusLabel;

  @FXML
  private Label userCountLabel;

  public void setUserList(List<String> userList) {
    userCountLabel.setText("连接人数：" + userList.size());
  }
}
