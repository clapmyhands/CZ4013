package main.java.server;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;
import main.java.Message;
import main.java.Opcode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class BankInterfaceSkeleton implements BankInterface, Runnable{
    private BankServer bankServer;
    byte[] receivedData;
    DatagramSocket udpConnection;

    public BankInterfaceSkeleton(int port) {
        try{
            udpConnection = new DatagramSocket(port);
            bankServer = BankServer.getInstance();
        } catch (SocketException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receivedData = new byte[512];
        for(;;){
            DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
            try {
                udpConnection.receive(packet);
                decodePacketHandler(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void decodePacketHandler(DatagramPacket packet) {
        byte[] msg = packet.getData();
        InetAddress client_addr = packet.getAddress();
        int client_port = packet.getPort();

        Message request = Message.unmarshall(msg);
        Opcode operation = Opcode.fromCode(request.getOp_code());
        Object[] args = request.getContent();

        Message reply = new Message(false, request.getRequest_id(),
                request.getObject_ref(), request.getOp_code(), null);
        Object[] reply_args = new Object[]{};
        switch(operation){
            case CREATE:
                reply_args = createAccount((Account) args[0]);
                break;
            case DELETE:
                reply_args = deleteAccount((Account) args[0]);
                break;
            case UPDATE:
                reply_args = updateAccount((Account) args[0], (int)args[1]>=1, (float)args[2]);
                break;
            case TRANSFER:
                reply_args = transferMoney((Account)args[0],(Account)args[1],(float)args[2]);
                break;
            case MONITOR:
                // monitor might need client address and client port
                reply_args = monitor((int)args[0], client_addr, client_port);
                break;
            case CHECK:
                reply_args = checkAccountBalance((Account) args[0]);
                break;
            default:
                break;
        }
        reply.setContent(reply_args);
        send_reply(client_addr, client_port, Message.marshall(reply));
    }

    private void send_reply(InetAddress address, int port, byte[] msg) {
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
        try{
            udpConnection.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // kur fill from here - call your own function and if need to throw exception then use return instead
    // like return Exception. remember to wrap everything as Object[]
    // as it will get marshalled and send back to client
    @Override
    public Object[] createAccount(Account account)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Object[] deleteAccount(Account account)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Object[] updateAccount(Account account, boolean draw, float amount)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Object[] transferMoney(Account from_acc, Account to_acc, float amount)
            throws IllegalArgumentException {
        return new Object[0];
    }

    // idk what to do with this one since you need address and port but the interface
    // shouldnt really need both address and port
    @Override
    public Object[] monitor(int interval)
            throws IllegalArgumentException {
        return null;
    }

    public Object[] monitor(int interval, InetAddress address, int port)
            throws IllegalArgumentException {
        return null;
    }

    @Override
    public Object[] checkAccountBalance(Account account)
            throws IllegalArgumentException {
        return null;
    }
}
