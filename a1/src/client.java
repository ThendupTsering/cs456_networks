import java.io.*;
import java.net.*;

/**
 * Created by Thendup on 2016-10-16.
 */


public class client {
    public static void main(String argv[]) throws Exception {
        String serverAddress = argv[0];
        int negotiationPortC = Integer.parseInt(argv[1]);
        String requisiteCode = argv[2];
        String message = argv[3];
        int randomPort = 0;

        // Stage 1: TCP
        Socket clientSocketTCP = new Socket(serverAddress, negotiationPortC);
        DataOutputStream outToServer = new DataOutputStream(clientSocketTCP.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocketTCP.getInputStream()));

        outToServer.writeBytes(requisiteCode + "\n");
        randomPort = Integer.parseInt(inFromServer.readLine());
        clientSocketTCP.close();

        // Stage 2: UCP
        if (randomPort != 0) {
            DatagramSocket clientSocketUCP = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName(serverAddress);
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, randomPort);
            clientSocketUCP.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocketUCP.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData());
            System.out.println("FROM SERVER:" + modifiedSentence);
            clientSocketUCP.close();
        } else {
            System.out.println("Error receiving random port from server. Current random port: " + randomPort);
        }


    }
}