package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRoom implements Serializable {

  private String chatRoom;
  private String creator;
  private RoomType roomType;
  private List<String> users;
  private ArrayList<MessageSent> messages;

  @Override
  public String toString() {
    return chatRoom;
  }

  public ChatRoom(String chatRoom, String creator, RoomType roomType, List<String> users) {
    this.chatRoom = chatRoom;
    this.creator = creator;
    this.roomType = roomType;
    this.users = users;
    messages = new ArrayList<>();
    messages.add(new MessageSent(System.currentTimeMillis(), creator, "TheBang", "Hello"));
  }


  public String getChatRoom() {
    return chatRoom;
  }

  public void setChatRoom(String chatRoom) {
    this.chatRoom = chatRoom;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public RoomType getRoomType() {
    return roomType;
  }

  public void setRoomType(RoomType roomType) {
    this.roomType = roomType;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

  public ArrayList<MessageSent> getMessages() {
    return messages;
  }


}
