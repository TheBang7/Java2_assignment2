package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

  public static HashSet<String> UserList = new HashSet<>();
  public static HashMap<String, ObjectOutputStream> UserToWriter = new HashMap<>();
  public static HashSet<ObjectOutputStream> writers = new HashSet<>();
  public static HashMap<String, HashMap<String, ChatRoom>> Messages = new HashMap<>();

  //线程池对象
  public static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 32, 1,
      TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));

  public static void main(String[] args) {
    try (ServerSocket listener = new ServerSocket(1234)) {
      while (true) {
        poolExecutor.execute(new Handler(listener.accept()));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
