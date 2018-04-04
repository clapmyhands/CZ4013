package main.java.Bank;

import main.java.Bank.Account;

public interface BankInterface {
    public Object[] createAccount(Account account) throws IllegalArgumentException;
    public Object[] deleteAccount(Account account) throws IllegalArgumentException;
    // draw - 1 | deposit - 0
    public Object[] updateAccount(Account account, boolean draw, float amount) throws IllegalArgumentException;
    public Object[] transferMoney(Account from_acc, Account to_acc, float amount) throws IllegalArgumentException;
    // return back interval to confirm server receive request
    public Object[] monitor(int interval) throws IllegalArgumentException;
    public Object[] checkAccountBalance(Account account) throws IllegalArgumentException;
}