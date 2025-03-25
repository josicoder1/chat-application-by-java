import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatInterface extends Remote {
    void registerClient(ChatClientInterface client) throws RemoteException;
    void sendMessage(String message, String clientName) throws RemoteException;
    void sendFile(byte[] fileData, String fileName, String clientName) throws RemoteException;
}