import java.io.*;
import java.net.*;

public class BerkeleySlave {
    private long currentTime;
    private int port;

    public BerkeleySlave(int port) {
        this.port = port;
        this.currentTime = System.currentTimeMillis();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Escravo Berkeley iniciado na porta " + port + " com tempo inicial: " + currentTime);

            while (true) {
                Socket masterSocket = serverSocket.accept();
                DataInputStream in = new DataInputStream(masterSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(masterSocket.getOutputStream());

                // Recebe o tempo do mestre (não usado diretamente para cálculo, mas para RTT se necessário)
                long masterTimeAtMaster = in.readLong();

                // Envia o tempo atual do escravo para o mestre
                out.writeLong(currentTime);

                // Recebe o ajuste do mestre
                long adjustment = in.readLong();
                currentTime += adjustment;
                System.out.println("Tempo ajustado na porta " + port + ": " + currentTime + " (Ajuste: " + adjustment + " ms)");

                masterSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java BerkeleySlave <porta>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        BerkeleySlave slave = new BerkeleySlave(port);
        slave.start();
    }
}
