package client;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
/**javax是java extension
 * awt & swing 都是GUI组件*/
public class ChatClient extends JFrame implements KeyListener, ActionListener, FocusListener {

    /**客户端的默认数值，包括：
     *1.客户端标题
     *2.主机地址
     *3.远程端口
     *4.昵称*/
    public static final String appName = "Chat Tool";
    public static final String serverText = "127.0.0.1";
    public static final String portText = "3500";
    public static final String nickText = "YourName";

    /**上中下面板*/
    JPanel northPanel, southPanel, centerPanel;

    /**记录初始化文本信息的文本框*/
    JTextField txtHost, txtPort, msgWindow, txtNick;

    /**记录用户信息*/
    JTextArea users;

    /**按键*/
    JButton buttonConnect, buttonSend, buttonClear, buttonExit, buttonUpdateUsers;

    /**滑动面板*/
    JScrollPane sc, usc;

    /**客户端处理程序*/
    ClientKernel ck;

    /**消息记录，嵌入在中央面板中*/
    ClientHistory historyWindow;
    private String lastMsg = "";

    /** Creates a new instance of Class */
    public ChatClient() {
        uiInit();
        txtHost.setText("127.0.0.1");
        txtPort.setText("3500");
    }

    //UI界面初始化
    public void uiInit() {
        //为Frame窗口设置布局为BorderLayout，无需考虑布局
        setLayout(new BorderLayout());

        //North面板，GridLayout初始化布局
        northPanel = new JPanel(new GridLayout(5,2));

        //JLabel(String text)：创建具有指定文本的 JLabel
        northPanel.add(new JLabel("Host address:"));

        //JTextField(String text)：创建一个指定初始化文本信息的文本框
        northPanel.add(txtHost = new JTextField(ChatClient.serverText));
        northPanel.add(new JLabel("Port:"));
        northPanel.add(txtPort = new JTextField(ChatClient.portText));
        northPanel.add(new JLabel("Nick:"));
        northPanel.add(txtNick = new JTextField(ChatClient.nickText));
        //northPanel.add(new JLabel(""));
        //northPanel.add(new JLabel(""));
        //northPanel.add(new JLabel(""));

        //JButton(String text)：创建一个有标签文本、无图标的按钮
        northPanel.add(buttonConnect = new JButton("Log in"));
        northPanel.add(buttonExit = new JButton("Log out"));
        northPanel.add(buttonUpdateUsers = new JButton("Update users"));

        //按键反馈
        buttonConnect.addActionListener(this);
        buttonUpdateUsers.addActionListener(this);
        buttonExit.addActionListener(this);

        //键入反馈
        txtHost.addKeyListener(this);
        txtNick.addKeyListener(this);
        txtPort.addKeyListener(this);
        buttonConnect.addKeyListener(this);

        //聚焦反馈
        txtHost.addFocusListener(this);
        txtNick.addFocusListener(this);
        txtPort.addFocusListener(this);

        //将North加入Frame
        this.add(northPanel, BorderLayout.NORTH);

        //South面板，BorderLayout初始化布局
        southPanel = new JPanel();
        southPanel.add(msgWindow = new JTextField(20));
        southPanel.add(buttonSend = new JButton("Send"));
        southPanel.add(buttonClear = new JButton("Clear"));
        buttonSend.addActionListener(this);
        buttonClear.addActionListener(this);
        msgWindow.addKeyListener(this);
        add(southPanel, BorderLayout.SOUTH);

        //Center面板，BorderLayout初始化布局
        centerPanel = new JPanel(new GridLayout(1,2));
        //centerPanel = new JPanel();
        historyWindow = new ClientHistory();
        sc = new JScrollPane(historyWindow);
        sc.setAutoscrolls(true);
        centerPanel.add(sc);
        //centerPanel.add(users = new JTextField("users:"));
        users = new JTextArea("Users:\n");
        users.setEditable(false);
        centerPanel.add(usc = new JScrollPane(users));
        this.add(centerPanel, BorderLayout.CENTER);
    }

    //主函数，使用父类Frame的方法初始化
   public static void main(String args[]) {
        ChatClient client = new ChatClient();
        client.setTitle(client.appName);
        client.setSize(450, 500);
        client.setLocation(100,100);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.setVisible(true);
        client.msgWindow.requestFocus();
    }

