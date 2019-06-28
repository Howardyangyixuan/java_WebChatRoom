package server;
import java.net.*;
import java.io.*;
public class MainServer extends Thread {
    /**参数，包括
     * 1.软件错误，失去连接
     * 2.无套接字
     * 3.地址占用
     * 4.终结字符*/
    public static final String DISCONNECTED = "Software caused connection abort";
    public static final String DISCONNECTED_CLIENT = "Socket closed";
    public static final String PORT_USED_ERROR = "Address already in use: JVM_Bind";
    public static final char MSGENDCHAR = 0xff;

    /**，连接数*/
    public static long uptime = 0; 
    public static long connects = 0;

    /**默认端口 & 最大连接数*/
    int port = 1984;
    int clients = 8;

    /**端口状态*/
    private boolean newPort = true;

    /**服务器套接字，客户端套接字*/
    private ServerSocket sSock;
    private Socket sock;
    public ConnectionKeeper ck;
    public static DataSource ds;
    public static CommandParser cp;

    /***/
    public MainServer(int port) {
        this.port = port;
        ck = new ConnectionKeeper(MainServer.cp);
        MainServer.uptime = System.currentTimeMillis();
        start();
    }

    /**服务器线程运行*/
    public void run() {
        while(true) {
            try {
                //创建服务器套接字
                sSock = new ServerSocket(port);
                //更改端口时打印
                if(newPort) {
                    System.out.println("Server Listening at port: " + sSock.getLocalPort());
                    newPort = false;
                }
                //用本地套接字暂存，加入ConnectionKeeper后，关闭
                sock = sSock.accept();
                ck.add(sock);
                sSock.close();
            } catch(Exception e) {
                //如果端口错误，重新连接端口，端口号加一，端口状态置为新，否则报错退出
                String message = e.getMessage();
                if(message.startsWith(MainServer.PORT_USED_ERROR)) {
                    System.out.println("Port " + port + " is already used, Attempting to use " + (port+=1));
                    newPort = true;
                } else{
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }

    /**休眠函数*/
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch(Exception e) {}
    }
    /**主函数*/
    public static void main(String arg[]) {
        //初始化
        int port = 0;
        MainServer ms;
        MainServer.ds = new FileDataSource();
        MainServer.cp = new BroadcastCommandParser();
        MainServer.cp.setDataSource(MainServer.ds);
        //默认连接3500
        if(arg.length!=1) {
            ms = new MainServer(3500);
            System.out.println("Usage: java jchat.server.MainServer <port>\nAttempting to use default port 3500");
        } else {
            try {
                //使用传入的端口
                port = Integer.parseInt(arg[0]);
            } catch(NumberFormatException nfe) {
                System.out.println("Attempting to use default port 3500");
                port = 3500;
            } finally {
                ms = new MainServer(port);
            }
        }
    }
}
