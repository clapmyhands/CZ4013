package main.java.Bank;

import main.java.Bank.Account;

public interface BankInterface {
    public Object[] createAccount(Account account);
    public Object[] deleteAccount(Account account);
    // draw - 1 | deposit - 0
    public Object[] updateAccount(Account account, boolean draw, float amount);
    public Object[] transferMoney(Account from_acc, Account to_acc, float amount);
    // return back interval to confirm server receive request
    public Object[] monitor(int interval);
    public Object[] checkAccountBalance(Account account);
}