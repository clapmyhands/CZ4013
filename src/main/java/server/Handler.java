package main.java.server;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

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

    private static boolean checkIfAccountParticularsEqual(Account a, Account b)
    throws IllegalArgumentException{
        if(a.getAccountNumber()!=b.getAccountNumber()){
            throw new IllegalArgumentException("Account Number does not match");
        }
        String a_name = a.getName();
        String b_name = b.getName();
        if(!Objects.equals(a_name, b_name)){
            throw new IllegalArgumentException("Name registered to Account does not match");
        }
        String a_pass = a.getPassword();
        String b_pass = b.getPassword();
        if(Objects.equals(a_pass, b_pass)){
            throw new IllegalArgumentException("Wrong password");
        }
        return true;
    }

    public static class CreateAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "New account " + "" + "created.";
            return;
        };

        public static Object[] handle(Account newAccount) {
            try{
                int newAccountNumber = ++accountCounter;

                // Check if account exists already, return account if exists
                if (checkIfAccountExists(newAccountNumber)) {
                    return new Object[]{accounts.get(newAccountNumber)};
                }

                newAccount.setAccountNumber(newAccountNumber);
                accounts.put(newAccountNumber, newAccount);

                Object[] result = new Object[]{newAccount};
                return result;
            } catch (IllegalArgumentException e) {
                return new Object[]{e};
            }
        };
    }

    public static class DeleteAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " deleted.";
            return;
        };

        public static Object[] handle(Account account) {
            try {
                int accountNumber = account.getAccountNumber();

                // Check if account exists, raise error if not
                if (!checkIfAccountExists(accountNumber)) {
                    throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
                }

                Account storedAccount = accounts.get(accountNumber);
                checkIfAccountParticularsEqual(account, storedAccount);
                accounts.remove(accountNumber);

                Object[] result = new Object[]{storedAccount};
                NonIdempotentCallback(result);

                return result;
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return new Object[]{e};
            }
        };
    }

    public static class UpdateAccountHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Balance of Account " + "" + " changed by a value of " + arguments[1].toString() + ".";
            return;
        };

        public static Object[] handle(Account account, boolean draw, float amount) {
            try {
                int accountNumber = account.getAccountNumber();

                // Check if account exists, raise error if not
                if (!checkIfAccountExists(accountNumber)) {
                    throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
                }

                Account storedAccount = accounts.get(accountNumber);

                if (draw){
                    if(storedAccount.getBalance()-amount < 0)
                        throw new IllegalArgumentException("Balance is less than drawn amount");
                    storedAccount.setBalance(storedAccount.getBalance() - amount);
                }
                else storedAccount.setBalance(storedAccount.getBalance() + amount);

                accounts.put(accountNumber, storedAccount);

                Object[] result = new Object[]{storedAccount, draw?1:0, amount};
                NonIdempotentCallback(result);

                return result;
            } catch (IllegalArgumentException e) {
                return new Object[]{e};
            }
        };
    }

    public static class TransferHandler extends NonIdempotentOperationHandler {
        public static void NonIdempotentCallback(Object[] arguments) {
            String message = "Account " + "" + " transferred " + "" + " to account " + "";
            return;
        };

        public static Object[] handle(Account senderAccount, Account receiverAccount, float amount) {
            try{
                int senderAccountNumber = senderAccount.getAccountNumber();
                int receiverAccountNumber = receiverAccount.getAccountNumber();

                if (!checkIfAccountExists(senderAccountNumber)) {
                    throw new IllegalArgumentException("Account " + senderAccountNumber + " is not found.");
                } else if(!checkIfAccountExists(receiverAccountNumber)){
                    throw new IllegalArgumentException("Account " + receiverAccountNumber + " is not found.");
                }

                Account storedSenderAccount = accounts.get(senderAccountNumber);
                Account storedReceiverAccount = accounts.get(receiverAccountNumber);

                checkIfAccountParticularsEqual(storedSenderAccount, senderAccount);
                if(storedSenderAccount.getCurrency()!=storedReceiverAccount.getCurrency())
                    throw new IllegalArgumentException("Transfer between different currency are not supported");
                if(storedSenderAccount.getBalance()-amount < 0)
                    throw new IllegalArgumentException("Sender balance is less than transferred amount");

                storedSenderAccount.setBalance(storedSenderAccount.getBalance() - amount);
                storedReceiverAccount.setBalance(storedReceiverAccount.getBalance() + amount);

                accounts.put(senderAccountNumber, storedSenderAccount);
                accounts.put(receiverAccountNumber, storedReceiverAccount);

                Object[] result = new Object[]{storedSenderAccount, storedReceiverAccount, amount};
                NonIdempotentCallback(result);
                return result;

            } catch (IllegalArgumentException e) {
                return new Object[]{e};
            }
        }
    }

    public static class CheckBalanceHandler implements OperationHandler {
        public static Object[] handle(Account account) {
            try{
                int accountNumber = account.getAccountNumber();

                // Check if account exists, raise error if not
                if (!checkIfAccountExists(accountNumber)) {
                    throw new IllegalArgumentException("Account " + accountNumber + " is not found.");
                }

                Account storedAccount = accounts.get(accountNumber);

                Object[] result;

                checkIfAccountParticularsEqual(storedAccount, account);
                return new Object[]{storedAccount};
            } catch (IllegalArgumentException e) {
                return new Object[]{e};
            }
        };
    }
}
