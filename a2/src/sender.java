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

                // Take file name and parse file data into array of strings that are max 500 chars
                try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                    String fileLine;
                    while ((fileLine = reader.readLine()) != null) {
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


                // Begin to send data from string array
                int timeout = 100;
                serverGBN sender = new serverGBN(emulatorName, senderDataPort, emulatorACKPort);
                int seqNum = 0;
                int dataIndex = 0;
                int base = 0;
                int windowSize = 10;
                int dataListSize = dataList.size();
                LinkedList<packet> packetQueue = new LinkedList<>();

                // Start with First 10 Packets, send them to queue
                int initLimit = Math.min(windowSize, dataListSize);
                for (int i = 0; i < initLimit; i++) {
                    packetQueue.add(sender.createPacket(seqNum, dataList.get(i)));
                    seqNum++;
                    dataIndex++;
                }

                // Send first 10 packets in queue
                System.out.println("Send first 10 packets");
                sender.sendAllPackets(packetQueue, seqNumLog);

                Date beginTime = new Date();

                // Begin loop for remaining packets
                while (!packetQueue.isEmpty()) {
                    packet receivedPacket;
                    Date currentTime = new Date();

                    // Timeout handler
                    if (currentTime.getTime() - beginTime.getTime() > timeout) {
                        System.out.println("Timeout occurred. Resending all packets in queue.");
                        sender.sendAllPackets(packetQueue, seqNumLog);
                        beginTime = currentTime;
                    }

                    // Ensure packet is received from the receiver, otherwise end this loop iteration
                    try {
                        receivedPacket = sender.receivePacket(true);
                        if (receivedPacket == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        // Packet was not received and there was a timeout. Skip iteration!
                        continue;
                    }

                    // At this point, packet has been received
                    int receivedSeqNum = receivedPacket.getSeqNum();
                    ackLog.println(receivedSeqNum);
                    if (receivedSeqNum >= base) {
                        // Update base. All packets before receivedSeqNum have been acked

                        int numAcked = receivedSeqNum - base + 1;
                        // Remove the n acked packets from the queue
                        for (int i = 0; i < numAcked; i++) {
                            packetQueue.removeFirst();
                        }

                        // Add n packets to the queue if there are any in the string array
                        for (int i = 0; i < numAcked; i++) {
                            if (dataIndex < dataListSize) {
                                packetQueue.add(sender.createPacket(seqNum, dataList.get(dataIndex)));
                                sender.sendDataPacket(packetQueue.getLast(), seqNumLog);
                                seqNum++;
                                dataIndex++;
                            }
                        }

                        // Update base and seqNum accordingly
                        base = (base + numAcked) % 32;
                        seqNum = seqNum % 32;
                    } else {
                        // Ignore duplicate acks.
                        continue;
                    }

                }

                // Send EOT
                sender.sendDataPacket(packet.createEOT(seqNum), seqNumLog);

                seqNumLog.close();
                ackLog.close();

                System.out.println("End!");
            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        } else {
            System.out.println("Error! Illegal number of arguments.");
            System.out.println("Usage: java sender <emulatorName> <senderDataPort> <emulatorACKPort> <fileName>");
        }
    }
}
