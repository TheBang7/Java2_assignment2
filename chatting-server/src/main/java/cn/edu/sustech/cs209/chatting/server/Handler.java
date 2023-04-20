package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageSent;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.MyUser;
import cn.edu.sustech.cs209.chatting.common.RoomType;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Handler implements Runnable {

  private final Socket socket;

  private ObjectInputStream in;
  private InputStream inputStream;
  private ObjectOutputStream out;
  private OutputStream outputStream;
  private Server server;


  public Handler(Socket socket, Server server) {
    this.socket = socket;
    this.server = server;
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
          case C_S_register:
            dealRegister(message);
            break;
          case C_S_login:
            dealLogin(message);
            break;
          case C_S_GetUserList://发送并更新用户列表
            System.out.println("GetUserList");
            sendUserList();
            break;
          case C_S_addNewUser:
            System.out.println("addNewUser");
            addNewUser(message);
            break;
          case C_S_requirePrivateChat:
            System.out.println("requirePrivateChat");
            requirePrivateChat(message);
            break;
          case C_S_sendMessageToPrivate:
            dealMessage(message);
            break;
          case C_S_requireGroupChat:
            creatGroupChat(message);
            break;
          case C_S_sendMessageToGroup:
            dealGroupMessage(message);
            break;
          case C_S_quit:
            dealQuit(message);
            socket.close();
            break;
          case C_S_quitFromGroup:
            dealQuitFromGroup(message);
            break;
          case C_S_SendFileToPrivate:
            SendFileToPrivate(message);
            break;
          default:
            break;
        }
      }//处理服务端发来的所有信息


    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void dealQuitFromGroup(Message message) throws IOException {
    ChatRoom c = message.getChatRoom();
    Message back = new Message(MessageType.S_C_removeFromGroup);
    back.setUsername(message.getUsername());
    back.setChatRoom(c);
    for (String suser : c.getUsers()) {
      if (!suser.equals(message.getUsername())) {
        ObjectOutputStream o = server.UserToWriter.get(suser);
        o.writeObject(back);
        o.flush();
      }
    }
  }

  private void dealQuit(Message message) throws IOException {
    String username = message.getUsername();
    MyUser user = server.nameToUser.get(username);
    server.CurrentUserList.remove(user);
    server.CurrentUserNameList.remove(username);
    server.writers.remove(server.UserToWriter.get(username));
    server.UserToWriter.remove(username);
    Message back = new Message(MessageType.S_C_removeUser);
    back.setUser(user);
    back.setUsername(username);
    for (ObjectOutputStream o : server.writers) {
      o.writeObject(back);
      o.flush();
    }
  }

  private void SendFileToPrivate(Message message) throws IOException {
    ObjectOutputStream o = server.UserToWriter.get(message.getSUser());
    Message back = new Message(MessageType.S_C_ReceiveFile);
    back.setData(message.getData());
    back.setUsername(message.getUsername());
    back.setFilename(message.getFilename());
    o.writeObject(back);
    o.flush();
  }

  private void dealRegister(Message message) throws IOException {
    MyUser user = message.getUser();
    String username = user.getUsername();
    Message back = new Message(MessageType.S_C_Register);
    if (server.nameToUser.containsKey(username)) {
      back.setTell("Exist " + username);
    } else {
      server.nameToUser.put(username, user);
      back.setTell("success");
      addNewUser(message);
    }
    back.setUser(user);
    out.writeObject(back);
    out.flush();
  }

  private void dealLogin(Message message) throws IOException {
    MyUser user = message.getUser();
    String username = user.getUsername();
    Message back = new Message(MessageType.S_C_Login);
    if (!server.nameToUser.containsKey(username)) {
      back.setTell("notExist");
    } else if (server.CurrentUserNameList.contains(username)) {
      back.setTell("This account is online!");

    } else {
      String pastWord = user.getPassWord();
      String TPastWord = server.nameToUser.get(username).getPassWord();
      if (!pastWord.equals(TPastWord)) {
        back.setTell("Wrong Password");
      } else {
        back.setTell("success");
        addNewUser(message);
      }
    }
    back.setUser(user);
    out.writeObject(back);
    out.flush();
  }


  private void creatGroupChat(Message message) {
    List<String> users = message.getSelectedUsers();
    StringBuilder s = new StringBuilder(message.getUsername());
    if (users.size() == 0) {
      return;
    } else if (users.size() < 3) {
      for (String t : users) {
        s.append(",").append(t);
      }
    } else {
      for (int i = 0; i < 2; i++) {
        s.append(",").append(users.get(i));
      }
      s.append(" (").append(users.size()).append(")...");
    }
    users.add(message.getUsername());
    ChatRoom room = new ChatRoom(s.toString(), message.getUsername(), RoomType.group,
        users);//生成新的聊天室
    Message back = new Message(MessageType.S_C_sendGroupChat);
    back.setChatRoom(room);
    try {
      for (String user : users) {
        server.UserToGroup.get(user).put(room.getChatRoom(), room);
        ObjectOutputStream o = server.UserToWriter.get(user);
        back.setUsername(user);
        o.writeObject(back);
        o.flush();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void dealMessage(Message message) throws IOException {

    MessageSent m = message.getMessageSent();
    MyUser user1 = server.nameToUser.get(m.getSentBy());
    MyUser user2 = server.nameToUser.get(m.getSendTo());
    user1.LastMessageTime.put(user2.getUsername(), System.currentTimeMillis());
    user2.LastMessageTime.put(user1.getUsername(), System.currentTimeMillis());

    ChatRoom room1 = server.Messages.get(m.getSentBy()).get(m.getSendTo());
    Message back = new Message(MessageType.S_C_updateMessage);
    room1.addMessage(m);
    back.setChatRoom(new ChatRoom(room1));
    back.setSUser(user2.getUsername());
    out.writeObject(back);
    out.flush();

    ChatRoom room2 = server.Messages.get(m.getSendTo()).get(m.getSentBy());
    room2.addMessage(m);
    back = new Message(MessageType.S_C_updateMessage);
    back.setChatRoom(new ChatRoom(room2));
    back.setSUser(user1.getUsername());
    ObjectOutputStream o = server.UserToWriter.get(m.getSendTo());
    o.writeObject(back);
    o.flush();

  }

  private void dealGroupMessage(Message message) throws IOException {
    MessageSent m = message.getMessageSent();
    ChatRoom room = server.UserToGroup.get(message.getUsername())
        .get(message.getMessageSent().getSendTo());

    Message back = new Message(MessageType.S_C_updateGroupMessage);
    room.addMessage(m);
    back.setChatRoom(room);
    for (String user : room.getUsers()) {
      ObjectOutputStream o = server.UserToWriter.get(user);
      o.writeObject(back);
      o.flush();
    }
  }


  private void addNewUser(Message message) throws IOException {
    MyUser user = message.getUser();
    server.CurrentUserList.add(user);
    server.RegUserList.add(user);
    server.nameToUser.put(user.getUsername(), user);
    server.CurrentUserNameList.add(user.getUsername());
    server.RegUserNameList.add(user.getUsername());
    server.writers.add(out);
    server.UserToWriter.put(message.getUsername(), out);
    if (!server.Messages.containsKey(message.getUsername())) {
      server.Messages.put(message.getUsername(), new HashMap<>());
    }
    server.UserToGroup.put(message.getUsername(), new HashMap<>());

    System.out.println(server.CurrentUserNameList);
    Message message1 = new Message(MessageType.S_C_addNewUser);
    message1.setUsername(user.getUsername());
    message1.setUser(user);
    for (ObjectOutputStream writer : server.writers) {
      writer.writeObject(message1);
      writer.flush();
    }//更新每个用户的在线列表
  }

  private void sendUserList() throws IOException {
    Message message = new Message(MessageType.S_C_UserSent);
    message.setUserNames(server.CurrentUserNameList);
    message.setUsers(server.CurrentUserList);
    out.writeObject(message);
    out.flush();
  }


  private void requirePrivateChat(Message message) throws IOException {
    String user1, user2;
    user1 = message.getUsername();
    user2 = message.getSUser();

    HashMap<String, ChatRoom> t = server.Messages.get(user1);
    if (t.containsKey(user2)) {
      sendPrivateChat(t.get(user2));
    } else {
      ArrayList<String> list = new ArrayList<>();
      list.add(user1);
      list.add(user2);
      ChatRoom r1 = new ChatRoom(user2, user1, RoomType.one, list);
      t.put(user2, r1);
      ChatRoom r2 = new ChatRoom(user1, user2, RoomType.one, list);
      server.Messages.get(user1).put(user2, r1);
      server.Messages.get(user2).put(user1, r2);
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


