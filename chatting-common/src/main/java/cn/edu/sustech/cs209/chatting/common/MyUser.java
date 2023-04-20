package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.HashMap;

public class MyUser implements Serializable {

  public HashMap<String, ChatRoom> Messages;
  public HashMap<String, ChatRoom> GroupChat;
  public boolean isOnline;
  public String username;
  public String PassWord;
  public Integer Port;
  public String hostname;
  public HashMap<String, Long> LastMessageTime;

  public MyUser(String username, String passWord, String hostname, Integer port) {
    this.username = username;
    PassWord = passWord;
    Port = port;
    this.hostname = hostname;
    isOnline = true;
    Messages = new HashMap<>();
    GroupChat = new HashMap<>();
    LastMessageTime = new HashMap<>();
  }

  public HashMap<String, ChatRoom> getMessages() {
    return Messages;
  }

  public void setMessages(
      HashMap<String, ChatRoom> messages) {
    Messages = messages;
  }

  public HashMap<String, ChatRoom> getGroupChat() {
    return GroupChat;
  }

  public void setGroupChat(
      HashMap<String, ChatRoom> groupChat) {
    GroupChat = groupChat;
  }

  public boolean isOnline() {
    return isOnline;
  }

  public void setOnline(boolean online) {
    isOnline = online;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassWord() {
    return PassWord;
  }

  public void setPassWord(String passWord) {
    PassWord = passWord;
  }

  public Integer getPort() {
    return Port;
  }

  public void setPort(Integer port) {
    Port = port;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }
}
