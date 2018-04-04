package main.java.Bank;

import main.java.Bank.Currency;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Account {
    public static final int EMPTY_ACC_NUMBER = Integer.MIN_VALUE;
    public static final int PASS_LENGTH = 10;

    private int account_number = EMPTY_ACC_NUMBER;
    private String name;
    private String password;
    private Currency currency;
    private float balance;

    public Account(int account_number, String name, String password, Currency currency, float balance) {
        this.account_number = account_number;
        this.name = name;
        this.password = password;
        this.currency = currency;
        this.balance = balance;
    }

    public static Account createAccount(String name, String password, Currency currency, float balance)
        throws IllegalArgumentException
    {
        if(password.length() != PASS_LENGTH) {
            throw new IllegalArgumentException("Password length need to be 10");
        }
        return new Account(EMPTY_ACC_NUMBER, name, password, currency, balance);
    }

    public int getAccountNumber() {
        return account_number;
    }

    public void setAccountNumber(int account_number) {
        this.account_number = account_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public byte[] toByteArray() {
        byte[] acc_number, name, pass, cur, balance;
        byte[] tmp;
        int str_len;

        acc_number = ByteBuffer.allocate(Integer.BYTES)
                .putInt(getAccountNumber()).array();

        tmp = getName().getBytes(StandardCharsets.UTF_8);
        str_len = stringByteLength(getName());
        name = ByteBuffer.allocate(Integer.BYTES+str_len).putInt(str_len)
                .put(tmp).array();

        tmp = getPassword().getBytes(StandardCharsets.UTF_8);
        str_len = stringByteLength(getPassword());
        pass = ByteBuffer.allocate(Integer.BYTES+str_len).putInt(str_len)
                .put(tmp).array();

        cur = ByteBuffer.allocate(Integer.BYTES).putInt(getCurrency().getId()).array();

        balance = ByteBuffer.allocate(Float.BYTES).putFloat(getBalance()).array();

        ByteBuffer res = ByteBuffer.allocate(acc_number.length+name.length+
                pass.length+cur.length+balance.length);
        res.put(acc_number).put(name).put(pass).put(cur).put(balance);

        return res.array();
    }

    public static Account fromByteArray(byte[] acc_bytes){
        int offset = 0, str_len = 0;
        int account_number;
        String name;
        String password;
        Currency currency;
        float balance;

        ByteBuffer wrapper = ByteBuffer.wrap(acc_bytes);
        account_number = wrapper.getInt(offset);
        offset += Integer.BYTES;

        str_len = wrapper.getInt(offset);
        name = new String(acc_bytes, offset+Integer.BYTES, str_len, StandardCharsets.UTF_8);
        offset += Integer.BYTES+str_len;

        str_len = wrapper.getInt(offset);
        password = new String(acc_bytes, offset+Integer.BYTES, str_len, StandardCharsets.UTF_8);
        offset += Integer.BYTES+str_len;

        currency = Currency.fromId(ByteBuffer.wrap(acc_bytes, offset, Integer.BYTES).getInt());
        offset += Integer.BYTES;

        balance = ByteBuffer.wrap(acc_bytes, offset, Float.BYTES).getFloat();

        return new Account(account_number, name, password, currency, balance);
    }

    private static int stringByteLength(String str){
        // for now assume charset = utf-8
        return str.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(" " + account_number + " ");
        sb.append(" " + name + " ");
        sb.append(" " + password + " ");
        sb.append(" " + currency.toString() + " ");
        sb.append(" " + balance + " ");
        sb.append("]");
        return sb.toString();
    }
}
