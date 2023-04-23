package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.ChatRoom;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MessageType;
import cn.edu.sustech.cs209.chatting.common.MyUser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server implements Serializable {

  public transient HashSet<String> CurrentUserNameList = new HashSet<>();
  public HashSet<String> RegUserNameList = new HashSet<>();
  public transient HashSet<MyUser> CurrentUserList = new HashSet<>();
  public HashSet<MyUser> RegUserList = new HashSet<>();
  public HashMap<String, MyUser> nameToUser = new HashMap<>();
  public transient HashMap<String, ObjectOutputStream> UserToWriter = new HashMap<>();
  public transient HashSet<ObjectOutputStream> writers = new HashSet<>();
  public HashMap<String, HashMap<String, ChatRoom>> Messages = new HashMap<>();
  public HashMap<String, HashMap<String, ChatRoom>> UserToGroup = new HashMap<>();


  //线程池对象
  public static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 32, 1,
      TimeUnit.MINUTES, new ArrayBlockingQueue<>(16));

  public static void main(String[] args) throws IOException {
    Server server = loadServer("chatting-server/src/main/resources/save.txt");
    System.out.println(server.RegUserNameList);
    Thread thread = new Thread(() -> {
      Scanner scanner = new Scanner(System.in);
      while (true) {
        String input = scanner.nextLine();
        if ("stop".equals(input)) {
          break;
        }
      }
      try {
        server.shutDown();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      saveServer(server, "chatting-server/src/main/resources/save.txt");
      poolExecutor.shutdownNow();
      System.out.println("Stopping server...");
      System.exit(0); // 正常终止程序
    });
    thread.start();
    poolExecutor.execute(thread);
    try (ServerSocket listener = new ServerSocket(1234)) {

      while (true) {
        poolExecutor.execute(new MyHandler(listener.accept(), server));
      }
    } catch (IOException e) {
      e.printStackTrace();
      saveServer(server, "chatting-server/src/main/resources/save.txt");
    }
  }

  public void shutDown() throws IOException {
    Message message = new Message(MessageType.S_C_ServerShutDown);
    for (ObjectOutputStream o : writers) {
      o.writeObject(message);
      o.flush();
    }
  }

  public static void saveServer(Server server, String fileName) {
    try {
      FileOutputStream fileOut = new FileOutputStream(fileName);
      ObjectOutputStream out = new ObjectOutputStream(fileOut);
      out.writeObject(server);
      out.close();
      fileOut.close();
      System.out.println("Serialized data is saved in " + fileName);
    } catch (IOException i) {
      i.printStackTrace();
    }
  }


  public static Server loadServer(String fileName) {
    Server server = new Server();
    try {
      File file = new File(fileName);
      if (file.length() == 0) {
        return server;
      }
      FileInputStream fileIn = new FileInputStream(fileName);
      ObjectInputStream in = new ObjectInputStream(fileIn);
      server = (Server) in.readObject();
      in.close();
      fileIn.close();
      System.out.println("Serialized data is loaded from " + fileName);
    } catch (ClassNotFoundException c) {
      System.out.println("Server class not found");
      c.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    server.writers = new HashSet<>();
    server.UserToWriter = new HashMap<>();
    server.CurrentUserNameList = new HashSet<>();
    server.CurrentUserList = new HashSet<>();
    return server;
  }

}