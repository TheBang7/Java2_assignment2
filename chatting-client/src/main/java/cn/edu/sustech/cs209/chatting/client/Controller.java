package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageSent;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;


public class Controller implements Initializable {

  public HashSet<String> UserList;

  @FXML
  ListView<MessageSent> chatContentList;//保留的文本信息

  @FXML
  private ListView<String> chatList;//可选的聊天人群
  @FXML
  private TextArea inputArea;//输入的文本
  @FXML
  private Label currentOnlineCnt;//当前在线人数
  @FXML
  private Label currentUsername;
  String username;
  private ObjectOutputStream out;
  public static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 32, 1,
      TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));
  private clientListener listener;
  private ChatRoom chatRoom;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    Dialog<String> dialog = new TextInputDialog();
    dialog.setTitle("Login");
    dialog.setHeaderText(null);
    dialog.setContentText("Username:");

    chatRoom = null;
    Optional<String> input = dialog.showAndWait();
    if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
      username = input.get();
      try {
        listener = new clientListener("localhost", 1234, username, this);
        out = listener.getOut();
        poolExecutor.execute(listener);
        //将监听器加入到线程池
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      System.out.println("Invalid username " + input + ", exiting");
      Platform.exit();
    }
    chatContentList.setCellFactory(new MessageCellFactory());
  }

  public void upDateUsers() {
    Platform.runLater(() -> currentUsername.setText("CurrentUser:" + username));
    Platform.runLater(() -> currentOnlineCnt.setText("currentOnlineCnt:" + UserList.size()));
    Platform.runLater(() -> currentUsername.impl_updatePeer());
    Platform.runLater(() -> currentOnlineCnt.impl_updatePeer());
    ArrayList<String> a = new ArrayList<>(UserList);
    a.remove(username);
    ObservableList<String> arr1 = FXCollections.observableList(a);
    Platform.runLater(() -> chatList.setItems(arr1));
    Platform.runLater(() -> chatList.impl_updatePeer());
  }

  @FXML
  public void createPrivateChat() throws IOException {
    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    // FIXME: get the user list from server, the current user's name should be filtered out
    HashSet<String> temp = new HashSet<>(UserList);
    temp.remove(username);
    userSel.getItems().addAll(temp);

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    // TODO: if the current user already chatted with the selected user, just open the chat with that user
    // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
    System.out.println("Selected user: " + user.get());
    String SelUser = user.get();
    if (SelUser != null) {
      requirePrivateChat(SelUser);//向服务端请求对应的聊天
    }
  }


  public void requirePrivateChat(String SUser) throws IOException {
    Message message = new Message(MessageType.C_S_requirePrivateChat);
    message.setUsername(username);
    message.setSUser(SUser);
    out.writeObject(message);
    out.flush();
  }


  public void loadChatContentList(Message message) {
    this.chatRoom = message.getChatRoom();
    ObservableList<MessageSent> arr = FXCollections.observableList(
        message.getChatRoom().getMessages());
    Platform.runLater(() -> chatContentList.setItems(arr));
    Platform.runLater(() -> chatContentList.impl_updatePeer());
  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name. You can select
   * several users that will be joined in the group chat, including yourself.
   * <p>
   * The naming rule for group chats is similar to WeChat: If there are > 3 users: display the first
   * three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for
   * example: UserA, UserB, UserC... (10) If there are <= 3 users: do not display the ellipsis, for
   * example: UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() {
    List<String> selectedUsers = new ArrayList<>();
    Stage stage = new Stage();
    ListView<String> userSelector = new ListView<>();
    userSelector.setItems(chatList.getItems());
    userSelector.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      selectedUsers.addAll(userSelector.getSelectionModel().getSelectedItems());
      stage.close();
    });

    VBox box = new VBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSelector, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

    if (!selectedUsers.isEmpty()) {
      System.out.println("Selected users: " + selectedUsers);
      requireGroupChat(selectedUsers); //向服务端请求对应的聊天
    }
  }

  private void requireGroupChat(List<String> selectedUsers) {

  }

  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed. After sending the message, you should clear the text input
   * field.
   */
  @FXML
  public void doSendMessage() throws IOException {
    // TODO
    String s = inputArea.getText();
    if (!Objects.equals(s, "")) {
      Message message = new Message(MessageType.sendMessage);
      MessageSent m = new MessageSent(System.currentTimeMillis(), username, chatRoom.getChatRoom(),
          s);
      message.setMessageSent(m);
      out.writeObject(message);
      out.flush();
      Platform.runLater(() -> inputArea.clear());
    }
  }

  public void updateMessage(Message message) {
    if (chatRoom != null && chatRoom.getChatRoom().equals(message.getChatRoom().getChatRoom())) {
      ObservableList<MessageSent> arr = FXCollections.observableList(
          message.getChatRoom().getMessages());
      Platform.runLater(() -> chatContentList.setItems(arr));
      Platform.runLater(() -> chatContentList.impl_updatePeer());
    }
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model. Hint: you
   * may also define a cell factory for the chats displayed in the left panel, or simply override
   * the toString method.
   */
  private class MessageCellFactory implements
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
