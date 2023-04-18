package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class MessageSent implements Serializable {

  private final Long timestamp;

  private final String sentBy;

  private final String sendTo;

  private final String data;

  public MessageSent(Long timestamp, String sentBy, String sendTo, String data) {
    this.timestamp = timestamp;
    this.sentBy = sentBy;
    this.sendTo = sendTo;
    this.data = data;
  }

  public String toString() {
    return timestamp + " " + sentBy + ": " + data;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public String getSentBy() {
    return sentBy;
  }

  public String getSendTo() {
    return sendTo;
  }

  public String getData() {
    return data;
  }

}