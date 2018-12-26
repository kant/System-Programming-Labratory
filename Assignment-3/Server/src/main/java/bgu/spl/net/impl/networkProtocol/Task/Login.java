package bgu.spl.net.impl.networkProtocol.Task;

import bgu.spl.net.impl.networkProtocol.Operation.AckMessage;
import bgu.spl.net.impl.networkProtocol.Operation.ErrorMessage;
import bgu.spl.net.impl.networkProtocol.Operation.LoginMessage;
import bgu.spl.net.impl.networkProtocol.Operation.NetworkMessage;
import bgu.spl.net.impl.networkProtocol.User;
import bgu.spl.net.impl.networkProtocol.Database;

import java.util.concurrent.ConcurrentHashMap;

public class Login extends BaseTask <NetworkMessage>{
    private LoginMessage user;

    public Login(Database database, int connectionId, int opCode, LoginMessage user) {
        super(database, connectionId, opCode);
        this.user = user;
    }

    @Override
    public NetworkMessage run() {
        boolean login=false;
        if (database.isLoggedInByName(user.getUsername()) || database.isLoggedInbyConnId(connectionId)) {
            return new ErrorMessage(opCode);
        }
        User check = database.getUserbyName(user.getUsername());
        if(check != null && check.compareTo(new User(user.getUsername(),user.getPassword())) == 0){
            database.putNewLogin(user.getUsername(),connectionId);
            login = true;
        }
        if(login) return new AckMessage(opCode);
        else return new ErrorMessage(opCode);
    }
}
