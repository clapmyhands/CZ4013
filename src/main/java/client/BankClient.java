package main.java.client;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import main.java.Bank.Account;
import main.java.Bank.Currency;
import main.java.Opcode;
import main.java.client.BankInterfaceProxy;

public class BankClient {
    private static Scanner scanner;

    private static Scanner getScanner(){
        if(scanner==null){
            scanner = new Scanner(System.in);
        }
        return scanner;
    }

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
        System.out.println("7. Exit");
        System.out.println("Enter a command to proceed:");
    }

    public static void main(String[] args) {
//        BankClient bankClient = new BankClient();
        InetAddress addr; int port = 8098;
        BankInterfaceProxy proxy = new BankInterfaceProxy(port);
        try{
//            addr = InetAddress.getByName(args[0]);
//            addr = InetAddress.getByName("10.27.84.62");
            addr = InetAddress.getLocalHost();
            proxy.setServerAddress(addr);
        } catch (Exception e){
            e.printStackTrace();
        }

        Scanner sc = BankClient.getScanner();
        System.out.println("Bank Client Started...");
        print_menu();
        for(;;){
            int order_code = Integer.parseInt(sc.nextLine());
            if(order_code == 7){
                System.exit(0);
                proxy.udpConnection.close();
            }
            Opcode order = Opcode.fromCode(order_code);
            Object[] return_arg; Account tmp;
            Account acc; float amount;
            try {
                switch (order) {
                    case CREATE:
                        System.out.println("Creating account...");
                        acc = ask_particulars(true, true, true, true, false);
                        System.out.println(acc.toString());
                        return_arg = proxy.createAccount(acc);
                        tmp = (Account)return_arg[0];
                        System.out.println(String.format("Created account: %d under name %s",
                                tmp.getAccountNumber(), tmp.getName()));
                        break;
                    case DELETE:
                        System.out.println("Deleting account...");
                        acc = ask_particulars(true, true, false, false, true);
                        return_arg = proxy.deleteAccount(acc);
                        tmp = (Account)return_arg[0];
                        System.out.println(String.format("Deleted account: %d under name %s",
                                tmp.getAccountNumber(), tmp.getName()));
                        break;
                    case UPDATE:
                        System.out.println("Draw/Deposit money");
                        acc = ask_particulars(true, true, true, false, true);
                        System.out.println("Specify transaction: draw(1) / deposit(0)");
                        int op = Integer.parseInt(sc.nextLine());
                        if(op!=0 && op!=1)
                            throw new IllegalArgumentException("Transaction operation can only be draw(1) or deposit(0)");
                        System.out.println("Enter amount of money:");
                        amount = Float.parseFloat(sc.nextLine());
                        if(amount < 0)
                            throw new IllegalArgumentException("Amount cannot be negative");
                        return_arg = proxy.updateAccount(acc, op==1, amount);
                        tmp = (Account)return_arg[0];
                        System.out.println(String.format("account no: %d %s %f",
                                tmp.getAccountNumber(),
                                (int)return_arg[1]==1? "Drawn": "Deposited",
                                (float)return_arg[2]));
                        System.out.println(String.format(
                                "Account new balance: %f", tmp.getBalance()));
                        break;
                    case TRANSFER:
                        System.out.println("Transfer Money");
                        System.out.println("Enter account particular to transfer money from");
                        Account acc_f = ask_particulars(true, true, false, false, true);

                        System.out.println("Enter account particular to transfer money to");
                        Account acc_t = ask_particulars(false, false, false, false, true);

                        System.out.println("Enter amount to transfer:");
                        amount = Float.parseFloat(sc.nextLine());
                        if(amount < 0)
                            throw new IllegalArgumentException("Amount cannot be negative");
                        return_arg = proxy.transferMoney(acc_f, acc_t, amount);
                        tmp = (Account)return_arg[0];
                        Account temp = (Account)return_arg[1];
                        System.out.println(String.format("Transfered from acc. no: %d to acc. no: %d with amount: %f",
                                tmp.getAccountNumber(), temp.getAccountNumber(), (float)return_arg[2]));
                        break;
                    case MONITOR:
                        System.out.println("Enter interval to monitor in seconds:");
                        int interval = Integer.parseInt(sc.nextLine());
                        proxy.monitor(interval);
                        break;
                    case CHECK:
                        System.out.println("Enter account particular to check");
                        acc = ask_particulars(true, true, true, false, true);
                        return_arg = proxy.checkAccountBalance(acc);
                        tmp = (Account)return_arg[0];
                        System.out.println(String.format(
                                "Account no: %d under the name %s with currency: %s have balance: %f",
                                tmp.getAccountNumber(),
                                tmp.getName(),
                                tmp.getCurrency(),
                                tmp.getBalance()));
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
        Scanner sc = BankClient.getScanner();

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
            currency = Currency.fromId(Integer.parseInt(sc.nextLine()));
            if(currency==null)
                throw new IllegalArgumentException("Currency is not valid");
        }
        if(f_acc_balance){
            System.out.println("Enter the account balance:");
            balance = Float.parseFloat(sc.nextLine());
            if(balance<0)
                throw new IllegalArgumentException("Balance can't be negative");
        }
        if(f_account_no){
            System.out.println("Enter Account number:");
            account_no = Integer.parseInt(sc.nextLine());
        }
        Account acc = new Account(account_no, name, pass, currency, balance);
        return acc;
    }
}
