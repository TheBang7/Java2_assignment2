package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Handler implements Runnable {

  private final Socket socket;

  private ObjectInputStream in;
  private InputStream inputStream;
  private ObjectOutputStream out;
  private OutputStream outputStream;


  public Handler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      inputStream = socket.getInputStream();
      in = new ObjectInputStream(inputStream);
      outputStream = socket.getOutputStream();
      out = new ObjectOutputStream(outputStream);
      Server.writers.add(out);
      while (socket.isConnected()) {
        Message message = (Message) in.readObject();
        switch (message.getType()) {
          case C_S_GetUserList://发送并更新用户列表
            System.out.println("GetUserList");
            sendUserList();
            break;
          case C_S_GetUserListToCheck://新用户登录
            System.out.println("GetUserListToCheck");
            sendUserListToUpdate();
            break;
          case sendMessage:
            break;
          case C_S_addNewUser:
            System.out.println("addNewUser");
            Server.UserList.add(message.getUsername());
            Server.Messages.put(message.getUsername(), new HashMap<>());
            System.out.println(Server.UserList);
            Message message1 = new Message(MessageType.S_C_addNewUser);
            message1.setUsername(message.getUsername());
            for (ObjectOutputStream writer : Server.writers) {
              writer.writeObject(message1);
            }//更新每个用户的在线列表
            break;
          default:
            break;
        }
      }


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void sendUserList() throws IOException {
    Message message = new Message(MessageType.S_C_UserSent);
    message.setUsers(Server.UserList);
    out.writeObject(message);
  }

  private void sendUserListToUpdate() throws IOException {
    Message message = new Message(MessageType.S_C_UserSentToCheck);
    message.setUsers(Server.UserList);
    out.writeObject(message);
  }
}


