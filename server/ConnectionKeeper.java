package server;
import java.io.*;
import java.net.*;
import java.util.*;
/**保存连接信息，链表*/
public class ConnectionKeeper {
    /**已连接客户端链表*/
    private LinkedList clientList;
    private CommandParser cp;
    public ConnectionKeeper(CommandParser parser) {
        this.cp = parser;
        clientList = new LinkedList();
    }
    /**尾插*/
    public void add(Socket s) {
        MainServer.connects++;
        clientList.addLast(new ConnectedClient(s, this));
//        String a="";
//        for(int i =0;i<clientList.size();i++) {
//            ConnectedClient receiver = (ConnectedClient)(clientList.get(i));
//            a += receiver.nick+"\n";
//            }
//        broadcast("`"+a);
    }
    /**尾删*/
    public void remove(ConnectedClient cc) {
        clientList.remove(cc);
        cc = null;
    }
    /**获取用户列表*/
    public LinkedList users() {
        return clientList;
    }
    /**运行命令*/
    public void runCommand(ConnectedClient cc, String str) {
        cp.runCommand(cc, str);
    }

    /**向指定用户发送消息*/
    public void sendTo(ConnectedClient sender, String user, String msg) {
        boolean found = false;
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient receiver = (ConnectedClient)(clientList.get(i));
            if(user.equalsIgnoreCase(receiver.nick)) {
                receiver.sendMessage(msg);
                found = true;
                i = clientList.size()+5; // Stop the loop.
            }
        }
        if(!found) {
            sender.sendMessage("Unable to find user " + user);
        }
    }

    /**广播*/
    public void broadcast(String msg) {
        for(int i =0;i<clientList.size();i++) {
            ConnectedClient cc = (ConnectedClient)(clientList.get(i));
            cc.sendMessage(msg);
        }
    }
}
