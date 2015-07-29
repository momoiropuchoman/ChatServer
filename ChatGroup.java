import java.util.List;
import java.util.ArrayList;

class ChatGroup {
    
    List members = new ArrayList(); 
    private String groupName; 
    ChatClientHandler manager; 
    
    ChatGroup(ChatClientHandler manager, String groupName) {
	this.manager = manager; 
	this.groupName = groupName; 
	members.add(manager); 
    }

    public String getGroupName() {
	return groupName;
    }
}
