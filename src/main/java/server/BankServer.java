package main.java.server;

import main.java.Bank.Account;
import main.java.Bank.BankInterface;
import main.java.Handler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class BankServer extends Thread implements BankInterface {
    private static BankServer instance;
    private DatagramSocket socket;
    private static Handler handler = new Handler();
    private boolean running;
    private byte[] buf = new byte[256];
    private static HashMap<Integer, Object> responseStore;

    public BankServer() {
        try {
            socket = new DatagramSocket(4445);
        }
        catch (Exception e) {
            // Do something
        };
    }

    public static BankServer getInstance() {
        if(instance == null) {
            instance = new BankServer();
        }
        return instance;
    }

    public static void main(String args[]) {

        try {
            BankServer obj = BankServer.getInstance();
//            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
//            registry.bind("Hello", stub);

            System.err.println("ServerTest ready");

            obj.run();
        } catch (Exception e) {
            System.err.println("ServerTest exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void run()  {
        running = true;

        while (running) {
            // Receive packet
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
            }
            catch (Exception e) {
                // Do something
            };

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            Object received
                    = new String(packet.getData(), 0, packet.getLength());

            Object requestPayload = decode(packet);
            int requestId = packet.getRequestId();

            // Check if the request has been received before, return with stored response
            if (responseStore.containsKey(requestId)) {
                Object response = responseStore.get(requestId);
            }

            else {
                Object responsePayload = handle(packet.getOperation(), packet.getArguments());
                Object response = encode(responsePayload);

                responseStore.put(packet.getRequestId(), response);
            }

            if (received.equals("end")) {
                running = false;
                continue;
            }

            try {
                socket.send(packet);
            }
            catch (Exception e) {
                // Do something
            };
        }
        socket.close();
    }

    public Object[] createAccount(Account account) {
        return Handler.CreateAccountHandler.handle(account);
    };

    public Object[] deleteAccount(Account account) {
        return Handler.DeleteAccountHandler.handle(account);
    };

    public Object[] updateAccount(Account account, boolean draw, float amount) {
        return Handler.UpdateAccountHandler.handle(account, draw, amount);
    };

    public Object[] monitor(int interval, InetAddress clientAddress, int clientPort) {
        return Handler.MonitorHandler.handle(interval, clientAddress, clientPort);
    };

    public Object[] transfer(Account sender, Account receiver, float amount) {
        return Handler.TransferHandler.handle(sender, receiver, amount);
    };

    public Object[] checkBalance(Account account) {
        return Handler.CheckBalanceHandler.handle(account);
    };

    public Object[] statistic() {
        return Handler.StatisticHandler.handle();
    };

    private Object handle(String operation, Object[] arguments) {
        Object response = null;
        switch (operation) {
            case "CREATE_ACCOUNT":
                response = createAccount(arguments);
            case "DELETE_ACCOUNT":
                response = deleteAccount(arguments);
            case "UPDATE_ACCOUNT":
                response = updateAccount(arguments);
            case "MONITOR":
                response = monitor(arguments);
            case "TRANSFER":
                response = transfer(arguments);
            case "STATISTIC":
                response = statistic(arguments);
            default:
                // raise error?
        }

        return response;
    }
}