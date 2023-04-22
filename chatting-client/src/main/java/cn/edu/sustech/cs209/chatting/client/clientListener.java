package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.MyUser;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class clientListener implements Runnable {

  private String hostname;
  private int port;
  private String username;
  private Socket socket;

  private ObjectInputStream in;
  private InputStream inputStream;
  private ObjectOutputStream out;
  private OutputStream outputStream;

  private final Controller chatController;

  public static clientListener instance;

  public clientListener(String hostname, int port, String username,
      Controller chatController) throws IOException {
    this.hostname = hostname;
    this.port = port;
    this.username = username;
    this.chatController = chatController;
    instance = this;

    socket = new Socket(hostname, port);
    outputStream = socket.getOutputStream();
    out = new ObjectOutputStream(outputStream);
    inputStream = socket.getInputStream();
    in = new ObjectInputStream(inputStream);
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public void run() {
    //获取socket对象
    try {
      while (socket.isConnected()) {
        Message message = (Message) in.readObject();
        switch (message.getType()) {
          case S_C_Login:
            Login(message);
            break;
          case S_C_Register:
            Register(message);
            break;
          case S_C_UserSent:
            System.out.println("Get Users");
            GetUserList(message);
            break;
          case S_C_addNewUser:
            addNewUser(message);
            break;
          case S_C_sendPrivateChat:
            System.out.println("S_C_sendPrivateChat:" + message.getChatRoom());
            chatController.loadChatContentList(message);
            break;
          case S_C_updateMessage:
            System.out.println("S_C_updateMessage");
            chatController.updateMessage(message);
            break;
          case S_C_sendGroupChat:
            chatController.S_C_GroupChat(message);
            break;
          case S_C_updateGroupMessage:
            chatController.updateGroupMessage(message);
            break;
          case S_C_ServerShutDown:
            chatController.showMessage("Server shut down!");
            System.exit(0); // 正常终止程序
            break;
          case S_C_ReceiveFile:
            chatController.receiveFile(message);
            break;
          case S_C_removeUser:
            chatController.removeUser(message);
            break;
          case S_C_removeFromGroup:
            chatController.removeFromGroup(message);
            break;
          case S_C_quitSuccessfully:
            chatController.showMessage("Quit successfully!");
            socket.close();
            System.exit(0); // 正常终止程序
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();

    }
  }

  private void addNewUser(Message message) {
    chatController.UserNameList.add(message.getUsername());
    chatController.UserList.add(message.getUser());
    chatController.nameToUser.put(message.getUsername(), message.getUser());
    chatController.upDateUsers();
  }

  private void GetUserList(Message message) {
    chatController.UserNameList = message.getUserNames();
    chatController.UserList = message.getUsers();
    for (MyUser user : message.getUsers()) {
      chatController.nameToUser.put(user.getUsername(), user);
    }
    chatController.upDateUsers();
  }

  private void Login(Message message) {
    if (message.getTell().equals("success")) {
      chatController.setUser(message.getUser());
    } else {
      System.out.println(message.getTell());
      chatController.MyQuit();
    }
  }

  private void Register(Message message) {
    if (message.getTell().equals("success")) {
      chatController.setUser(message.getUser());
    } else {
      System.out.println(message.getTell());
      chatController.MyQuit();
    }
  }

  public void RequireUsers() throws IOException {
    Message message = new Message(MessageType.C_S_GetUserList);
    out.writeObject(message);
    out.flush();
  }


  public void addUser() throws IOException {
    Message message = new Message(MessageType.C_S_addNewUser);
    message.setUsername(username);
    out.writeObject(message);
    out.flush();
  }

  public void requirePrivateChat(String SUser) throws IOException {
    Message message = new Message(MessageType.C_S_requirePrivateChat);
    message.setUsername(username);
    message.setSUser(SUser);
    out.writeObject(message);
    out.flush();
  }

  public ObjectOutputStream getOut() {
    return out;
  }
}
