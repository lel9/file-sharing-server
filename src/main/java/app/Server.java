package app;

import model.File;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import service.FileService;
import service.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Component
public class Server {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    private Map<String, ClientThread> map = new HashMap<>();

    public void startServer() {
        ServerSocket server;
        try {
            server = new ServerSocket(8000);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while (true) {
            try {
                Socket socket = server.accept();
                System.out.println("Somebody connected");
                new ClientThread(socket, this).start();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public FileService getFileService() {
        return fileService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void addClient(String username, ClientThread client) {
        map.put(username, client);
    }

    public Socket getClientSocketByUsername(String username) {
        ClientThread clientThread = map.get(username);
        if (clientThread != null)
            return clientThread.getSocket();
        else
            return null;
    }
}