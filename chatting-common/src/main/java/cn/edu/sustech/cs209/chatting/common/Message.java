package cn.edu.sustech.cs209.chatting.common;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Message implements Serializable {

  MessageType type;
  MessageSent messageSent;
  HashSet<String> userNames;
  HashSet<MyUser> users;
  String username;
  String SUser;
  ArrayList<MessageSent> S_C_Messages;
  ChatRoom chatRoom;
  List<String> selectedUsers;
  ObjectOutputStream out;
  MyUser user;
  String tell;
  byte[] data;
  String filename;

  public Message(MessageType messageType) {
    this.type = messageType;
    messageSent = null;
    userNames = new HashSet<>();
    S_C_Messages = null;
    selectedUsers = new ArrayList<>();
    out = null;
  }

  public HashSet<MyUser> getUsers() {
    return users;
  }

  public void setUsers(HashSet<MyUser> users) {
    this.users = users;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public String getTell() {
    return tell;
  }

  public void setTell(String tell) {
    this.tell = tell;
  }

  public MyUser getUser() {
    return user;
  }

  public void setUser(MyUser user) {
    this.user = user;
  }

  public ObjectOutputStream getOut() {
    return out;
  }

  public void setOut(ObjectOutputStream out) {
    this.out = out;
  }

  public List<String> getSelectedUsers() {
    return selectedUsers;
  }

  public void setSelectedUsers(List<String> selectedUsers) {
    this.selectedUsers = selectedUsers;
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

  public HashSet<String> getUserNames() {
    return userNames;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public void setUserNames(HashSet<String> userNames) {
    this.userNames = userNames;
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
