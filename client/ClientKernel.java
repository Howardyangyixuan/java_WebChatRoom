package client;
import java.io.*;
import java.net.*;
import java.util.*;
//客户端处理程序
public class ClientKernel {
    /**参数*/
    public static final char MSGENDCHAR = 0xff;
    public static final char EXIT = 0xFE;
    public static final char NICK = 0xFD;
    public static final char COMMAND = 0xFD;
    public static final char USER = '`';

    /**昵称：格式为 输入的昵称+端口号*/
    public String nick;

    /**存储 连接的服务器 & 端口号 & 套接字 & 发送 & 接受*/
    private String serverAd;
    private int port;
    private Socket sock;
    private ClientMsgSender cms;
    private ClientMsgListener cml;

    /**客户端状态*/
    private boolean isConnected = false;
    private boolean dropMe = false;
    public boolean printMsg = true;

    /**客户端连接列表*/
    private LinkedList clients;

    /** Creates a new instance of ClientKernel */
    /**根据 服务期地址 和 端口号 初始化
     * 建立连接（初始化socket），并初始化*/
    public ClientKernel(String server, int port) {
        this.port = port;
        nick = "" + port;
        serverAd = server;
        clients = new LinkedList();
        connect();
        if(isConnected) {
            cms = new ClientMsgSender(this, sock);
            cml = new ClientMsgListener(this, sock);
        }
    }

    /**通过 初始化套接字 实现连接 更改客户端状态*/
    public void connect() {
        try {
            sock = new Socket(serverAd, port);
            isConnected = true;
            //UpdateUsers();
        } catch(IOException ioe ) {
            ioe.printStackTrace();
        }
    }

    /**获取端口号，连接的服务器端口号*/
    public int getPort() {
        return port;
    }

    /** 设置昵称 */
    public boolean setNick(String nick) {
        sendMessage("" + ClientKernel.COMMAND + "nick " + nick);
        return true;
    }

    /** 获取本地端口，即套接字的端口 （发送接受使用的端口） */
    public int getLocalPort() {
        return sock.getLocalPort();
    }

    /**  */
    public void dropMe() {
        System.out.println("Drop ME!!!");
        cms.drop();
        cml.drop();
        dropMe = true;
        while(cml.hasStoped() && cms.hasStoped()) pause(5);
    }

    /** 未dropMe时，正常发送消息，'/'开头为指令执行其他操作，开头其他为常规消息 */
    public void sendMessage(String str) {
        if(!dropMe) {
            if(str.charAt(0) == '/')
                cms.addMessage("" + ClientKernel.COMMAND + str.substring(1) );
            else cms.addMessage(str);
        }
    }

    /** 将客户加入客户列表 */
    public void addClient(ChatClient c) {
        clients.add(c);
        //UpdateUsers();
    }

    /** 将客户移出客户列表 */
    public void removeClient(ChatClient c) {
        clients.remove(c);
    }

    /** 暂停 */
    public void pause(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {}
    }

    /** 共享资源，轮流打印
     *  将消息打印到每个已连接的客户端
     *  ！！！注意这里是用的是每个client的方法，在ChatClient中，center栏中的historywindow！！！*/
    public synchronized void storeMsg(String str) {
        Object[] client = clients.toArray();
        for(int i=0;i<client.length;i++) {
            ((ChatClient) (client[i])).addMsg(str);
            System.out.println("has here");
            if(str.equals("Server: You are being disconected!")) {
                ((ChatClient) (client[i])).ck = null;
                System.out.println("has null");
            }
        }
    }

    /**打印用户*/
    public synchronized void storeUsers(String str) {
        Object[] client = clients.toArray();
        for(int i=0;i<client.length;i++) {
            ((ChatClient) (client[i])).addUsers(str);
        }
    }

    /**打印用户*/
    public synchronized void UpdateUsers() {
        Object[] client = clients.toArray();
        for(int i=0;i<client.length;i++) {
            ((ChatClient) (client[i])).askUsers();
        }
    }

    /** 判断是否连接 */
    public boolean isConnected() {
        return isConnected;
    }

