package main.java;

import java.util.HashMap;
import java.util.List;
import main.java.Bank.Account;
import main.java.Bank.Currency;


public class Handler {
    private static List<Integer> monitors;
    private static HashMap<Integer, Account> accounts;
    private static int accountCounter = 0;

    public static List getMonitors() {
        return monitors;
    }

    public interface OperationHandler {
        static Object handle(Object[] arguments) throws Exception {
            return null;
        };
    }

    public static abstract class NonIdempotentOperationHandler implements OperationHandler {
        static void NonIdempotentCallback(Object[] arguments){};
    }

    private static boolean checkIfAccountExists(int accountNumber) {
        return accounts.containsKey(accountNumber);
    }

    public static class CreateAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "New account " + "" + "created.";
            return;
        };

        public static Object handle(Object[] arguments) {

            int newAccountNumber = ++accountCounter;

            // Check if account exists already, return account if exists
            if (checkIfAccountExists(newAccountNumber)) {
                return accounts.get(newAccountNumber);
            }

            Account newAccount = Account.createAccount(arguments[0].toString(), arguments[1].toString(), (Currency) arguments[2], (float) arguments[3]);
            accounts.put(newAccount.getAccountNumber(), newAccount);

            NonIdempotentCallback(arguments);

            return newAccount;
        };
    }

    public static class DeleteAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " deleted.";
            return;
        };

        public static Object handle(Object[] arguments) {
            int accountNumber = (int) arguments[0];

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            accounts.remove(accountNumber);

            return null;
        };
    }

    public static class UpdateAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Balance of Account " + "" + " changed by a value of " + arguments[1].toString() + ".";
            return;
        };

        public static Object handle(Object[] arguments) {
            int accountNumber = (int) arguments[0];

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            Account account = accounts.get(accountNumber);
            float balanceChange = (float) arguments[1];
            account.setBalance(account.getBalance() + balanceChange);
            accounts.put(accountNumber, account);

            return account;
        };
    }

    public static class MonitorHandler implements OperationHandler {
        public static Object handle(Object[] arguments) {
            int accountNumber = (int) arguments[0];

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            monitors.add(accountNumber);
            Account account = accounts.get(accountNumber);

            return account;
        };
    }

    public static class TransferHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " transferred " + "" + " to account " + "";
            return;
        };

        public static Object handle(Object[] arguments) {
            int senderAccountNumber = (int) arguments[0];
            int receiverAccountNumber = (int) arguments[1];

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(senderAccountNumber) && !checkIfAccountExists(receiverAccountNumber)) {
                throw new IllegalArgumentException("Account " + senderAccountNumber + " or " + receiverAccountNumber + " is not found.");
            }

            Account senderAccount = accounts.get(senderAccountNumber);
            Account receiverAccount = accounts.get(receiverAccountNumber);

            float transferAmount = (float) arguments[2];
            senderAccount.setBalance(senderAccount.getBalance() - transferAmount);
            receiverAccount.setBalance(receiverAccount.getBalance() + transferAmount);

            accounts.put(senderAccountNumber, senderAccount);
            accounts.put(receiverAccountNumber, receiverAccount);

            return senderAccount;
        };
    }

    public static class StatisticHandler implements OperationHandler {
        public static Object handle(Object[] arguments) {
            String report = "Bank Statistics";
            report += "\n===========================";

            report += "\nTotal accounts: " + accounts.size();
            report += "\nTotal money:";

            HashMap<Currency, Float> totalMoney = new HashMap<Currency, Float>();
            for (int accountNumber : accounts.keySet()) {
                Account cur = accounts.get(accountNumber);
                float newAmount;

                if (totalMoney.containsKey(cur.getCurrency())) {
                    newAmount = totalMoney.get(cur.getCurrency()) + cur.getBalance();
                }
                else {
                    newAmount = cur.getBalance();
                }

                totalMoney.put(cur.getCurrency(), newAmount);
            }

            for (Currency currency : totalMoney.keySet()) {
                report += "\n\t" + totalMoney.get(currency).toString() + " " + currency;
            }

            return report;
        };
    }
}
