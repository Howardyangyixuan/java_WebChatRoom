package server;
public interface CommandParser {
    
    public void setDataSource(DataSource ds);
    public void runCommand(ConnectedClient cc, String str);
}
