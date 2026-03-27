import java.io.*;
import java.net.*;

public class BerkeleySlave {
    private long currentTime;
    private int port;

    public BerkeleySlave(int port) {
        this.port = port;
        int salt = (port - 6000) * new java.util.Random().nextInt(9);
        // Simulamos um pequeno desvio inicial para ver o ajuste funcionando
        this.currentTime = System.currentTimeMillis() + (new java.util.Random().nextInt(200 + salt) - 100);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Escravo iniciado na porta " + port + " | Tempo inicial: " + currentTime);

            while (true) {
                // PRIMEIRA CONEXÃO: Enviar o tempo para o Mestre
                try (Socket socket1 = serverSocket.accept();
                        DataInputStream in = new DataInputStream(socket1.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket1.getOutputStream())) {

                    in.readLong(); // Recebe o tempo do mestre (vazio)
                    out.writeLong(this.currentTime);
                    System.out.println("Enviado tempo atual: " + currentTime);
                }

                // SEGUNDA CONEXÃO: Receber o ajuste calculado
                try (Socket socket2 = serverSocket.accept();
                        DataInputStream in = new DataInputStream(socket2.getInputStream())) {

                    long adjustment = in.readLong();
                    this.currentTime += adjustment;
                    System.out.println("Ajuste de " + adjustment + "ms aplicado. Novo tempo: " + currentTime);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro no escravo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java BerkeleySlave <porta>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        BerkeleySlave slave = new BerkeleySlave(port);
        slave.start(); // Inicia o loop de escuta
    }
}