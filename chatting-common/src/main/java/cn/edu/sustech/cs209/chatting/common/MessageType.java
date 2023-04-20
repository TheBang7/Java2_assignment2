package cn.edu.sustech.cs209.chatting.common;

public enum MessageType {
  C_S_GetUserList,//客户端发向服务端，用来获得当前的所有用户列表
  C_S_register,//客户端发向服务端，注册请求

  S_C_Register,//服务端发向客户端，注册成功
  C_S_login,//客户端发向服务端，登录请求
  S_C_Login,//服务端发向客户端，登录成功
  C_S_addNewUser,//客户端发向服务端，增加一个用户，需要更改username
  S_C_addNewUser,//服务端发向客户端，以维护每个客户端的在线列表
  C_S_sendMessageToPrivate,//发送信息给私人聊天
  C_S_sendMessageToGroup,//发送信息给群聊
  S_C_UserSent,//服务端发向客户端，内部包含userList
  C_S_requirePrivateChat,//客户端发向服务端，请求对应的聊天室
  S_C_sendPrivateChat,//服务端发向客户端，建立对应的聊天室
  S_C_updateMessage,//更新所有用户的信息列表
  C_S_requireGroupChat,//用于请求群聊
  S_C_sendGroupChat,//用于生成新的群聊
  S_C_updateGroupMessage,//用于发送群聊信息
  C_S_quit,//用户退出服务器
  S_C_quitSuccessfully,//用户成功退出服务器
  S_C_ReceiveFile,//客户端接收文件
  C_S_SendFileToPrivate,//客户端向私人发送文件
  S_C_ServerShutDown,//服务端关闭
  S_C_removeUser,//某用户退出
  C_S_quitFromGroup,
  S_C_removeFromGroup,

}
