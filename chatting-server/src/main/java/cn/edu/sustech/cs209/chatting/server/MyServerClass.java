package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.MessageSent;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MyServerClass {

    ArrayList<String> usersList;
    ArrayList<MessageSent> messageList;
    HashMap<String, Socket> hashMap;
    // 监听指定的端口
    ServerSocket server;
    int port;

    public MyServerClass(int port,ServerSocket server) {
        this.port = port;
        this.server=server;
        usersList = new ArrayList<>();
        usersList.add("TheBang");
        messageList = new ArrayList<>();
    }

    public void getUsers(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(usersList);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
