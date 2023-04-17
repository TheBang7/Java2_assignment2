package cn.edu.sustech.cs209.chatting.common;

public enum MessageType {
  C_S_GetUserList,//客户端发向服务端，用来获得当前的所有用户列表
  C_S_GetUserListToCheck,//客户端发向服务端，以请求一个UserSentToCheck的命令，来检查是否重复
  C_S_addNewUser,//客户端发向服务端，增加一个用户，需要更改username
  S_C_addNewUser,//服务端发向客户端，以维护每个客户端的在线列表
  sendMessage,//发送信息
  S_C_UserSent,//服务端发向客户端，内部包含userList
  S_C_UserSentToCheck;//服务端发向客户端，发送一个userList用于检查是否重复


}
