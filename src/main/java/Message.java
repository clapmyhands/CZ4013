package main.java;

import main.java.Bank.Account;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Message {
    public static final byte ACCOUNT_CODE = 0x01;
    public static final byte INT_CODE = 0x02;
    public static final byte FLOAT_CODE = 0x03;
    public static final byte EXCEPTION_CODE = 0x04;
    public static final byte STRING_CODE = 0x05;

    private boolean is_request; // 1 bit - request = 1 | reply = 0
    private int request_id; // 32 bit
    private int object_ref;
    private int op_code;
    private Object[] content;

    public Message(boolean is_request, int request_id, int object_ref, int opcode, Object[] content){
        this.is_request = is_request;
        this.request_id = request_id;
        this.object_ref = object_ref;
        this.op_code = opcode;
        this.content = content;
    }

    public boolean is_request() {
        return is_request;
    }

    public void set_request(boolean request) {
        this.is_request = request;
    }

    public int getRequest_id() {
        return request_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public int getObject_ref() {
        return object_ref;
    }

    public void setObject_ref(int object_ref) {
        this.object_ref = object_ref;
    }

    public int getOp_code() {
        return op_code;
    }

    public void setOp_code(int op_code) {
        this.op_code = op_code;
    }

    public Object[] getContent(){
        return this.content;
    }

    public void setContent(Object[] content) {
        this.content = content;
    }

    public static byte[] marshall(Message msg){
        int request = requestToInt(msg.getRequest_id(), msg.is_request());

        int op_code = msg.getOp_code();

        ArrayList<byte[]> contents = new ArrayList<>();
        /***************************
         * amount of arguments
         * ******* -> class_code
         * * obj * -> length of obj
         * ******* -> obj bytes
         */
        int content_len = Integer.BYTES; // 1 int to specify amount of args
        for(Object obj: msg.getContent()){
            Class<?> cls = obj.getClass();
            if(cls == Integer.class){
                contents.add(ByteBuffer.allocate(Byte.BYTES+Integer.BYTES)
                        .put(INT_CODE)
                        .putInt((int)obj)
                        .array());
                content_len += Byte.BYTES+Integer.BYTES;
            } else if(cls == Account.class){
                byte[] acc = ((Account)obj).toByteArray();
                contents.add(ByteBuffer.allocate(Byte.BYTES+Integer.BYTES+acc.length)
                        .put(ACCOUNT_CODE)
                        .putInt(acc.length)
                        .put(acc)
                        .array());
                content_len += Byte.BYTES+Integer.BYTES+acc.length;
            } else if(cls == Exception.class){
                byte[] exc = ((Exception)obj).getMessage().getBytes(StandardCharsets.UTF_16);
                contents.add(ByteBuffer.allocate(Byte.BYTES+Integer.BYTES+exc.length)
                        .put(EXCEPTION_CODE)
                        .putInt(exc.length)
                        .put(exc)
                        .array());
                content_len += Byte.BYTES+Integer.BYTES+exc.length;
            } else if(cls == Float.class){
                contents.add(ByteBuffer.allocate(Byte.BYTES+Float.BYTES)
                        .put(FLOAT_CODE)
                        .putFloat((float)obj)
                        .array());
                content_len += Byte.BYTES+Float.BYTES;
            } else if(cls == String.class){
                byte[] str = ((String)obj).getBytes(StandardCharsets.UTF_16);
                contents.add(ByteBuffer.allocate(Byte.BYTES+Integer.BYTES+str.length)
                        .put(STRING_CODE)
                        .putInt(str.length)
                        .put(str)
                        .array());
                content_len += Byte.BYTES+Float.BYTES+str.length;
            }
        }
        // (request+request_id)+object_ref+op_code+content_len - 3 int
        ByteBuffer marshalled_msg = ByteBuffer.allocate(
                3*Integer.BYTES+content_len);
        marshalled_msg
                .putInt(request)
                .putInt(msg.getObject_ref())
                .putInt(op_code)
                .putInt(contents.size());
        for(byte[] bytes: contents) marshalled_msg.put(bytes);

        return marshalled_msg.array();
    };

    public static Message unmarshall(byte[] marshalled_msg){
        int offset=0, len=0;
        boolean is_request;
        int request_id;
        int object_ref;
        int op_code;
        Object[] content;

        ByteBuffer wrapper = ByteBuffer.wrap(marshalled_msg);
        int request = wrapper.getInt(offset);
        is_request = (request & 0x80000000) == 0x80000000;
        request_id = (request<<1) >> 1; // remove leftmost bit
        offset += Integer.BYTES;

        object_ref = wrapper.getInt(offset);
        offset+=Integer.BYTES;

        op_code = wrapper.getInt(offset);
        offset+=Integer.BYTES;

        int args_amount = wrapper.getInt(offset);
        offset+=Integer.BYTES;

        Object[] contents = new Object[args_amount];
        for(int i=0; i<args_amount; i++){
            byte class_code = wrapper.get(offset);
            offset+=Byte.BYTES;
            if(class_code == ACCOUNT_CODE){
                int acc_len = wrapper.getInt(offset);
                offset+=Integer.BYTES;
                byte[] tmp = Arrays.copyOfRange(
                        marshalled_msg, offset, offset+acc_len);
                contents[i] = Account.fromByteArray(tmp);
                offset+=acc_len;
            } else if (class_code == INT_CODE){
                contents[i] = wrapper.getInt(offset);
                offset+=Integer.BYTES;
            } else if (class_code == FLOAT_CODE){
                contents[i] = wrapper.getFloat(offset);
                offset+=Float.BYTES;
            } else if (class_code == EXCEPTION_CODE){
                int exc_len = wrapper.getInt(offset);
                byte[] tmp = Arrays.copyOfRange(
                        marshalled_msg, offset, offset+exc_len);
                contents[i] = new IllegalArgumentException(
                        new String(tmp, StandardCharsets.UTF_16));
            } else if(class_code == STRING_CODE){
                int str_len = wrapper.getInt(offset);
                offset+=Integer.BYTES;
                byte[] tmp = Arrays.copyOfRange(
                        marshalled_msg, offset, offset+str_len);
                contents[i] = new String(tmp, StandardCharsets.UTF_16);
            }
        }
        return new Message(
                is_request, request_id, object_ref, op_code, contents);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(" " + is_request + " ");
        sb.append(" " + request_id + " ");
        sb.append(" " + object_ref + " ");
        sb.append(" " + op_code + " ");
        sb.append(" " + content.length + " ");
        sb.append("]");
        return sb.toString();
    }

    public static boolean checkEqualHeader(Message a, Message b) {
        return (a.getRequest_id() == b.getRequest_id()) &&
                (a.getOp_code() == b.getOp_code()) &&
                (a.getObject_ref() == b.getObject_ref());
    }

    public static int requestToInt(int request_id, boolean request){
        return request_id | (request? 0x80000000: 0x00000000);
    }
}
