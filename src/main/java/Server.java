package main.java;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Server extends Thread implements BankInterface{

    private DatagramSocket socket;
    private static Handler handler = new Handler();
    private boolean running;
    private byte[] buf = new byte[256];
    private static HashMap<Integer, Object> responseStore;

    public Server() {
        try {
            socket = new DatagramSocket(4445);
        }
        catch (Exception e) {
            // Do something
        };
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

    public Object createAccount(Object[] arguments) {
        return Handler.CreateAccountHandler.handle(arguments);
    };

    public Object deleteAccount(Object[] arguments) {
        return Handler.DeleteAccountHandler.handle(arguments);
    };

    public Object updateAccount(Object[] arguments) {
        return Handler.UpdateAccountHandler.handle(arguments);
    };

    public Object monitor(Object[] arguments) {
        return Handler.MonitorHandler.handle(arguments);
    };

    public Object transfer(Object[] arguments) {
        return Handler.TransferHandler.handle(arguments);
    };

    public Object statistic(Object[] arguments) {
        return Handler.StatisticHandler.handle(arguments);
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