    /** 主函数，创建一个客户端内核 */
    public static void main(String args[]) {
        new ClientKernel("localhost", 1984);
    }
}
/*********************************************************************************************/
/*********************************************************************************************/
/*********************************************************************************************/
/** 客户端发送线程 */
class ClientMsgSender extends Thread {
    /**使用的套接字 和 客户端内核*/
    private Socket s;
    private ClientKernel ck;

    /**消息链表*/
    private LinkedList msgList;

    /**线程状态*/
    private boolean running = true;
    private boolean hasStoped = false;

    /**初始化，同时启动线程*/
    public ClientMsgSender(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        msgList = new LinkedList();
        start();
    }

    /**共享资源，轮流加入
     * 单线程运行，加入消息*/
    public synchronized void addMessage(String msg) {
        msgList.addLast(msg);
    }

    /**停止线程，更改运行状态为停止*/
    public void drop() {
        running = false;
    }

    /**判断线程是否停止*/
    public boolean hasStoped() {
        return hasStoped;
    }

    /**线程运行时的操作*/
    public void run() {
        try {
//            //获取输出流，DataOutputStream数据输出流允许应用程序以与机器无关方式将Java基本数据类型写到底层输出流
//            DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
            PrintWriter dataOut = new PrintWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"),true);

            //按队列方式，将消息链表里每个内容输出到输出流，写完后写入终结符号，重复直至队空，在写入退出符号，并关闭输出流
            //无论如何，最后状态为结束
            while(running) {
                while(msgList.size()>0) {
                    String msg = ((String)(msgList.removeFirst()));
                    dataOut.println(msg+ClientKernel.MSGENDCHAR);
//                    char[] data = msg.toCharArray();
//                    for(int i=0;i<data.length;i++) {
//                        dataOut.write((int)data[i]);
//                    }
//                    dataOut.write(ClientKernel.MSGENDCHAR);
                }
                sleep(10);
            }
            //dataOut.write(ClientKernel.EXIT);
            //dataOut.close();
            //stop();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
            System.out.println("msgsender stop");
        }
    }
}

/*********************************************************************************************/
/*********************************************************************************************/
/*********************************************************************************************/
/** 接收线程 */
class ClientMsgListener extends Thread{
    /**使用的套接字 和 客户端内核*/
    private ClientKernel ck;
    private Socket s;

    /**线程状态*/
    private boolean running = true;
    private boolean hasStoped = false;

    /**初始化，同时启动线程*/
    public ClientMsgListener(ClientKernel ck, Socket s) {
        this.ck = ck;
        this.s  = s;
        start();
    }

    /**停止线程，更改运行状态为停止*/
    public void drop() {
        running = false;
    }

    /**判断线程是否停止*/
    public boolean hasStoped() {
        return hasStoped;
    }

    /**线程运行时的操作*/
    public void run() {
        try {
//                //获取输入流，BufferedInputStream 为输入流提供“缓冲功能”
//                BufferedInputStream buffIn = new BufferedInputStream(s.getInputStream());
//
//                //DataInputStream数据输入流允许应用程序以与机器无关方式从底层输入流中读取基本 Java 数据类型
//                DataInputStream dataIn = new DataInputStream(buffIn);
            BufferedReader dataIn = new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));

                //通过strBuff暂存读入数据，调用客户端内核程序，将消息写入每一个客户端，实现广播
                //无论如何，最后状态为结束
                while(running) {
                    //StringBuffer strBuff = new StringBuffer();
                    String strBuff = dataIn.readLine();
//                    if(strBuff.length()>0){
//                        strBuff = strBuff.substring(0,strBuff.length()-1);
//                    }
//                    int c;
//                    while( (c=dataIn.read()) != ClientKernel.MSGENDCHAR) {
//                        strBuff.append((char)c);
//                    }
                    if(strBuff.length()>0 && strBuff.charAt(0)==ClientKernel.USER) {
                        //ck.storeUsers("" + strBuff.toString());
                        ck.storeUsers("" + strBuff);
                    }else {
                        //ck.storeMsg("" + strBuff.toString());
                        ck.storeMsg("" + strBuff);
                    }
                }
                //dataIn.close();
                //buffIn.close();
                stop();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            hasStoped = true;
            System.out.println("msgreceiver stop");
        }
    }
}
