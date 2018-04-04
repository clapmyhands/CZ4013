package main.java.server;

import main.java.Opcode;

import java.net.InetAddress;

public class Subscriber {
    private static int op_code = Opcode.MONITOR.getId();

    private InetAddress address;
    private int port;
    private int request_id;


    public Subscriber(InetAddress address, int port, int request_id) {
        this.address = address;
        this.port = port;
        this.request_id = request_id;
    }

    public static int getOp_code() {
        return op_code;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getRequest_id() {
        return request_id;
    }
}
