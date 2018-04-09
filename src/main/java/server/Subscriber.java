package main.java.server;

import main.java.Opcode;

import java.net.InetAddress;

public class Subscriber {
    private static int op_code = Opcode.MONITOR.getId();

    private InetAddress address;
    private int port;
    private int request_id;
    private int interval;
    private long register_time;


    public Subscriber(InetAddress address, int port, int request_id, int interval) {
        this.address = address;
        this.port = port;
        this.request_id = request_id;
        this.interval = interval;
        this.register_time = System.nanoTime();
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

    public boolean checkValid(){
        // interval in seconds
        return (System.nanoTime()-register_time)/1e9<=interval;
    }
}
