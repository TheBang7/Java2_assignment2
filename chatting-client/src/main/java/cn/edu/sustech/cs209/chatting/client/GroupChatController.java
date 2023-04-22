package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageSent;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class GroupChatController implements Initializable {

  @FXML
  private Label GroupName;

  @FXML
  private ListView<MessageSent> chatContentList;

  @FXML
  private ListView<String> chatList;

  @FXML
  private Label currentOnlineCnt;

  @FXML
  private Label currentUsername;

  @FXML
  private TextArea inputArea;

  @FXML
  private Font x3;

  @FXML
  private Color x4;
  private String username;
  private List<String> UserList;
  ObjectOutputStream out;
  ChatRoom chatRoom;
  @FXML
  private ComboBox<String> emojiComboBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservableList<String> emojiList = FXCollections.observableArrayList(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉",
        "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😋", "😛", "😝",
        "😜", "🤪", "🤔", "🤨", "😐", "😑", "😶", "😏", "😒", "🙄", "😬",
        "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢", "🤮",
        "🥴", "😵", "🤯", "🤠", "🥳", "😎", "🤓", "🧐", "😕", "😟", "🙁",
        "☹️", "😮", "😯", "😲", "😳", "🥺", "😦", "😧", "😨", "😰", "😥",
        "😢", "😭", "😱", "😖", "😣", "😞"
    );
    emojiComboBox.setItems(emojiList);
    emojiComboBox.getSelectionModel().selectFirst();
    emojiComboBox.setOnAction(event -> {
      String selectedEmoji = emojiComboBox.getSelectionModel().getSelectedItem().toString();
      inputArea.appendText(selectedEmoji);
    });
    chatContentList.setCellFactory(new GroupChatController.MessageCellFactory1());
    out = null;
  }


  @FXML
  void doSendMessage(ActionEvent event) throws IOException {
    String s = inputArea.getText();
    if (!Objects.equals(s, "")) {
      Message message = new Message(MessageType.C_S_sendMessageToGroup);
      MessageSent m = new MessageSent(System.currentTimeMillis(), username,
          chatRoom.getChatRoom(),
          s);
      message.setMessageSent(m);
      message.setUsername(username);
      message.setSelectedUsers(UserList);
      out.writeObject(message);
      out.flush();
      Platform.runLater(() -> inputArea.clear());
    }
  }

  public void updateMessage(Message message) {

    ObservableList<MessageSent> arr = FXCollections.observableList(
        message.getChatRoom().getMessages());
    Platform.runLater(() -> chatContentList.setItems(arr));
    Platform.runLater(() -> chatContentList.impl_updatePeer());

  }

  public void reload(Message message) {
    username = message.getUsername();
    UserList = message.getChatRoom().getUsers();
    Platform.runLater(() -> {
      GroupName.setText(message.getChatRoom().getChatRoom());
      GroupName.impl_updatePeer();
    });
    upDateUsers();
    loadChatContentList(message);
    out = message.getOut();
    chatRoom = message.getChatRoom();
  }

  public void upDateUsers() {
    Platform.runLater(() -> {
      currentUsername.setText("CurrentUser:" + username);
      currentOnlineCnt.setText("currentOnlineCnt:" + UserList.size());
      currentUsername.impl_updatePeer();
      currentOnlineCnt.impl_updatePeer();
      ArrayList<String> a = new ArrayList<>(UserList);
      a.remove(username);
      ObservableList<String> arr1 = FXCollections.observableList(a);
      chatList.setItems(arr1);
      chatList.impl_updatePeer();
    });
  }

  public void loadChatContentList(Message message) {
    ObservableList<MessageSent> arr = FXCollections.observableList(
        message.getChatRoom().getMessages());
    Platform.runLater(() -> chatContentList.setItems(arr));
    Platform.runLater(() -> chatContentList.impl_updatePeer());
  }

  public void removeUser(Message message) {
    UserList.remove(message.getUsername());
    if (UserList.size() <= 1) {
      Platform.exit();
    }
    upDateUsers();
  }

  public void quit(String username) throws IOException {
    Message message = new Message(MessageType.C_S_quitFromGroup);
    message.setUsername(username);
    message.setChatRoom(chatRoom);
    out.writeObject(message);
    out.flush();
  }


  private class MessageCellFactory1 implements
      Callback<ListView<MessageSent>, ListCell<MessageSent>> {

    @Override
    public ListCell<MessageSent> call(ListView<MessageSent> param) {
      return new ListCell<MessageSent>() {

        @Override
        public void updateItem(MessageSent msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(msg.getData());

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}



