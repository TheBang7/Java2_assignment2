package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageSent;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.MyUser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Controller implements Initializable {

  public HashSet<String> UserNameList;
  public HashSet<MyUser> UserList;
  public HashMap<String, MyUser> nameToUser;
  public HashMap<String, GroupChatController> GroupToCtrl;
  public HashMap<String, Stage> GroupToStage;

  @FXML
  ListView<MessageSent> chatContentList; //保留的文本信息

  @FXML
  private ListView<String> chatList; //可选的聊天人群
  @FXML
  private TextArea inputArea; //输入的文本
  @FXML
  private Label currentOnlineCnt; //当前在线人数
  @FXML
  private Label currentUsername;
  @FXML
  private ComboBox<String> emojiComboBox;
  String username;
  private ObjectOutputStream out;
  public ThreadPoolExecutor poolExecutor;
  private clientListener listener;
  private ChatRoom chatRoom;
  private MyUser user;
  LoginController loginController;
  public Stage loginStage;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    nameToUser = new HashMap<>();
    GroupToStage = new HashMap<>();
    poolExecutor = new ThreadPoolExecutor(16, 32, 1,
        TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));
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
    chatRoom = null;
    GroupToCtrl = new HashMap<>();
    chatContentList.setCellFactory(new MessageCellFactory());
    chatList.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        try {
          if (selectedItem != null) {
            requirePrivateChat(selectedItem);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        System.out.println("Clicked on " + selectedItem);
      }
    });

    tryLogin();

  }

  public void MyQuit() {
    Platform.exit();
  }

  public void tryLogin() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(
          getClass().getResource("Login.fxml"));

      loginStage = new Stage();
      loginStage.setScene(new Scene(fxmlLoader.load()));
      loginStage.setTitle("Login");
      loginController = fxmlLoader.getController();
      loginController.setCtrl(this);
      loginStage.showAndWait();

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void client(String username, String password, String hostname, String port) {
    try {
      clientListener listener = new clientListener(hostname, Integer.parseInt(port), username,
          this);
      this.out = listener.getOut();
      poolExecutor.execute(listener);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void login(String username, String password, String hostname, String port)
      throws IOException {
    requireUsers();
    Message message = new Message(MessageType.C_S_login);
    message.setUsername(username);
    message.setUser(new MyUser(username, password, hostname, Integer.parseInt(port)));
    out.writeObject(message);
    out.flush();
    closeLogin();
  }

  public void requireUsers() throws IOException {
    Message message = new Message(MessageType.C_S_GetUserList);
    out.writeObject(message);
    out.flush();
  }

  public void setUser(MyUser user) {
    this.username = user.getUsername();
    this.user = user;
    upDateUsers();
  }

  public void closeLogin() {
    if (loginStage != null) {
      loginStage.close();
    }
  }

  public void register(String username, String password, String hostname, String port)
      throws IOException {
    requireUsers();
    Message message = new Message(MessageType.C_S_register);
    message.setUsername(username);
    message.setUser(new MyUser(username, password, hostname, Integer.parseInt(port)));
    out.writeObject(message);
    out.flush();
    closeLogin();
  }


  public void quit() throws IOException {
    Message message = new Message(MessageType.C_S_quit);
    message.setUsername(username);
    message.setUser(user);
    out.writeObject(message);
    out.flush();
    Platform.exit();
    poolExecutor.shutdownNow();
    System.exit(1);
  }

  public void upDateUsers() {
    Platform.runLater(() -> currentUsername.setText("CurrentUser:" + username));
    Platform.runLater(
        () -> currentOnlineCnt.setText("currentOnlineCnt:" + UserNameList.size()));
    Platform.runLater(() -> currentUsername.impl_updatePeer());
    Platform.runLater(() -> currentOnlineCnt.impl_updatePeer());
    ArrayList<MyUser> b = new ArrayList<>(UserList);
    b.remove(user);
    ArrayList<String> a = new ArrayList<>();
    b.stream().sorted((o1, o2) -> {
      if (o1.LastMessageTime.containsKey(username) && o2.LastMessageTime.containsKey(
          username)) {
        return (int) (o2.LastMessageTime.get(username) - o1.LastMessageTime.get(username));
      } else if (!o1.LastMessageTime.containsKey(username)) {
        return 1;
      }
      return -1;
    }).forEach(e -> a.add(e.getUsername()));
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
    HashSet<String> temp = new HashSet<>(UserNameList);
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

    // TODO: if the current user already chatted with the selected user,
    //  just open the chat with that user
    // TODO: otherwise, create a new chat item in the left panel,
    //  the title should be the selected user's name
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
  public void createGroupChat() throws IOException {
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

  public void S_C_GroupChat(Message message) throws IOException {
    Platform.runLater(() -> {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GroupChat.fxml"));
      Stage stage = new Stage();
      try {
        stage.setScene(new Scene(fxmlLoader.load()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      stage.setTitle("GroupChat");
      stage.setOnCloseRequest(event -> {
        GroupChatController c = fxmlLoader.getController();
        try {
          c.quit(username);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

      });
      stage.show();
      GroupChatController groupChatController = fxmlLoader.getController();
      GroupToCtrl.put(message.getChatRoom().getChatRoom(), groupChatController);
      GroupToStage.put(message.getChatRoom().getChatRoom(), stage);
      message.setOut(out);
      message.setUsername(username);
      groupChatController.reload(message, this);
    });
  }

  public void removeGroup(String group) {
    GroupToCtrl.remove(group);
    Platform.runLater(() -> {
      GroupToStage.get(group).close();
      GroupToStage.remove(group);
    });

  }


  private void requireGroupChat(List<String> selectedUsers) throws IOException {
    Message message = new Message(MessageType.C_S_requireGroupChat);
    message.setSelectedUsers(selectedUsers);
    message.setUsername(username);
    out.writeObject(message);
    out.flush();
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
    if (chatRoom != null && !Objects.equals(s, "")) {
      Message message = new Message(MessageType.C_S_sendMessageToPrivate);
      MessageSent m = new MessageSent(System.currentTimeMillis(), username,
          chatRoom.getChatRoom(),
          s);
      message.setMessageSent(m);
      out.writeObject(message);
      out.flush();
      Platform.runLater(() -> inputArea.clear());
    }
  }

  public void updateMessage(Message message) {
    MyUser user1 = nameToUser.get(message.getSUser());
    user1.LastMessageTime.put(username, System.currentTimeMillis());
    if (chatRoom != null && chatRoom.getChatRoom()
        .equals(message.getChatRoom().getChatRoom())) {
      ObservableList<MessageSent> arr = FXCollections.observableList(
          message.getChatRoom().getMessages());
      Platform.runLater(() -> {
        chatContentList.setItems(arr);
        chatContentList.impl_updatePeer();
      });
      upDateUsers();
    } else {
      Platform.runLater(
          () -> showMessage(
              message.getChatRoom().getChatRoom() + "Send a message to you!"));
      upDateUsers();
    }
  }

  public void showMessage(String message) {
    Thread thread = new Thread(() -> {
      JOptionPane.showMessageDialog(null, message);
    });
    thread.start();
    try {
      thread.join(3000); // 暂停当前线程，等待5秒钟
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (thread.isAlive()) {
      thread.interrupt(); // 如果线程还在运行，就中断它
    }
  }

  public void updateGroupMessage(Message message) {
    GroupChatController c = GroupToCtrl.get(message.getChatRoom().getChatRoom());
    c.updateMessage(message);
  }

  @FXML
  Label filePathLabel;

  @FXML
  public void chooseFile(ActionEvent event) throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Choose File");
    File file = fileChooser.showOpenDialog(null);
    if (file != null) {
      if (!file.exists()) {
        System.out.println("File does not exist");
        return;
      }
      byte[] fileContent = Files.readAllBytes(file.toPath());
      Message message = new Message(MessageType.C_S_SendFileToPrivate);
      message.setFilename(file.getName());
      message.setData(fileContent);
      message.setUsername(username);
      message.setSUser(chatRoom.getChatRoom());
      out.writeObject(message);
      out.flush();
      System.out.println("File sent to client");
    }
  }

  public void receiveFile(Message message) throws IOException {

    try {
      int response = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
          message.getUsername() + " send a file to you,do you want to receive the file?",
          "File transfer", JOptionPane.YES_NO_OPTION);
      if (response == JOptionPane.YES_OPTION) {
        String fileName = message.getFilename(); // 从消息中获取文件名
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save file");
        fileChooser.setSelectedFile(new File(fileName)); // 设置默认文件名
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
          String filePath = fileChooser.getSelectedFile().getAbsolutePath();
          byte[] fileContent = message.getData();
          FileOutputStream fileOutputStream = new FileOutputStream(filePath);
          fileOutputStream.write(fileContent);
          fileOutputStream.flush();
          fileOutputStream.close();
          JOptionPane.showMessageDialog(null, "File saved to " + filePath);
        } else {
          JOptionPane.showMessageDialog(null, "File download canceled");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

  public void removeUser(Message message) {
    UserList.remove(message.getUser());
    UserNameList.remove(message.getUsername());
    upDateUsers();
    for (GroupChatController c : GroupToCtrl.values()) {
      c.removeUser(message);
    }
  }

  public void removeFromGroup(Message message) {
    GroupChatController c = GroupToCtrl.get(message.getChatRoom().getChatRoom());
    c.removeUser(message);
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
