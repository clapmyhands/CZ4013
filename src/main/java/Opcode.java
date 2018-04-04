package cz4013.assign;

public enum Opcode {
    CREATE(1), DELETE(2), UPDATE(3), TRANSFER(4), MONITOR(5), TBD(6);

    private int operation_code = 0;

    Opcode(int code) {
        this.operation_code = code;
    }

    public int getId(){
        return this.operation_code;
    }

    public static Opcode fromCode(int code){
        for (Opcode opc: Opcode.values()){
            if(opc.operation_code == code)
                return opc;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
