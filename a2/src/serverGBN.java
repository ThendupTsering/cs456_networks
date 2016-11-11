import java.io.*;
import java.net.*;
import java.util.LinkedList;


/**
 * Created by Thendup on 2016-11-09.
 */
public class serverGBN { // server Go Back N

    private InetAddress IPAddress;
    private int emulatorReceivePort;
    private DatagramSocket socket;

    public serverGBN(String emulatorAddress, int emulatorReceivePort, int emulatorSendPort) throws IOException {
        this.IPAddress = InetAddress.getByName(emulatorAddress);
        this.emulatorReceivePort = emulatorReceivePort;
        this.socket = new DatagramSocket(emulatorSendPort);
    }

    public packet createPacket(int seqNum, String data) {
        packet dataPacket;
        try {
            dataPacket = packet.createPacket(seqNum, data);
        } catch (Exception e) {
            System.out.println("Error creating packet: " + e);
            dataPacket = null;
        }
        return dataPacket;
    }

    public void sendDataPacket(packet dataPacket, PrintWriter seqNumLog) {
        if (dataPacket.getType() == 1) {
            seqNumLog.println(dataPacket.getSeqNum());
        }
        byte[] data = dataPacket.getUDPdata();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, this.IPAddress, this.emulatorReceivePort);
        try {
            System.out.println("Sending- Type: " + dataPacket.getType() + " SeqNum: " + dataPacket.getSeqNum());
            this.socket.send(sendPacket);
        } catch (Exception e) {
            System.out.println("Error sending packet seqNum: " + dataPacket.getSeqNum() + ", " + e);
        }
    }

    public void sendAllPackets(LinkedList<packet> sendQueue, PrintWriter seqNumLog) {
        for (packet p: sendQueue) {
            this.sendDataPacket(p, seqNumLog);
        }
    }

    public packet receivePacket(boolean timeout) {
        byte[] data = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(data, data.length);
        packet received;
        try {
            if (timeout) {
                socket.setSoTimeout(250);
            }
            socket.receive(receivePacket);
            received = packet.parseUDPdata(data);
            System.out.println("Received- Type: " + received.getType() + " SeqNum: " + received.getSeqNum());
        } catch (Exception e) {
            // Packet was not received and there was a timeout. Skip iteration!
            received = null;
        }
        return received;
    }
}
