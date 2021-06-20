package nsu.oop.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private static final int PORT = 8080;

    private final Vector<ClientHandler> clients;
    private ServerSocket server;
    private Authentication authenticator;

    public Server() {
        Socket socket = null;
        clients = new Vector<>();

        try {
            server = new ServerSocket(PORT);
            authenticator = new Authentication();

            System.out.println("state: Server is working");

            while (true) {
                System.out.println("state: Server is waiting for any connection");
                socket = server.accept();
                new ClientHandler(socket, this);
                System.out.println("state: New client connected");
            }

        } catch (IOException e) {
            System.err.println("ERROR: Server did not started");
            e.printStackTrace();
        } finally {
            try {
                if (server != null) server.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getNumberOfClients() {
        return clients.size();
    }

    public synchronized void broadcast(String msg) throws IOException {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    public synchronized void broadcast(String msg, String... users) throws IOException {
        int countCurrent = 0;
        int countAll = users.length;
        for (ClientHandler c : clients) {
            for (String nick : users) {
                if (c.getName().equals(nick)) {
                    c.sendMessage(msg);
                    if (++countCurrent == countAll) return;
                }
            }
        }
    }

    public synchronized boolean haveUserAlreadyConnected(String nick) {
        for (ClientHandler c : clients) {
            if (c.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void addNewClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public Authentication getAuthenticator() {
        return this.authenticator;
    }

    private String getUserList() {
        StringBuilder sb = new StringBuilder("/user_list:");
        for (ClientHandler client : clients) {
            sb.append(" ").append(client.getName());
        }
        return sb.toString();
    }

    public void broadcastUserList(String... users) throws IOException {
        String userList = getUserList();
        broadcast(userList, users);
    }

    public void broadcastUserList() throws IOException {
        String userList = getUserList();

        for (ClientHandler client : clients) {
            client.sendMessage(userList);
        }
    }
}
