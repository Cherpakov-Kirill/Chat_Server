package nsu.oop.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private Server server;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    private String inputMessage;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.server = server;
            this.name = "NoName";
        } catch (IOException e) {
            e.getMessage();
        }

        new Thread(() -> {
            try {
                System.out.println("client: Server is waiting for authentication");
                out.writeUTF("Server needs a authentication");
                while (!socket.isClosed()) {
                    while (!socket.isClosed()) {
                        readMessage();
                        if (inputMessage.startsWith("/auth")) {
                            String[] elements = inputMessage.split(" ");
                            String login = elements[1];
                            String pass = elements[2];

                            if (elements.length == 3) {
                                if (server.getAuthenticator().passAuthentication(login, pass)) {
                                    if (!server.haveUserAlreadyConnected(login)) {
                                        this.name = login;
                                        sendMessage("/auth_success " + login);
                                        setAuthorized(true);
                                        break;
                                    } else {
                                        sendMessage("/auth_failed User account already had been created");
                                    }
                                } else {
                                    sendMessage("/auth_failed Wrong login or password");
                                }
                            } else {
                                sendMessage("/auth_failed Wrong number of parameters for auth");
                            }
                        } else if (inputMessage.startsWith("/registration")) {
                            String[] elements = inputMessage.split(" ");
                            String login = elements[1];
                            String pass = elements[2];

                            if (elements.length == 3) {
                                if (server.getAuthenticator().addLoginPass(login, pass)) {
                                    sendMessage("/registration_success " + login);
                                    this.name = login;
                                    setAuthorized(true);
                                    break;
                                } else {
                                    sendMessage("/registration_failed This login already had been employed");
                                }
                            } else {
                                sendMessage("/registration_failed Wrong number of parameters for registration");
                            }
                        } else if (inputMessage.startsWith("/end")) {
                            server.broadcast(inputMessage, name);
                            socket.close();
                            break;
                        } else {
                            sendMessage("/auth_failed Server needs a authentication firstly");
                        }
                    }
                    while (!socket.isClosed()) {
                        readMessage();
                        if (inputMessage.equalsIgnoreCase("/end")) {
                            server.broadcast(inputMessage + " " + name);
                            socket.close();
                            break;
                        } else if (inputMessage.startsWith("@")) {
                            String[] elements = inputMessage.split(" ");
                            if (elements.length == 2) {
                                String destination = elements[0].substring(1);
                                if (destination.isEmpty())
                                    server.broadcast(name + " : " + inputMessage);
                                String message = elements[1];
                                server.broadcast(name + " : " + elements[0] + ", " + message, name, destination);
                            } else {
                                server.broadcast("!help: @username message", name);
                            }
                        } else if (inputMessage.startsWith("/private_message")) {
                            String[] elements = inputMessage.split(" ");
                            if (elements.length == 3) {
                                String destination = elements[1];
                                String message = elements[2];
                                server.broadcast("/private_message " + name + " : " + message, name, destination);
                            } else {
                                server.broadcast("!help: /private_message username message", name);
                            }
                        } else if (inputMessage.equalsIgnoreCase("/list")) {
                            server.broadcastUserList(name);
                        } else if (inputMessage.equalsIgnoreCase("/delete_account")) {
                            server.getAuthenticator().deleteByLogin(name);
                            server.broadcast(inputMessage, name);
                            break;
                        } else {
                            server.broadcast(name + " : " + inputMessage);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                try {
                    setAuthorized(false);
                    socket.close();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
                System.out.println("Number of Online clients = " + server.getNumberOfClients());
            }
        }).start();
    }

    private void readMessage() throws IOException {
        try {
            inputMessage = in.readUTF();
            System.out.println("Receive <- " + name + ": " + inputMessage);
        } catch (IOException exception) {
            throw new IOException("ERROR with reading from " + name);
        }
    }

    public void sendMessage(String msg) throws IOException {
        try {
            System.out.println("Send -> " + (this.name != null ? " " + this.name : "") + ": " + msg);
            out.writeUTF(msg);
            out.flush();
        } catch (IOException exception) {
            throw new IOException("ERROR with sending to " + name);
        }
    }

    public String getName() {
        return name;
    }

    private void setAuthorized(boolean isAuthorized) throws IOException {
        if (isAuthorized) {
            server.addNewClient(this);

            if (!name.isEmpty()) {
                server.broadcast(name + " is coming to the chat");
                server.broadcastUserList();
            }
        } else {
            server.removeClient(this);

            if (!name.isEmpty()) {
                server.broadcast(name + " went out from chat");
                server.broadcastUserList();
            }
        }
    }
}
