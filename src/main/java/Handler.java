package main.java;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import main.java.Bank.Account;
import main.java.Bank.Currency;


public class Handler {
    private static HashMap<Object[], LocalDateTime> monitors = new HashMap<Object[], LocalDateTime>();
    private static HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();
    private static int accountCounter = 0;

    public static HashMap<Object[], LocalDateTime> getMonitors() {
        return monitors;
    }

    public interface OperationHandler {
//        static Object[] handle(Object... arguments) throws Exception {
//            return null;
//        };
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

        public static Object[] handle(Account newAccount) {

            int newAccountNumber = ++accountCounter;

            // Check if account exists already, return account if exists
            if (checkIfAccountExists(newAccountNumber)) {
                return new Object[]{accounts.get(newAccountNumber)};
            }

            newAccount.setAccountNumber(newAccountNumber);
            accounts.put(newAccount.getAccountNumber(), newAccount);

            Object[] result = new Object[]{newAccount};
            NonIdempotentCallback(result);

            return result;
        };
    }

    public static class DeleteAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " deleted.";
            return;
        };

        public static Object[] handle(Account account) {
            int accountNumber = account.getAccountNumber();

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            accounts.remove(accountNumber);

            Object[] result = new Object[]{account};
            NonIdempotentCallback(result);

            return result;
        };
    }

    public static class UpdateAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Balance of Account " + "" + " changed by a value of " + arguments[1].toString() + ".";
            return;
        };

        public static Object[] handle(Account account, boolean draw, float amount) {
            int accountNumber = account.getAccountNumber();

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            Account storedAccount = accounts.get(accountNumber);

            if (draw) account.setBalance(account.getBalance() - amount);
            else account.setBalance(account.getBalance() + amount);

            accounts.put(accountNumber, storedAccount);

            Object[] result = new Object[]{storedAccount};
            NonIdempotentCallback(result);

            return result;
        };
    }

    public static class MonitorHandler implements OperationHandler {
        public static Object[] handle(int interval, InetAddress clientAddress, int clientPort) {
            LocalDateTime now = LocalDateTime.now();
            // Use Object array to store the clientAddress and clientPort as key
            monitors.put(new Object[]{clientAddress, clientPort}, now.plusSeconds(interval));

            // Delete outdated monitors
            for (Object[] key : monitors.keySet()) {
                if (monitors.get(key).isBefore(LocalDateTime.now())) {
                    monitors.remove(key);
                }
            }

            Object[] result = new Object[]{interval, clientAddress, clientPort};

            return result;
        };
    }

    public static class TransferHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " transferred " + "" + " to account " + "";
            return;
        };

        public static Object[] handle(Account senderAccount, Account receiverAccount, float amount) {
            int senderAccountNumber = senderAccount.getAccountNumber();
            int receiverAccountNumber = receiverAccount.getAccountNumber();

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(senderAccountNumber) && !checkIfAccountExists(receiverAccountNumber)) {
                throw new IllegalArgumentException("Account " + senderAccountNumber + " or " + receiverAccountNumber + " is not found.");
            }

            Account storedSenderAccount = accounts.get(senderAccountNumber);
            Account storedReceiverAccount = accounts.get(receiverAccountNumber);

            storedSenderAccount.setBalance(storedSenderAccount.getBalance() - amount);
            storedReceiverAccount.setBalance(storedReceiverAccount.getBalance() + amount);

            accounts.put(senderAccountNumber, storedSenderAccount);
            accounts.put(receiverAccountNumber, storedReceiverAccount);

            Object[] result = new Object[]{storedSenderAccount, storedReceiverAccount, amount};
            NonIdempotentCallback(result);

            return result;
        };
    }

    public static class CheckBalanceHandler implements OperationHandler {
        public static Object[] handle(Account account) {
            int accountNumber = account.getAccountNumber();

            // Check if account exists, raise error if not
            if (!checkIfAccountExists(accountNumber)) {
                throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
            }

            Account storedAccount = accounts.get(accountNumber);

            Object[] result = new Object[]{storedAccount};
            return result;
        };
    }

    public static class StatisticHandler implements OperationHandler {
        public static Object[] handle() {
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

            Object[] result = new Object[]{report};
            return result;
        };
    }
}