    /**通过显示窗口显示消息*/
    public void addMsg(String str) {
        historyWindow.addText(str);
    }

    public void askUsers() { ck.sendMessage("/getUsers");}

    public void askExit() { ck.sendMessage("/exit");System.out.println("exit");}

    public void addUsers(String str) {
        //users.setText("Users:\n");
        users.setText("");
        users.append(str.replace("`",""));
    }
    /**发起连接*/
    private void connect() {
        try {
            //如果该客服端已经连接，则释放当前CLientKernel，并抛出异常
            if(ck!=null){
                addMsg("You have logged in, please log out before new connect");
                //ck.dropMe();
            }else {
                //创建新的CLientKernel，并初始化
                ck = new ClientKernel(txtHost.getText(), Integer.parseInt(txtPort.getText()));
                ck.setNick(txtNick.getText());
                //初始化连接
                if (ck.isConnected()) {
                    ck.addClient(this);
                    addMsg("<font color=\"#00ff00\">connected! Local Port:" + ck.getLocalPort() + "</font>");
                } else {
                    addMsg("<font color=\"#ff0000\">connect failed!</font>");
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    /**发送消息，清空消息框*/
    private void send() {
        String toSend = msgWindow.getText();
        ck.sendMessage(toSend);
        lastMsg = "" + toSend;
        msgWindow.setText("");
    }


    /**键盘摁下触发*/
    public void keyPressed(KeyEvent e) {
    }

    /**键盘松开触发*/
    public void keyReleased(KeyEvent e) {
        if(e.getSource() == msgWindow && e.getKeyCode() == KeyEvent.VK_UP) {
            msgWindow.setText(lastMsg);
        }
    }

    /**键盘键入触发*/
    public void keyTyped(KeyEvent e) {
        if(e.getKeyChar() ==KeyEvent.VK_ENTER) {
            if(e.getSource() == msgWindow) {
                send();
            }
            if(e.getSource() == txtNick) {
                connect(); msgWindow.requestFocus();
            }
            if(e.getSource() == txtHost) {
                txtPort.requestFocus();
            }
            if(e.getSource() == txtPort) {
                txtNick.requestFocus();
            }
        }
    }

    /**鼠标动作触发*/
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==buttonConnect) {
            connect();
        }
        if(e.getSource()==buttonSend) {
            send();
        }
        if(e.getSource()==buttonClear) {
            historyWindow.clear();
        }
        if(e.getSource()==buttonUpdateUsers){
            //users.append("hello\n");
            askUsers();
        }
        if(e.getSource()==buttonExit){
            askExit();
        }
    }

    /**光标聚焦触发*/
    public void focusGained(FocusEvent e) {
//        if(e.getSource()==txtHost && txtHost.getText().equals(ChatClient.serverText)) txtHost.setText("");
//        if(e.getSource()==txtPort && txtPort.getText().equals(ChatClient.portText)) txtPort.setText("");
//        if(e.getSource()==txtNick && txtNick.getText().equals(ChatClient.nickText)) txtNick.setText("");
    }

    /**光标失去聚焦触发*/
    public void focusLost(FocusEvent e) {
       if(e.getSource()==txtPort && txtPort.getText().equals("")) {
           txtPort.setText(ChatClient.portText);
       }
       if(e.getSource()==txtHost && txtHost.getText().equals("")){
           txtHost.setText(ChatClient.serverText);
       }
       if(e.getSource()==txtNick && txtNick.getText().equals("")) {
           txtNick.setText(ChatClient.nickText);
       }
    }

    /**历史消息面板*/
    class ClientHistory extends JEditorPane {
        public ClientHistory() {
            super("text/html;", "" + ChatClient.appName);
            setEditable(false);
            setAutoscrolls(true);
//            setContentType("text/html;charset=UTF-8");
//            System.out.println(System.getProperty("file.encoding"));
        }
        public void addText(String str) {
            String html = getText();
            int end = html.lastIndexOf("</body>");
            String startStr = html.substring(0, end);
            String endStr = html.substring(end, html.length());
            String newHtml = startStr + "<br>" + str + endStr;
            setText(newHtml);
            setSelectionStart(newHtml.length()-1);
            setSelectionEnd(newHtml.length());

         }
        public void clear() {
            setText("");
        }
    }
}

