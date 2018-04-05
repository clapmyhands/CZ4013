package main.java.server;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class BankServer implements BankInterface {
    private static BankServer instance;
    private DatagramSocket socket;
    private static Handler handler = new Handler();
    private byte[] buf = new byte[512];
    private ArrayList<Subscriber> subs;
    private String update_msg;

    private static final int DEFAULT_PORT = 4445;


    public BankServer() {
        subs = new ArrayList<>();
    }

    public static BankServer getInstance() {
        if(instance == null) {
            instance = new BankServer();
        }
        return instance;
    }

    public static void main(String args[]) {
        int port;
        try{
            port = Integer.parseInt(args[0]);
        } catch (Exception e){
            port = DEFAULT_PORT;
        }

        try {
            BankServer obj = BankServer.getInstance();
            BankInterfaceSkeleton skeleton = new BankInterfaceSkeleton(
                    DEFAULT_PORT, 1);
            System.err.println("BankServer running...");
            skeleton.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object[] createAccount(Account account) {
        Object[] obj = Handler.CreateAccountHandler.handle(account);
        if(!(obj[0] instanceof Exception)){
            Account acc = (Account) obj[0];
            update_msg = String.format("Created account with acc. number: %d", acc.getAccountNumber());
        } else{
            update_msg = "";
        }
        return obj;
    }

    public Object[] deleteAccount(Account account) {
        Object[] obj =  Handler.DeleteAccountHandler.handle(account);
        if(!(obj[0] instanceof Exception)){
            update_msg = String.format("Deleted account with acc. number: %d", account.getAccountNumber());
        } else{
            update_msg = "";
        }
        return obj;
    }

    public Object[] updateAccount(Account account, boolean draw, float amount) {
        Object[] obj =  Handler.UpdateAccountHandler.handle(account, draw, amount);
        if(!(obj[0] instanceof Exception)){
            update_msg = String.format("%s %f %s from acc. number: %d",
                    draw? "Drawn":"Deposit",
                    amount,
                    account.getCurrency().toString(),
                    account.getAccountNumber());
        } else{
            update_msg = "";
        }
        return obj;
    }

    public Object[] transferMoney(Account sender, Account receiver, float amount) {
        Object[] obj = Handler.TransferHandler.handle(sender, receiver, amount);
        if(!(obj[0] instanceof Exception)){
            update_msg = String.format("Transferred %d from acc.number %d to %d",
                    amount,
                    sender.getAccountNumber(),
                    receiver.getAccountNumber());
        } else{
            update_msg = "";
        }
        return obj;
    }

    @Override
    public Object[] monitor(int interval) {
        return new Object[0];
    }

    public Object[] monitor(int interval, InetAddress clientAddress, int clientPort, int request_id) {
        Subscriber new_sub = new Subscriber(clientAddress, clientPort, request_id, interval);
        subs.add(new_sub);
        return new Object[]{"Client subscribed..."};
    }

    public Object[] getSubscriber(){
        for(Subscriber sub: subs){
            if(!sub.checkValid()){
                subs.remove(sub);
            }
        }
        return subs.toArray();
    }

    public String getUpdateMessage() {
        return this.update_msg;
    }

    public Object[] checkAccountBalance(Account account) {
        return Handler.CheckBalanceHandler.handle(account);
    }

}