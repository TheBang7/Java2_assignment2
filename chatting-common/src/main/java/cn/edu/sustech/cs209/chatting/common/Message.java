package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.HashSet;

public class Message implements Serializable {

  MessageType type;
  MessageSent messageSent;
  HashSet<String> users;
  String username;

  public Message(MessageType messageType) {
    this.type = messageType;
    messageSent = null;
    users = new HashSet<>();
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
