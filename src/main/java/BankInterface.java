package main.java;

public interface BankInterface {
    public Object createAccount(Object[] arguments);
    public Object deleteAccount(Object[] arguments);
    public Object updateAccount(Object[] arguments);
    public Object monitor(Object[] arguments);
    public Object transfer(Object[] arguments);
    public Object statistic(Object[] arguments);
}
