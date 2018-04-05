package main.java.client;

import java.net.InetAddress;
import java.util.Scanner;

import main.java.Bank.Account;
import main.java.Bank.Currency;
import main.java.Opcode;
import main.java.client.BankInterfaceProxy;

public class BankClient {
    BankClient() {
    }

    public static void print_menu() {
        System.out.println();
        System.out.println("List of Command:");
        System.out.println("1. Create Account");
        System.out.println("2. Delete Account");
        System.out.println("3. Draw/Deposit money");
        System.out.println("4. Transfer money");
        System.out.println("5. Monitor update");
        System.out.println("6. Check account balance");
        System.out.println("Enter a command to proceed:");
    }

    public static void main(String[] args) {
//        BankClient bankClient = new BankClient();
        InetAddress addr; int port = 4445;
        BankInterfaceProxy proxy = new BankInterfaceProxy(port);
        try{
            addr = InetAddress.getByName(args[0]);
            proxy.setServerAddress(addr);
        } catch (Exception e){
            e.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Bank Client Started...");
        print_menu();
        for(;;){
            Opcode order = Opcode.fromCode(sc.nextInt());
            Account acc; float amount;
            try {
                switch (order) {
                    case CREATE:
                        acc = ask_particulars(true, true, true, true, false);
                        System.out.println(acc.toString());
                        proxy.createAccount(acc);
                        break;
                    case DELETE:
                        acc = ask_particulars(true, true, false, false, true);
                        proxy.deleteAccount(acc);
                        break;
                    case UPDATE:
                        acc = ask_particulars(true, true, true, false, true);
                        System.out.println("Specify transaction: draw(1) / deposit(0)");
                        int op = sc.nextInt();
                        if(op!=0 || op!=1)
                            throw new IllegalArgumentException("Transaction operation can only be draw(1) or deposit(0)");
                        System.out.println("Enter amount of money:");
                        amount = sc.nextFloat();
                        if(amount < 0)
                            throw new IllegalArgumentException("Amount cannot be negative");
                        proxy.updateAccount(acc, op==1, amount);
                        break;
                    case TRANSFER:
                        System.out.println("Enter account particular to transferMoney from");
                        Account acc_f = ask_particulars(true, true, false, false, true);

                        System.out.println("Enter account particular to transferMoney to");
                        Account acc_t = ask_particulars(false, false, false, false, true);

                        amount = sc.nextFloat();
                        if(amount < 0)
                            throw new IllegalArgumentException("Amount cannot be negative");
                        proxy.transferMoney(acc_f, acc_t, amount);
                        break;
                    case MONITOR:
                        System.out.println("Enter interval to monitor in milliseconds:");
                        int interval = sc.nextInt();
                        proxy.monitor(interval);
                        break;
                    case CHECK:
                        System.out.println("Enter account particular to check");
                        acc = ask_particulars(true, true, true, false, true);
                        proxy.checkAccountBalance(acc);
                        break;
                    default:
                        System.out.println("Invalid input...");
                        break;
                }
            } catch (IllegalArgumentException e){
                System.out.println(e.getMessage());
            }
            print_menu();
        }
    }

    public static Account ask_particulars(boolean f_name, boolean f_pass,
                                          boolean f_currency, boolean f_acc_balance,
                                          boolean f_account_no)
    throws IllegalArgumentException {
        String name = "", pass = "";
        int account_no = Account.EMPTY_ACC_NUMBER;
        Currency currency = Currency.default_val;
        float balance = 0;
        Scanner sc = new Scanner(System.in);

        if(f_name){
            System.out.println("Enter the name:");
            name = sc.nextLine();
        }
        if(f_pass){
            System.out.println("Enter the password (10 characters):");
            pass = sc.nextLine();
            if(pass.length()!=10)
                throw new IllegalArgumentException("Password length need to be 10 characters");
        }
        if(f_currency){
            for(Currency cur: Currency.values()){
                System.out.println(String.format("%d. %s",cur.getId(), cur.name()));
            }
            System.out.println("Choose the currency:");
            currency = Currency.fromId(sc.nextInt());
            if(currency==null)
                throw new IllegalArgumentException("Currency is not valid");
        }
        if(f_acc_balance){
            System.out.println("Enter the account balance:");
            balance = sc.nextFloat();
            if(balance<0)
                throw new IllegalArgumentException("Balance can't be negative");
        }
        if(f_account_no){
            System.out.println("Enter Account number:");
            account_no = sc.nextInt();
        }
        Account acc = new Account(account_no, name, pass, currency, balance);
        return acc;
    }
}
