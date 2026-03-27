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
                System.out.println("\n--- Iniciando ciclo de sincronização ---");
                
                long masterTime = System.currentTimeMillis();
                Map<Integer, Long> slaveTimesMap = new HashMap<>();
                long sum = masterTime;

                // 1. Fase de Coleta: Solicita o tempo de cada escravo
                for (int slavePort : SLAVE_PORTS) {
                    try (Socket socket = new Socket("localhost", slavePort);
                         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                         DataInputStream in = new DataInputStream(socket.getInputStream())) {

                        // Envia tempo do mestre 
                        out.writeLong(masterTime);
                        
                        // Recebe o tempo atual do escravo
                        long sTime = in.readLong();
                        slaveTimesMap.put(slavePort, sTime);
                        sum += sTime;
                        
                        System.out.println("Porta " + slavePort + " respondeu: " + sTime);

                    } catch (ConnectException e) {
                        System.err.println("Escravo na porta " + slavePort + " offline.");
                    } catch (IOException e) {
                        System.err.println("Erro na porta " + slavePort + ": " + e.getMessage());
                    }
                }

                // 2. Fase de Cálculo e Ajuste
                if (!slaveTimesMap.isEmpty()) {
                    long averageTime = sum / (slaveTimesMap.size() + 1);
                    System.out.println("Média calculada: " + averageTime);

                    for (Map.Entry<Integer, Long> entry : slaveTimesMap.entrySet()) {
                        int port = entry.getKey();
                        long slaveTime = entry.getValue();

                        try (Socket socket = new Socket("localhost", port);
                             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                            //(Média - Tempo Original do Escravo)
                            long individualAdjustment = averageTime - slaveTime;
                            out.writeLong(individualAdjustment);
                            
                            System.out.println("Ajuste enviado para " + port + ": " + individualAdjustment + "ms");

                        } catch (IOException e) {
                            System.err.println("Erro ao enviar ajuste para " + port);
                        }
                    }
                    
                    // O mestre também deve se ajustar (logicamente)
                    long masterAdjustment = averageTime - masterTime;
                    System.out.println("Auto-ajuste do mestre: " + masterAdjustment + "ms");
                } else {
                    System.out.println("Nenhum escravo disponível.");
                }

                Thread.sleep(5000); // Intervalo entre ciclos
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}