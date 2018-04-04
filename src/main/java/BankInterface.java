package main.java;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BankInterface extends Remote {
    public Object createAccount(Object[] arguments) throws RemoteException;
    public Object deleteAccount(Object[] arguments) throws RemoteException;
    public Object updateAccount(Object[] arguments) throws RemoteException;
    public Object monitor(Object[] arguments) throws RemoteException;
    public Object transfer(Object[] arguments) throws RemoteException;
    public Object statistic(Object[] arguments) throws RemoteException;
}
