package main.java.client;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;
import main.java.Message;
import main.java.Opcode;

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
            udpConnection.setSoTimeout(5000);
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
        int retry = 0;
        while(retry<3){
            try {
                DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);
                udpConnection.send(packet);
                break;
            } catch (SocketTimeoutException ste) {
                retry++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Message receive_reply(){
        byte[] reply = new byte[1024];
        DatagramPacket packet = new DatagramPacket(reply, reply.length);
        for(;;){
            try{
                udpConnection.receive(packet);
                break;
            } catch(SocketTimeoutException e){
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] msg = packet.getData();
        Message message = Message.unmarshall(msg);
        return message;
    }

    public Message createRequestMessage(){
        return new Message(true, request_id++, 0, 0, null);
    }

    @Override
    public Object[] createAccount(Account account){
        Message message = new Message(true, request_id++, 0, 0, null);;
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
        Message message = new Message(true, request_id++, 0, 0, null);;
        message.setOp_code(Opcode.UPDATE.getId());
        message.setContent(new Object[]{account, draw? 1:2, amount});
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

    @Override
    public void monitor(int interval){
        Message message = createRequestMessage();
        message.setOp_code(Opcode.MONITOR.getId());
        message.setContent(new Object[]{interval});
        byte[] content = Message.marshall(message);

        send_message(server_addr, server_port, content);
        long ellapsed = System.nanoTime();
        while((System.nanoTime()-ellapsed)/1e9<=interval){
            Message reply = receive_reply();
            String updates = String.valueOf(reply.getContent()[0]);
            System.out.println(updates);
        }
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
