package app;

import exception.AppException;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class ClientThread extends Thread {

    private static final String PATH_TO_FILES = "C:\\labs\\network\\server";

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
                if (word == null)
                    continue;
                String[] command = word.split(" ");
                if (command.length == 0)
                    continue;
                String[] args = new String[command.length - 1];
                System.arraycopy(command, 1, args, 0, command.length - 1);
                switch (command[0]) {
                    case "Stop":
                        sstop();
                        break;
                    case "Authorization":
                        authorization(args);
                        break;
                    case "Registration":
                        registration(args);
                        break;
                    case "UpdateUsersList":
                        updateUserList();
                        break;
                    case "UpdateFilesList":
                        updateFileList();
                        break;
                    case "SendFile":
                        sendFile(args);
                        break;
                    case "DownloadFile":
                        downloadFile(args);
                        break;
                }
            }

        }
        catch (IOException e) {
        }
    }

    private void registration(String[] args) {
        if (args.length != 3) {
            sendToClient("Не задан логин, пароль или email");
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
        server.addClient(user.getUsername(), this);
        sendToClient("Ok");
    }

    private void authorization(String[] args) {
        if (args.length != 2) {
            sendToClient("Не задан логин или пароль");
            return;
        }
        String username = args[0];
        String password = args[1];
        try {
            user = server.getUserService().authorizeUser(username, password);
        } catch (AppException e) {
            sendToClient(e.getMessage());
            return;
        }
        server.addClient(user.getUsername(), this);
        sendToClient("Ok");
    }

    private void sstop() {
        server.removeClient(user.getUsername());
        try {
            socket.close();
            inS.close();
            outS.close();
            inR.close();
            outW.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserList() {
        List<User> users = server.getUserService().findAll();
        StringBuilder data = new StringBuilder();
        users.forEach(user -> data.append(user.getUsername()).append(" "));
        sendToClient(data.toString());
    }

    private void updateFileList() {
        StringBuilder data = new StringBuilder();
        user.getFiles().forEach(file -> {
            data.append(file.getId().toString()).append(" ");
            data.append(file.getName()).append(" ");
            data.append(file.getUser().getUsername()).append(" ");
            data.append(new File(file.getPath()).length()).append(" "); // временный фикс
        });
        sendToClient(data.toString());
    }

    private void sendFile(String[] args) {
        if (args.length != 3) {
            sendToClient("Ошибка параметров команды. Ожидалось 3 параметра");
            return;
        }
        String filename = args[0];
        String receiverName = args[1];
        long size = Long.parseLong(args[2]);

        User receiver = server.getUserService().findByUsername(receiverName);

        File directory = new File(PATH_TO_FILES + "\\" + receiver.getId().toString());

        if(!directory.exists()){
            directory.mkdir();
        }

        model.File saved = server.getFileService().save(user, filename, directory.getAbsolutePath());
        server.getUserService().addFileToUser(saved, receiver);

        sendToClient("Ok");

        byte[] bytes = new byte[(int)size];

        try {
            int readSize = inS.read(bytes,0, (int)size);
            if (readSize != size) {
                sendToClient("Ошибка при отправке файла");
                return;
            }
        } catch (IOException e) {
            sendToClient("Ошибка при отправке файла");
            e.printStackTrace();
            return;
        }

        File Nfile = new File(saved.getPath());
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(Nfile,false);
            fos.write(bytes);
            fos.flush();
        } catch (IOException e) {
            sendToClient("Ошибка при сохранении файла на сервере");
            e.printStackTrace();
            return;
        }

        sendToClient("Ok");
    }

    private void downloadFile(String[] args) {
        if (args.length != 1) {
            sendToClient("Ошибка параметров команды. Ожидался 1 параметр");
            return;
        }
        String id = args[0];

        Optional<model.File> file = server.getFileService().findById(id);
        if (!file.isPresent()) {
            sendToClient("Файл не найден в базе");
            return;
        }

        File targetFile = new File(file.get().getPath());

        byte[] bytes = new byte[(int)targetFile.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(targetFile);
            fileInputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            sendToClient("Ошибка чтения файла");
            return;
        }

        sendToClient("Ok");

        try {
            outS.write(bytes);
            outS.flush();
        } catch (IOException e) {
            e.printStackTrace();
            sendToClient("Ошибка чтения файла");
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
        outS = new BufferedOutputStream(new DataOutputStream(socket.getOutputStream()));
        inS = new BufferedInputStream(new DataInputStream(socket.getInputStream()));
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
