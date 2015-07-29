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

    public void run() {
	try{
	    open();
	    while(true) {
		String message = receive();
		String[] commands = message.split(" ");

		if(commands[0].equalsIgnoreCase("help")) {
		    send("help, name, whoami, users, bye, post, tell, reject, create, leave, join, groups, members");
		}
		else if(commands[0].equalsIgnoreCase("name")) {
		    setClientName(commands[1]); 
		    send(getClientName());
		}
		else if(commands[0].equalsIgnoreCase("whoami")) {
		    send(getClientName()); 
		}
		else if(commands[0].equalsIgnoreCase("users")) {
		    printUsers(); 
		}
		else if(commands[0].equalsIgnoreCase("bye")) {
		    bye(); 
		    break; 
		}
		else if(commands[0].equalsIgnoreCase("post")) {
		    post(this.clients, commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("tell")) {
		    tell(commands[1], commands[2]);
		}
		else if(commands[0].equalsIgnoreCase("reject")) {
		    if(commands.length == 2) {
			reject(commands[1]);
		    }	
		    if(commands.length == 1) { 
			reject(); 
		    }	
		}
		else if(commands[0].equalsIgnoreCase("create")) {
		    createGroup(commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("leave")) {
		    leaveGroup(commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("join")) {
		    joinGroup(commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("groups")) {
		    printGroups();
		}
		else if(commands[0].equalsIgnoreCase("members")) {
		    printGroupMembers(commands[1]);
		}
	    }
	} catch(IOException e) {
	    e.printStackTrace();
	} finally{
	    close(); 
	}
    }

    public String getClientName() {
	return name;
    }
 
    public void setClientName(String name) throws IOException {

	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(name.equals(handler.getClientName())) { 
		send("この名前は使用できません.");
		return; 
	    }
	}
	
	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    for(int j = 0; j < handler.rejectNames.size(); j++) {
		if(handler.rejectNames.get(j).equals(this.name)) {
		    handler.rejectNames.set(j, name);
		}
	    }
	}
	
	this.name = name; 
    }

    public void printUsers() throws IOException {
	
	List users = new ArrayList();
	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    users.add(handler.getClientName()); 
	}
  
	String returnMessage = toString(users); 
	this.send(returnMessage); 
    }

    public void bye() throws IOException {	
	 send("接続を切断しました."); 

	 for(int i = 0; i < clients.size(); i++) {
	     ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	     if(handler == this) {
		 clients.remove(i); 
	     }
	 }

	 for(int i = 0; i < clients.size(); i++) {
	     ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	     for(int j = 0; j < handler.rejectNames.size(); j++) {
		 if(this.name.equals(handler.rejectNames.get(j))) {
		     handler.rejectNames.remove(j);
		 }		
	     }
	 }
	 
	 leaveAllGroups(); 
     }

    public void leaveAllGroups() throws IOException {

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);	    
	    for(int j = 0; j < group.members.size(); j++) {
		if(this == group.members.get(j)) {
		    group.members.remove(j);
		    if(group.members.size() == 0) {
			groups.remove(i);
		    }
		}
	    }	    
	}	
    }


    public void post(List clients, String message) throws IOException {

	List names = new ArrayList(); 
	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    boolean isRejected = false; 
	    if(handler != this) { 
		for(int j = 0; j < handler.rejectNames.size(); j++) {
		    if(handler.rejectNames.get(j).equals(this.getClientName())) {
			isRejected = true; 
		    }
		}
		if(isRejected == false) {
		    names.add(handler.getClientName());
		    handler.send("[" + this.getClientName() + "] " + message);
		}
	    }
	}
	String returnMessage = toString(names);
	
	if(names.size() == 0) { 
	    this.send("no one receive message"); 
	} else {
	    this.send(returnMessage);
	}
    }

    public void tell(String name, String message) throws IOException {	
	
	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(handler.getClientName().equals(name)) {
		for(int j = 0; j < handler.rejectNames.size(); j++) {
		    if(handler.rejectNames.get(j).equals(this.getClientName())) {
			this.send("no one receive message");
			return;
		    }
		}
		handler.send("[" + this.getClientName() + 
			     "≫" +  handler.getClientName() + "] " + message); 
		this.send(handler.getClientName()); 
		return;		
	    }   
	}

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    if(name.equals(group.getGroupName())) {
		post(group.members, message);
		return;
	    }
	}
	this.send("この名前は存在しません."); 	
    }	

    public void reject(String name) throws IOException {

	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(name.equals(handler.getClientName())) { 
		for(int j = 0; j < rejectNames.size(); j++) {
		    if(rejectNames.get(j).equals(name)) {
			rejectNames.remove(j);
			reject();
			return;
		    }
		}
		rejectNames.add(name);	
		reject();
		return;
	    }
	}
	send("その名前は存在しません.");	
    }

    public void reject() throws IOException {

	String returnMessage = toString(rejectNames); 
	if(returnMessage.equals("")) {
	    returnMessage = "拒否しているユーザはいません.";
	}
	this.send(returnMessage);
    }

    public void createGroup(String groupName) throws IOException {

	for(int i = 0; i < clients.size(); i++) {
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(groupName.equals(handler.getClientName())) {
		send("その名前は使用できません.");
		return;
	    }
	}

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    if(groupName.equals(group.getGroupName())) {
		send("その名前は使用できません.");
		return;
	    }
	}
	
	ChatGroup group = new ChatGroup(this, groupName);
	groups.add(group);
	send("グループを作成しました.");
    }

    public void leaveGroup(String groupName) throws IOException {

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    if(groupName.equals(group.getGroupName())) {
		for(int j = 0; j < group.members.size(); j++) {
		    if(this == group.members.get(j)) {
			group.members.remove(j);
			if(group.members.size() == 0) {
			    groups.remove(i);
			}
			send("脱退しました.");
			return;
		    }
		}
		send("あなたはそのグループのメンバではありません.");
		return;
	    }
	}
	send("そのグループは存在しません.");
    }

    public void joinGroup(String groupName) throws IOException {

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    
	    if(groupName.equals(group.getGroupName())) {
		for(int j = 0; j < group.members.size(); j++) {
		    if(this == group.members.get(j)) {
			send("あなたはすでにメンバーです.");
			return;
		    }
		}
		group.members.add(this); 
		send(groupName + "に参加しました.");
		return;
	    }
	}
	
	// グループの名前が見つからなければここまで到達する
	send("そのグループは存在しません.");
    }

    public void printGroups() throws IOException {
	List names = new ArrayList(); 

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    names.add(group.getGroupName()); 
	}
	
	String returnMessage = toString(names); 
	
	if(returnMessage == "") {
	    returnMessage = "グループは存在しません.";
	}
	this.send(returnMessage); 
    }

    public void printGroupMembers(String groupName) throws IOException {
	
	List names = new ArrayList(); 

	for(int i = 0; i < groups.size(); i++) {
	    ChatGroup group = (ChatGroup)groups.get(i);
	    if(groupName.equals(group.getGroupName())) {
		for(int j = 0; j < group.members.size(); j++) {
		    ChatClientHandler handler = (ChatClientHandler)group.members.get(j);
		    if(handler == group.manager) { 
			names.add(handler.getClientName() + "★");
		    }
		    else{ 
			names.add(handler.getClientName());	
		    }	    
		}
		
		String returnMessage = toString(names); 
		this.send(returnMessage); 
		return;
	    }
	}
	send("そのグループは存在しません.");
    }
    
    public String toString(List list) {
	
	Collections.sort(list);	
	String message = "";

	for(int i = 0; i < list.size(); i++) {
	    message = message + list.get(i);
	    if(i < list.size() - 1) { 
		message = message + ", ";
	    }	    
	}
	return message;
    }
    
    void open() throws IOException {
	InputStream socketIn = socket.getInputStream();
	OutputStream socketOut = socket.getOutputStream();
	in = new BufferedReader(new InputStreamReader(socketIn));
	out = new BufferedWriter(new OutputStreamWriter(socketOut));
    }
    
    String receive() throws IOException {
	String line = in.readLine();
	System.out.print(this.name + ": ");
	System.out.println(line);
	return line;
    }

    void send(String message) throws IOException {
	out.write(message);
	out.write("\r\n");
	out.flush();
    }
  
    void close() {
	if(in != null) {
	    try{
		in.close();
	    } catch(IOException e) {}
	}
	if(out != null) {
	    try{
		out.close();
	    } catch(IOException e) {}
	}
	if(socket != null) {
	    try{
		socket.close();
	    } catch(IOException e) {}
	}
	System.out.println("クライアントが接続を切断しました.");
    }

}
