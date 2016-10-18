import java.io.*;
import java.net.*;
import java.util.Random;

/**
 * Created by Thendup on 2016-10-16.
 */
public class server {
    public static void main(String argv[]) throws Exception {
        if (argv.length == 1) {
            // Stage 1: TCP
            String requisiteCode = argv[0];
            boolean requisitePass = false;
            int listenPort = 0;

            Random rand = new Random();
            int negotiationPort = rand.nextInt(9999) + 1024;
            System.out.println("SERVER_PORT=" + negotiationPort);
            ServerSocket welcomeSocket = new ServerSocket(negotiationPort);

            while (!requisitePass) {
                Socket connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

                String requisiteCodeC = inFromClient.readLine();
                if (requisiteCodeC.equals(requisiteCode)) {
                    System.out.println("Requisite Codes match!");
                    listenPort = rand.nextInt(9999)+1024;
                    String randomPort = String.valueOf(listenPort);
                    outToClient.writeBytes(randomPort + "\n");
                    requisitePass = true;
                } else {
                    System.out.println("Error! Requisite Codes don't match!");
                    outToClient.writeBytes("-1\n");
                }
            }

            // Stage 2: UCP
            if (requisitePass) {
                DatagramSocket serverSocket = new DatagramSocket(listenPort);
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];

                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData());
                System.out.println("Reversing string: " + sentence);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                String reversedSentence = new StringBuffer(sentence).reverse().toString();
                sendData = reversedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
                System.out.println("Done");
            }
        } else {
            System.out.println("Error! Illegal number of arguments.");
            System.out.println("Usage: ./server.sh <req_code>");
        }
    }
}
