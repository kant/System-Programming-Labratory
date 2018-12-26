package bgu.spl.net.impl.networkProtocol;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.impl.networkProtocol.Task.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class NetworkProtocol implements BidiMessagingProtocol<String> {

    private boolean shouldTerminate = false;
    private Connections<String> connections;
    private Database database;
    private int connectionId;

    public NetworkProtocol(Database database) {
        this.database = database;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId=connectionId;
        this.connections=connections;
    }

    @Override
    public void process(String msg) {
        System.out.println("message received: " + msg);
        String replay = (String)parseMessage(msg);
        System.out.println("sending replay: " + replay);
        connections.send(connectionId,replay);
        //ToDo: change it after creating logout task class
        if(replay == "ACK 3")
            this.connections.disconnect(this.connectionId);

    }

    private Object parseMessage(String msg) {
        Object ans = null;
        byte[] bytes = msg.substring(0,2).getBytes(StandardCharsets.UTF_8);
        short opCode = bytesToShort(bytes);
        String []tokens = msg.substring(2).replace("\n","").split("\0");
        MessageType messageType = MessageType.fromInteger(opCode);
        Task task=null;
        switch (messageType){

            case REGISTER:
                task = new Register(database,connectionId,opCode,new User(tokens[0],tokens[1]));
                ans = task.run();
                break;
            case LOGIN:
                task = new Login(database,connectionId,opCode,new User(tokens[0],tokens[1]));
                ans = task.run();
                ArrayList<Message> messages = database.getUserByConnectionID(connectionId).getMessages();
                messages.stream()
                        .filter(m -> m.getTime() > database.getUserByConnectionID(connectionId).getLogoutTime())
                        .forEach( m-> connections.send(connectionId, new Notification(opCode, "Public", m.getUserName(), m.getMessage()).toString()));
                break;
            case LOGOUT:
                task = new Logout(database,connectionId,opCode);
                ans = task.run();
                break;
            case FOLLOW:
                //04 1 3 ITAY EYAL DDD
                //[04, 1 3 ITAY, EYAL, DDD]
                int sign = tokens[0].charAt(0)-48;
                int size = tokens[0].charAt(2) - 48;
                tokens[0] = tokens[0].substring(4);
                ArrayList<String> list = new ArrayList<>();
                list.addAll(Arrays.asList(tokens));
                task = new Follow_Unfollow(database, connectionId, opCode, sign, size, list);
                ans = task.run();
                break;
            case POST:
                long time = System.currentTimeMillis();
                ArrayList<String> users = new Post(database, connectionId, opCode, tokens[0]).run();
                for(String user: users){
                    int connId = database.getConnetionIdByName(user);
                    if(connId != 0) {
                        Notification reply = new Notification(opCode, "Public", database.getUserByConnectionID(connectionId).getName(), tokens[0]);
                        connections.send(connId, reply.toString());
                    }
                    database.getUserbyName(user).addMessage(new Message(tokens[0], time, database.getUserByConnectionID(connectionId).getName()));
                }

                database.getUserByConnectionID(connectionId).addPost(new Message(tokens[0], time, database.getUserByConnectionID(connectionId).getName()));
                ans = new AckMessage(opCode).toString();
                break;
            case PM:
                break;
            case USERLIST:
                String userList = new UserList(database.getUsers()).toString();
                ans = new AckMessage(opCode, database.getNumOfUsers(), userList).toString();
                break;
            case STAT:
                User user = database.getUserbyName(tokens[0]);
                ans = new AckMessage(opCode, user.getNumOfPost(), user.getNumOfFollowers(), user.getNumOfFollowing()).toString();
                break;
        }

        return ans;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }


    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

}
