import java.io.PrintWriter;

/**
 * Created by Thendup on 2016-11-07.
 */
public class receiver {
    public static void main(String argv[]) throws Exception {
        if (argv.length == 4) {
            String emulatorName = argv[0];
            int receiverACKPort = Integer.parseInt(argv[1]); // Emulator port to receive ACK from emulator
            int emulatorDataPort = Integer.parseInt(argv[2]); // Receiver port to receive data from emulator
            String fileName = argv[3];

            serverGBN receiver = new serverGBN(emulatorName, receiverACKPort, emulatorDataPort);
            try {
                PrintWriter outFile = new PrintWriter(fileName, "UTF-8");
                PrintWriter arrivalLog = new PrintWriter("arrival.log", "UTF-8");
                boolean lastPacketReceived = false;
                int seqNumExpected = 0;

                // Receiver listen loop
                while (!lastPacketReceived) {
                    packet arrived = receiver.receivePacket(false);
                    packet respondPacket;
                    if (arrived.getType() == 2) {
                        // EOT Packet received, signal end of while loop
                        lastPacketReceived = true;
                        respondPacket = packet.createEOT(arrived.getSeqNum());
                    } else {
                        // Packet arrived
                        arrivalLog.println(arrived.getSeqNum());
                        if (seqNumExpected == arrived.getSeqNum()) {
                            // Packet is expected, send ack for that packet
                            outFile.println(new String(arrived.getData()));
                            respondPacket = packet.createACK(arrived.getSeqNum());
                            seqNumExpected++;
                        } else {
                            // Packet is not expected, send ack for most recent packet received
                            respondPacket = packet.createACK(seqNumExpected-1);
                        }
                    }
                    seqNumExpected = seqNumExpected % 32;
                    receiver.sendDataPacket(respondPacket, null);
                }

                outFile.close();
                arrivalLog.close();
                System.out.println("End!");
            } catch (Exception e) {
                System.out.println("Error with Receiver: " + e);
            }
        } else {
            System.out.println("Error! Illegal number of arguments.");
            System.out.println("Usage: java receiver <emulatorName> <receiverACKPort> <emulatorDataPort> <fileName>");
        }
    }
}
