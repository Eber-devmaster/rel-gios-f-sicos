import java.io.*;
import java.net.*;
import java.util.*;

public class BerkeleyMaster {
    private static final int MASTER_PORT = 6000;
    private static final List<Integer> SLAVE_PORTS = Arrays.asList(6001, 6002, 6003);

    public static void main(String[] args) {
        try (ServerSocket masterSocket = new ServerSocket(MASTER_PORT)) {
            System.out.println("Mestre Berkeley iniciado na porta " + MASTER_PORT);

            while (true) {
                System.out.println("\nIniciando ciclo de sincronização...");
                List<Long> slaveTimes = new ArrayList<>();
                long masterTime = System.currentTimeMillis();
                slaveTimes.add(masterTime);

                for (int slavePort : SLAVE_PORTS) {
                    try (Socket slaveSocket = new Socket("localhost", slavePort);
                         DataInputStream in = new DataInputStream(slaveSocket.getInputStream());
                         DataOutputStream out = new DataOutputStream(slaveSocket.getOutputStream())) {

                        // Envia o tempo do mestre para o escravo
                        out.writeLong(masterTime);
                        // Recebe o tempo do escravo
                        long slaveTime = in.readLong();
                        slaveTimes.add(slaveTime);
                        System.out.println("Tempo recebido do escravo na porta " + slavePort + ": " + slaveTime);

                    } catch (ConnectException e) {
                        System.err.println("Escravo na porta " + slavePort + " não está ativo. Ignorando.");
                    } catch (IOException e) {
                        System.err.println("Erro de comunicação com escravo na porta " + slavePort + ": " + e.getMessage());
                    }
                }

                if (slaveTimes.size() > 1) { // Pelo menos o mestre e um escravo
                    long averageTime = calculateAverage(slaveTimes);
                    System.out.println("Tempo médio calculado: " + averageTime);

                    // Envia ajustes para os escravos
                    for (int slavePort : SLAVE_PORTS) {
                        try (Socket slaveSocket = new Socket("localhost", slavePort);
                             DataOutputStream out = new DataOutputStream(slaveSocket.getOutputStream())) {

                            long adjustment = averageTime - masterTime; // Ajuste baseado no tempo do mestre
                            out.writeLong(adjustment);
                            System.out.println("Ajuste enviado para escravo na porta " + slavePort + ": " + adjustment + " ms");

                        } catch (ConnectException e) {
                            // Já tratado acima, apenas ignora se o escravo não estiver ativo
                        } catch (IOException e) {
                            System.err.println("Erro ao enviar ajuste para escravo na porta " + slavePort + ": " + e.getMessage());
                        }
                    }
                } else {
                    System.out.println("Nenhum escravo ativo para sincronizar.");
                }

                try {
                    Thread.sleep(5000); // Sincroniza a cada 5 segundos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Mestre interrompido.");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long calculateAverage(List<Long> times) {
        // Implementação simples, sem descarte de outliers como mencionado no PDF.
        // Para uma implementação mais robusta, seria necessário adicionar lógica para outliers.
        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        return sum / times.size();
    }
}
