package cn.edu.sustech.cs209.chatting.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static void main(String[] args) throws Exception {

    try {
      int port = 55533;
      ServerSocket server = new ServerSocket(port);
      MyServerClass MyServer = new MyServerClass(port, server);
      // server将一直等待连接的到来
      System.out.println("server将一直等待连接的到来");
      //如果使用多线程，那就需要线程池，防止并发过高时创建过多线程耗尽资源
      ExecutorService threadPool = Executors.newFixedThreadPool(100);
      new Thread(() -> {
        while (true) {
          try {
            Socket socket = server.accept();
            Runnable runnable = () -> {
              try {
                ObjectInputStream in = new ObjectInputStream(
                    socket.getInputStream());
                String s = (String) in.readObject();
                if (s.equals("GetUserList:")) {
                  MyServer.getUsers(socket);
                  try {
                    String s1 = (String) in.readObject();
                    if (s1.equals("AddNewUser")) {
                      String s2 = (String) in.readObject();
                      MyServer.usersList.add(s2);
                    }

                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }

              } catch (Exception e) {
                e.printStackTrace();
              }
            };
            threadPool.submit(runnable);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }).start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
