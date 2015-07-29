import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ChatServer{

    private static ChatServer chatServer = new ChatServer();
    private ServerSocket server;
    private List clients = new ArrayList();

    private ChatServer() {	
    }

    public void listen(){
	try{
	    server = new ServerSocket(18080);
	    System.out.println("チャットサーバをポート18080で起動しました.");
	    while(true){
		Socket socket = server.accept(); 
		ChatClientHandler handler = new ChatClientHandler(socket, clients);
		clients.add(handler);
		System.out.println("クライアントが接続してきました.");
		handler.start();
	    }
	} catch(IOException e){
	    e.printStackTrace();
	}
    }
    
    public static void main(String[] args){
	chatServer.listen();
    }

}
