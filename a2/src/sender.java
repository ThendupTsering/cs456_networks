import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * Created by Thendup on 2016-11-07.
 */
public class sender {
    public static void main(String argv[]) throws Exception {
        if (argv.length == 4) {
            String emulatorName = argv[0];
            int senderDataPort = Integer.parseInt(argv[1]); // Emulator port to receive data from sender
            int emulatorACKPort = Integer.parseInt(argv[2]); // Sender port to receive ACK from emulator
            String fileName = argv[3];
            int dataMaxSize = 500;

            try {
                PrintWriter seqNumLog = new PrintWriter("seqnum.log", "UTF-8");
                PrintWriter ackLog = new PrintWriter("ack.log", "UTF-8");
                ArrayList<String> dataList = new ArrayList<>();
                // Parse file
                try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                    String fileLine;
                    while ((fileLine = br.readLine()) != null) {
                        if (fileLine.length() > dataMaxSize) {
                            // Line is too large, split line up
                            int splits = fileLine.length()/dataMaxSize;
                            String linePart;
                            for (int i = 0; i < splits; i++) {
                                linePart = fileLine.substring(i*dataMaxSize,(i+1)*dataMaxSize);
                                dataList.add(linePart);
                            }
                            linePart = fileLine.substring(splits*dataMaxSize, fileLine.length());
                            dataList.add(linePart);
                        } else {
                            dataList.add(fileLine);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading file: " + e);
                }

                int timeout = 100;
                serverGBN sender = new serverGBN(emulatorName, senderDataPort, emulatorACKPort);
                int seqNum = 0;
                int dataIndex = 0;
                int base = 0;
                int windowSize = 10;
                int dataListSize = dataList.size();
                LinkedList<packet> packetQueue = new LinkedList<>();

                // Start with First 10 Packets
                int initLimit = Math.min(windowSize, dataListSize);
                for (int i = 0; i < initLimit; i++) {
                    packetQueue.add(sender.createPacket(seqNum, dataList.get(i)));
                    seqNum++;
                    dataIndex++;
                }

                // Send first 10 packets
                sender.sendAllPackets(packetQueue, seqNumLog);

                Date beginTime = new Date();

                while (!packetQueue.isEmpty()) {
                    packet receivedPacket;
                    Date currentTime = new Date();

                    // Timeout
                    if (currentTime.getTime() - beginTime.getTime() > timeout) {
                        System.out.println("Timeout! Sending all packets");
                        sender.sendAllPackets(packetQueue, seqNumLog);
                        beginTime = currentTime;
                    }

                    try {
                        receivedPacket = sender.receivePacket(true);
                    } catch (Exception e) {
                        System.out.println("Error receiving packet: " + e);
                        continue;
                    }

                    if (receivedPacket == null) {
                        continue;
                    }

                    int receivedSeqNum = receivedPacket.getSeqNum();
                    if (receivedSeqNum >= base) {
                        // Update base. All packets before receivedSeqNum have been acked
                        System.out.println("Received ACK: " + receivedPacket.getSeqNum() + ", ackFromReceiver");

                        int numAcked = receivedSeqNum - base + 1;
                        for (int i = 0; i < numAcked; i++) {
                            System.out.println("Removing packet: " + packetQueue.peekFirst().getSeqNum());
                            packetQueue.removeFirst();
                        }

                        for (int i = 0; i < numAcked; i++) {
                            if (dataIndex < dataListSize) {
                                System.out.println("Adding packet: " + seqNum + " to queue");
                                packetQueue.add(sender.createPacket(seqNum, dataList.get(dataIndex)));
                                sender.sendDataPacket(packetQueue.getLast(), seqNumLog);
                                seqNum++;
                                dataIndex++;
                            }
                        }

                        base = (base + numAcked) % 32;
                        seqNum = seqNum % 32;
                    } else {
                        System.out.println("Received Ack: " + receivedSeqNum + ". Ignoring.");
                        continue;
                    }


                }

                // Send EOT
                sender.sendDataPacket(packet.createEOT(seqNum), seqNumLog);

                seqNumLog.close();
                ackLog.close();
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        } else {
            System.out.println("Error! Illegal number of arguments.");
            System.out.println("Usage: java sender <emulatorName> <senderDataPort> <emulatorACKPort> <fileName>");
        }
    }
}
