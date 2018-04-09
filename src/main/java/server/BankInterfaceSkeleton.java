package main.java.server;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;
import main.java.Message;
import main.java.Opcode;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;

public class BankInterfaceSkeleton implements BankInterface, Runnable{
    private BankServer bankServer;
    byte[] receivedData;
    DatagramSocket udpConnection;
    private boolean at_most_once;
    private static HashMap<Integer, Message> responseStore;

    public BankInterfaceSkeleton(int port, int at_most_once_flag) {
        try{
            udpConnection = new DatagramSocket(port);
            udpConnection.setSoTimeout(5000);
            bankServer = BankServer.getInstance();
            responseStore = new HashMap<>();
            at_most_once = at_most_once_flag>=1;
        } catch (SocketException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        receivedData = new byte[1024];
        for(;;){
            try {
                DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
                udpConnection.receive(packet);
                decodePacketHandler(packet);
            } catch (SocketTimeoutException e){
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

        System.out.println(request.toString());

        // check for previous execution and saved reply
        Message reply;
        if(at_most_once){
            reply = responseStore.get(request.getRequest_id());
            if(reply != null){
                send_message(client_addr, client_port, Message.marshall(reply));
                return;
            }
        }

        reply = new Message(false, request.getRequest_id(),
                request.getObject_ref(), request.getOp_code(), null);
        Object[] reply_args = invoke_method(operation, args, request.getRequest_id(), packet);
        reply.setContent(reply_args);
        if(reply_args==null){
            return;
        }
        if(at_most_once){
            responseStore.put(request.getRequest_id(), reply); // set saved reply after execution
        }
        System.out.println(reply.toString());
        send_message(client_addr, client_port, Message.marshall(reply));

        String update_msg = bankServer.getUpdateMessage();
        if(update_msg!=""){
            System.out.println("here1");
            publishToSubs(bankServer.getSubscriber(),
                    request.getObject_ref(),
                    operation, new Object[]{update_msg});
        }
    }

    public Object[] invoke_method(Opcode operation, Object[] args, int request_id, DatagramPacket packet){
        Object[] reply_args;
        switch(operation){
            case CREATE:
                reply_args = createAccount((Account) args[0]);
                break;
            case DELETE:
                reply_args = deleteAccount((Account) args[0]);
                break;
            case UPDATE:
                reply_args = updateAccount((Account) args[0], ((int)args[1])==1, (float)args[2]);
                break;
            case TRANSFER:
                reply_args = transferMoney((Account)args[0],(Account)args[1],(float)args[2]);
                break;
            case MONITOR:
                // monitor might need client address and client port
                monitor((int)args[0], packet.getAddress(), packet.getPort(), request_id);
                return null;
            case CHECK:
                reply_args = checkAccountBalance((Account) args[0]);
                break;
            default:
                return null;
        }
        return reply_args;
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

    @Override
    public Object[] createAccount(Account account){
        return bankServer.createAccount(account);
    }

    @Override
    public Object[] deleteAccount(Account account){
        return bankServer.deleteAccount(account);
    }

    @Override
    public Object[] updateAccount(Account account, boolean draw, float amount){
        return bankServer.updateAccount(account, draw, amount);
    }

    @Override
    public Object[] transferMoney(Account from_acc, Account to_acc, float amount){
        return bankServer.transferMoney(from_acc, to_acc, amount);
    }


    @Override
    public void monitor(int interval){
    }

    public void monitor(int interval, InetAddress address, int port, int request_id){
        bankServer.monitor(interval, address, port, request_id);
    }

    public void publishToSubs(List<Subscriber> subs, int object_ref, Opcode opcode, Object[] reply_content){
        Message message = new Message(false, 0, object_ref, opcode.getId(), reply_content);
        for(Subscriber subscriber: subs){
            System.out.println("here2");
            message.setRequest_id(subscriber.getRequest_id());
            message.setContent(reply_content);

            byte[] msg = Message.marshall(message);
            DatagramPacket packet = new DatagramPacket(msg, msg.length,
                    subscriber.getAddress(), subscriber.getPort());
            int retry = 0;
            while(retry<3){
                try {
                    udpConnection.send(packet);
                    break;
                } catch (SocketTimeoutException ste) {
                    retry++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public Object[] checkAccountBalance(Account account){
        return bankServer.checkAccountBalance(account);
    }
}
