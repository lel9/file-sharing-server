package app;

import exception.AppException;
import model.User;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread {

    private Socket socket;
    private BufferedInputStream inS; // поток чтения из сокета
    private BufferedOutputStream outS; // поток записи в сокет
    private BufferedReader inR; // поток чтения из сокета
    private BufferedWriter outW; // поток записи в сокет
    private Server server;
    private User user;

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run(){
        String word;
        try {
            while (true) {
                word = inR.readLine();
                String[] command = word.split(" ");
                if (command.length == 0)
                    continue;
                String[] args = new String[command.length - 1];
                System.arraycopy(command, 1, args, 0, command.length - 1);
                switch (command[0]) {
                    case "Stop":
                        break;
                    case "Authorization":
                        authorization(args);
                        break;
                    case "Registration":
                        registration(args);
                        break;
                    case "UpdateUsersList":
                        break;
                    case "UpdateFilesList":
                        break;
                }
            }

        }
        catch (IOException e) {
        }
    }

    private void registration(String[] args) {
        if (args.length != 3) {
            sendToClient("Неверное количество параметров команды. Ожидалось: 3");
            return;
        }
        String username = args[0];
        String email = args[1];
        String password = args[2];
        try {
            user = server.getUserService().registerUser(username, email, password);
        } catch (AppException e) {
            sendToClient(e.getMessage());
            return;
        }
        sendToClient("Ok");
    }

    private void authorization(String[] args) {
        if (args.length != 2) {
            sendToClient("Неверное количество параметров команды. Ожидалось: 2");
            return;
        }
        String username = args[0];
        String password = args[1];
        try {
            user = server.getUserService().authorizeUser(username, password);
        } catch (AppException e) {
            server.addClient(user.getUsername(), this);
            sendToClient(e.getMessage());
            return;
        }
        sendToClient("Ok");
    }

    private void sendToClient(String message) {
        try {
            outW.write(message + "\n");
            outW.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void help () throws IOException {
        outW.write("help" +"\n");
        outW.flush();
        outW.write("Для вызова справки введите: ?" +"\n");
        outW.flush(); // выталкиваем все из буфера
        outW.write("Для отправки файла введите: /send file"  + "\n");
        outW.flush(); // выталкиваем все из буфер
    }

    public ClientThread(Socket socket, Server server)throws IOException {
        this.socket = socket;
        this.server = server;
        inR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void send(String msg, Socket socket) {
        try {
            inR = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outW = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            outS = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
            inS = new BufferedInputStream(new DataInputStream(socket.getInputStream()));
            outW.write(msg + "\n");
            outW.flush();
        } catch (IOException ignored) {}
    }
}
