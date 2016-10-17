import java.io.*;
import java.net.*;
import java.util.Random;

/**
 * Created by Thendup on 2016-10-16.
 */
public class server {
    public static void main(String argv[]) throws Exception {

        // Stage 1: TCP
        String requisiteCode = argv[0];
        boolean requisitePass = false;
        int listenPort = 0;

        Random rand = new Random();
        int negotiationPort = rand.nextInt(9999) + 1024;
        System.out.println("SERVER_PORT=" + negotiationPort);

        ServerSocket welcomeSocket = new ServerSocket(negotiationPort);
        Socket connectionSocket = welcomeSocket.accept();
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        String requisiteCodeC = inFromClient.readLine();

        if (requisiteCodeC.equals(requisiteCode)) {
            listenPort = rand.nextInt(9999)+1024;
            String randomPort = String.valueOf(listenPort);
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            outToClient.writeBytes(randomPort + "\n");
            requisitePass = true;
        } else {
            System.out.println("Error! Requisite Codes don't match!");
        }

        // Stage 2: UCP
        if (requisitePass) {
            DatagramSocket serverSocket = new DatagramSocket(listenPort);
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            InetAddress IPAddress =receivePacket.getAddress();
            int port = receivePacket.getPort();
            String reversedSentence = new StringBuffer(sentence).reverse().toString();
            sendData = reversedSentence.getBytes();
            DatagramPacket sendPacket =new DatagramPacket(sendData,sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }


    }
}
