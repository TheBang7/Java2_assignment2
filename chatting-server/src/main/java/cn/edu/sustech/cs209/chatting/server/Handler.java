package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageSent;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.RoomType;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

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
          case C_S_addNewUser:
            System.out.println("addNewUser");
            addNewUser(message);
            break;
          case C_S_requirePrivateChat:
            System.out.println("requirePrivateChat");
            requirePrivateChat(message);
            break;
          case sendMessage:
            dealMessage(message);
            break;
          default:
            break;
        }
      }//处理服务端发来的所有信息


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void dealMessage(Message message) throws IOException {
    MessageSent m = message.getMessageSent();
    ChatRoom room1 = Server.Messages.get(m.getSentBy()).get(m.getSendTo());
    Message back = new Message(MessageType.S_C_updateMessage);
    room1.addMessage(m);
    back.setChatRoom(new ChatRoom(room1));
    out.writeObject(back);
    out.flush();

    ChatRoom room2 = Server.Messages.get(m.getSendTo()).get(m.getSentBy());
    room2.addMessage(m);
    back = new Message(MessageType.S_C_updateMessage);
    back.setChatRoom(new ChatRoom(room2));
    ObjectOutputStream o = Server.UserToWriter.get(m.getSendTo());
    o.writeObject(back);
    o.flush();

  }


  private void addNewUser(Message message) throws IOException {
    Server.UserList.add(message.getUsername());
    Server.writers.add(out);
    Server.UserToWriter.put(message.getUsername(), out);
    Server.Messages.put(message.getUsername(), new HashMap<>());

    System.out.println(Server.UserList);
    Message message1 = new Message(MessageType.S_C_addNewUser);
    message1.setUsername(message.getUsername());
    for (ObjectOutputStream writer : Server.writers) {
      writer.writeObject(message1);
      writer.flush();
    }//更新每个用户的在线列表
  }

  private void sendUserList() throws IOException {
    Message message = new Message(MessageType.S_C_UserSent);
    message.setUsers(Server.UserList);
    out.writeObject(message);
    out.flush();
  }

  private void sendUserListToUpdate() throws IOException {
    Message message = new Message(MessageType.S_C_UserSentToCheck);
    message.setUsers(Server.UserList);
    out.writeObject(message);
    out.flush();
  }

  private void requirePrivateChat(Message message) throws IOException {
    String user1, user2;
    user1 = message.getUsername();
    user2 = message.getSUser();

    HashMap<String, ChatRoom> t = Server.Messages.get(user1);
    if (t.containsKey(user2)) {
      sendPrivateChat(t.get(user2));
    } else {
      HashSet<String> list = new HashSet<>();
      list.add(user1);
      list.add(user2);
      ChatRoom r1 = new ChatRoom(user2, user1, RoomType.one, list);
      t.put(user2, r1);
      ChatRoom r2 = new ChatRoom(user1, user2, RoomType.one, list);
      Server.Messages.get(user1).put(user2, r1);
      Server.Messages.get(user2).put(user1, r2);
      sendPrivateChat(t.get(user2));
    }


  }

  private void sendPrivateChat(ChatRoom chatRoom) throws IOException {
    Message message = new Message(MessageType.S_C_sendPrivateChat);
    message.setChatRoom(chatRoom);
    out.writeObject(message);
    out.flush();
  }
}


