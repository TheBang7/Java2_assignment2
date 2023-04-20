package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatRoom implements Serializable {

  private ArrayList<MessageSent> messages;
  private String chatRoom;
  private String creator;
  private RoomType roomType;
  private List<String> users;

  public ChatRoom(ChatRoom chatRoom) {
    this.chatRoom = chatRoom.chatRoom;
    this.creator = chatRoom.creator;
    this.roomType = chatRoom.roomType;
    this.users = new ArrayList<>(chatRoom.users);
    this.messages = new ArrayList<>();
    chatRoom.messages.stream().sorted(Comparator.comparing(MessageSent::getTimestamp))
        .forEach(e -> messages.add(e));
  }

  public void setMessages(ArrayList<MessageSent> messages) {
    this.messages = messages;
  }

  public List<String> getUsers() {
    return users;
  }

  public void setUsers(List<String> users) {
    this.users = users;
  }

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
  }

  public void addMessage(MessageSent m) {
    messages.add(m);
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


  public ArrayList<MessageSent> getMessages() {
    return messages;
  }


}
