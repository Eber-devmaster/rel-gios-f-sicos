import java.io.*;
import java.net.*;

public class CristianClient {
    public static void main(String[] args) throws IOException {
        String hostname = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(hostname, port)) {
            long clientSendTime = System.currentTimeMillis();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            long serverTime = in.readLong();

            long clientReceiveTime = System.currentTimeMillis();

            long rtt = clientReceiveTime - clientSendTime;
            long estimatedServerTime = serverTime + (rtt / 2);

            System.out.println("Tempo do cliente (antes da sincronização): " + clientSendTime);
            System.out.println("Tempo do servidor recebido: " + serverTime);
            System.out.println("RTT: " + rtt + " ms");
            System.out.println("Tempo do cliente (após sincronização - estimado): " + estimatedServerTime);

        } catch (UnknownHostException ex) {
            System.out.println("Servidor não encontrado: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Erro de I/O: " + ex.getMessage());
        }
    }
}
