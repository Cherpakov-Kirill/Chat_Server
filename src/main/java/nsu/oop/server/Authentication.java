package nsu.oop.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Authentication {
    private final File file;
    private final Properties accounts;

    Authentication() {
        file = new File("src/main/resources" + System.getProperty("file.separator") + "accounts.properties");
        accounts = new Properties();
        if (file.canRead()) {
            try {
                accounts.load(new FileInputStream(file));
            } catch (IOException e) {
                try {
                    if (file.createNewFile()) System.out.println("File \"accounts.properties\" is created!");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    private void storeAccountFile() {
        try {
            accounts.store(new FileWriter(file), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean passAuthentication(String login, String pass) {
        String password = accounts.getProperty(login);
        if (password != null) {
            return password.equals(pass);
        }
        return false;
    }

    public boolean addLoginPass(String login, String pass) {
        if (accounts.getProperty(login) != null) return false;
        accounts.setProperty(login, pass);
        storeAccountFile();
        return true;
    }

    public void deleteByLogin(String login) {
        accounts.remove(login);
        storeAccountFile();
    }
}
