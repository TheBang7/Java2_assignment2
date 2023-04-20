package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import javafx.application.Platform;

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

  @Override
  public void run() {
    //获取socket对象
    try {

      firstTimeRegister();//检查是否有重复

      while (socket.isConnected()) {
        Message message = (Message) in.readObject();
        switch (message.getType()) {
          case S_C_UserSentToCheck:
            System.out.println(message.getUsers());
            if (message.getUsers().contains(username)) {
              System.out.println("Same Id!");
              Platform.exit();
            } else {
              System.out.println(message.getUsers());
              chatController.UserList = message.getUsers();
              chatController.upDateUsers();
              addUser();
            }//添加新用户到聊天目录
          case S_C_UserSent:
            System.out.println("Get Users");
            chatController.UserList = message.getUsers();
            chatController.upDateUsers();
            break;
          case S_C_addNewUser:
            chatController.UserList.add(message.getUsername());
            chatController.upDateUsers();
            System.out.println(chatController.UserList);
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
          default:
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void RequireUsers() throws IOException {
    Message message = new Message(MessageType.C_S_GetUserList);
    out.writeObject(message);
    out.flush();
  }

  public void firstTimeRegister() throws IOException {
    Message message = new Message(MessageType.C_S_GetUserListToCheck);
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
