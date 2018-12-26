package bgu.spl.net.impl.networkProtocol.Task;

public class AckMessage {

    private int opCode;

    AckMessage(int opCode){
        this.opCode = opCode;
    }

    @Override
    public String toString() {
        return "ACK " + opCode;
    }
}