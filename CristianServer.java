import java.io.*;
import java.net.*;
import java.util.Date;

public class CristianServer {
    public static void main(String[] args) throws IOException {
        int port = 5000;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor Cristian iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                long serverTime = new Date().getTime();
                out.writeLong(serverTime);
                System.out.println("Tempo do servidor enviado: " + serverTime);

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
