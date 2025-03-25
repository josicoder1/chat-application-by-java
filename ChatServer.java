import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServer extends UnicastRemoteObject implements ChatInterface {
    private List<ChatClientInterface> clients = new ArrayList<>();

    public ChatServer() throws RemoteException {
        super();
    }

    @Override
    public void registerClient(ChatClientInterface client) throws RemoteException {
        clients.add(client);
        System.out.println("Client registered. Total clients: " + clients.size());
    }

    @Override
    public void sendMessage(String message, String clientName) throws RemoteException {
        String fullMessage = clientName + ":" + message;
        for (ChatClientInterface client : clients) {
            client.receiveMessage(fullMessage);
        }
    }

    @Override
    public void sendFile(byte[] fileData, String fileName, String clientName) throws RemoteException {
        String fullFileName = clientName + ":" + fileName;
        for (ChatClientInterface client : clients) {
            client.receiveFile(fileData, fullFileName);
        }
    }

    public static void main(String[] args) {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            ChatServer server = new ChatServer();
            java.rmi.Naming.rebind("ChatService", server);
            System.out.println("Chat Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}