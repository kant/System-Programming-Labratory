package bgu.spl.net.impl.networkProtocol.Task;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.networkProtocol.Database;
import bgu.spl.net.impl.networkProtocol.Message;
import bgu.spl.net.impl.networkProtocol.MessageType;
import bgu.spl.net.impl.networkProtocol.NetworkMessage;
import bgu.spl.net.impl.networkProtocol.ReplayMessage.AckMessage;
import bgu.spl.net.impl.networkProtocol.ReplayMessage.ErrorMessage;
import bgu.spl.net.impl.networkProtocol.ReplayMessage.Notification;
import bgu.spl.net.impl.networkProtocol.ReplayMessage.ReplyMessage;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PostMessage implements Task<Database> {
    private String content;
    private String messageStr;
    int opCode;

    public PostMessage(){
        opCode = MessageType.POST.getOpcode();
    }

    public ReplyMessage run(Database arg, int connectionId){
        return null;
    }

    public ReplyMessage run(Database database, Connections<NetworkMessage> connections, int connectionId){
        ReplyMessage ans;
        ArrayList<String> users = database.postCommand(content, connectionId);
        if(database.isLoggedInbyConnId(connectionId)) {
            long time = System.currentTimeMillis();
            for (String user : users) {
                synchronized (user) {
                    if (database.isLoggedInByName(user)) {
                        int connId = database.getConnetionIdByName(user);
                        Notification reply = new Notification(opCode, "Public", database.getUserByConnectionID(connectionId).getName() + " ", content);
                        connections.send(connId, reply);
                    }
                    database.getUserbyName(user).addMessage(new Message(content, time, database.getUserByConnectionID(connectionId).getName()));
                }
            }
            database.getUserByConnectionID(connectionId).addPost(new Message(content, time, database.getUserByConnectionID(connectionId).getName()));
            ans = new AckMessage(opCode);
        }
        else{
            ans = new ErrorMessage(opCode);
        }
        return ans;
    }

    @Override
    public boolean checkIfMessageIsValid(String msg) {
        if (msg.length() > 2 && Pattern.compile("([\\w].*[\0]){1}$").matcher(msg.substring(2)).find()){
            updateFields(msg);
            return true;
        }
        return false;
    }

    //ToDo: change updateFields here
    @Override
    public void updateFields(String msg) {
        this.messageStr = msg;
        content = messageStr.substring(2,messageStr.length() - 1);
    }

    public String getContent() {
        return content;
    }
}