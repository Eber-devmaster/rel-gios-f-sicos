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
                System.out.println("\n--- Iniciando Ciclo de Sincronização ---");
                
                long masterTime = System.currentTimeMillis();
                Map<Integer, Long> slaveTimesMap = new HashMap<>();
                long sum = masterTime;

                // 1. Fase de Coleta: Solicita o tempo de cada escravo
                for (int slavePort : SLAVE_PORTS) {
                    try (Socket socket = new Socket("localhost", slavePort);
                         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                         DataInputStream in = new DataInputStream(socket.getInputStream())) {

                        out.writeLong(masterTime); 
                        long sTime = in.readLong();
                        
                        slaveTimesMap.put(slavePort, sTime);
                        sum += sTime;
                        
                        System.out.println("[COLETA] Escravo " + slavePort + " enviou: " + sTime + " ms");

                    } catch (ConnectException e) {
                        System.err.println("[ERRO] Escravo na porta " + slavePort + " está offline.");
                    } catch (IOException e) {
                        System.err.println("[ERRO] Falha na porta " + slavePort + ": " + e.getMessage());
                    }
                }

                // 2. Fase de Cálculo e Distribuição
                if (!slaveTimesMap.isEmpty()) {
                    long averageTime = sum / (slaveTimesMap.size() + 1);
                    System.out.println("[CÁLCULO] Média do grupo: " + averageTime + " ms");

                    for (Map.Entry<Integer, Long> entry : slaveTimesMap.entrySet()) {
                        int port = entry.getKey();
                        long originalSlaveTime = entry.getValue();

                        try (Socket socket = new Socket("localhost", port);
                             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                            // Ajuste individual = Média - Tempo que o escravo tinha
                            long adjustment = averageTime - originalSlaveTime;
                            out.writeLong(adjustment);
                            
                            System.out.println("[AJUSTE] Enviado para " + port + ": " + adjustment + " ms");

                        } catch (IOException e) {
                            System.err.println("[ERRO] Falha ao enviar ajuste para " + port);
                        }
                    }
                    
                    long masterAdjustment = averageTime - masterTime;
                    System.out.println("[MESTRE] Meu ajuste local: " + masterAdjustment + " ms");
                } else {
                    System.out.println("[AVISO] Nenhum escravo ativo para sincronizar.");
                }

                Thread.sleep(1000); // Aguarda 5 segundos para o próximo ciclo
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}