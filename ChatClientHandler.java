import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

class ChatClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    private List clients; 
    private String name;
    public List rejectNames = new ArrayList();
    static List groups = new ArrayList(); 
    static int count = 0; 

    ChatClientHandler(Socket socket, List clients) throws IOException {
	count ++;
	this.socket = socket;
	this.clients = clients;
	setClientName("undefined" + count); 
    }

}
