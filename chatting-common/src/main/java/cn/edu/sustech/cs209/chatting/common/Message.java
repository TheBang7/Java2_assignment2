package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Message implements Serializable {

  MessageType type;
  MessageSent messageSent;
  HashSet<String> users;
  String username;
  String SUser;
  ArrayList<MessageSent> S_C_Messages;
  ChatRoom chatRoom;

  public Message(MessageType messageType) {
    this.type = messageType;
    messageSent = null;
    users = new HashSet<>();
    S_C_Messages = null;
  }

  public ChatRoom getChatRoom() {
    return chatRoom;
  }

  public void setChatRoom(ChatRoom chatRoom) {
    this.chatRoom = new ChatRoom(chatRoom);
  }

  public void setS_C_Messages(ArrayList<MessageSent> s_C_Messages) {
    S_C_Messages = s_C_Messages;
  }

  public List<MessageSent> getS_C_Messages() {
    return S_C_Messages;
  }

  public String getSUser() {
    return SUser;
  }

  public void setSUser(String SUser) {
    this.SUser = SUser;
  }

  public void setMessageSent(MessageSent messageSent) {
    this.messageSent = messageSent;
  }

  public MessageType getType() {
    return type;
  }

  public HashSet<String> getUsers() {
    return users;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public void setUsers(HashSet<String> users) {
    this.users = users;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public MessageSent getMessageSent() {
    return messageSent;
  }
}
