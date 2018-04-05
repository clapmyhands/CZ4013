package main.java.client;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;
import main.java.Message;
import main.java.Opcode;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

public class BankInterfaceProxy implements BankInterface{
    byte[] receivedData;
    DatagramSocket udpConnection;
    InetAddress server_addr;
    int server_port;
    private static int request_id;

    public BankInterfaceProxy(int port) {
        try{
            udpConnection = new DatagramSocket();
            server_port = port;
            request_id = 0;
        } catch (SocketException e){
            e.printStackTrace();
        }
    }

    public void setServerAddress(InetAddress addr) {
        this.server_addr = addr;
    }

    private void send_message(InetAddress address, int port, byte[] msg) {
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
        int retry = 0;
        while(retry<3){
            try {
                udpConnection.send(packet);
            } catch (SocketTimeoutException ste) {
                retry++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Message receive_reply(){
        byte[] reply = new byte[512];
        DatagramPacket packet = new DatagramPacket(reply, reply.length);
        try{
            udpConnection.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] msg = packet.getData();
        Message message = Message.unmarshall(msg);
        return message;
    }

    public Message createRequestMessage(){
        return new Message(true, request_id++, 0, 0, null);
    }

    // kur fill from here - call your own function and if need to throw exception then use return instead
    // like return Exception. remember to wrap everything as Object[]
    // as it will get marshalled and send back to client
    @Override
    public Object[] createAccount(Account account){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.CREATE.getId());
        message.setContent(new Object[]{account});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        Message reply = receive_reply();
        assert Message.checkEqualHeader(message, reply);

        return reply.getContent();
    }

    @Override
    public Object[] deleteAccount(Account account){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.DELETE.getId());
        message.setContent(new Object[]{account});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        Message reply = receive_reply();
        assert Message.checkEqualHeader(message, reply);

        return reply.getContent();
    }

    @Override
    public Object[] updateAccount(Account account, boolean draw, float amount){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.UPDATE.getId());
        message.setContent(new Object[]{account, draw, amount});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        Message reply = receive_reply();
        assert Message.checkEqualHeader(message, reply);

        return reply.getContent();
    }

    @Override
    public Object[] transferMoney(Account from_acc, Account to_acc, float amount){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.TRANSFER.getId());
        message.setContent(new Object[]{from_acc, to_acc, amount});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        Message reply = receive_reply();
        assert Message.checkEqualHeader(message, reply);

        return reply.getContent();
    }

    // idk what to do with this one since you need address and port but the interface
    // shouldnt really need both address and port
    @Override
    public Object[] monitor(int interval){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.MONITOR.getId());
        message.setContent(new Object[]{interval});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        long ellapsed = System.nanoTime();
        while((System.nanoTime()-ellapsed)/1000<=interval){
            Message reply = receive_reply();
            String updates = String.valueOf(reply.getContent()[0]);
            System.out.println(updates);
        }

        return new Object[]{};
    }

    @Override
    public Object[] checkAccountBalance(Account account){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.CHECK.getId());
        message.setContent(new Object[]{account});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        Message reply = receive_reply();
        assert Message.checkEqualHeader(message, reply);

        return reply.getContent();
    }
}
