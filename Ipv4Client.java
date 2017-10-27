
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Random;

/*  Oscar Alcaraz & Ismail Abbas
	CS 380 Networks
	Project 3
*/

public class Ipv4Client {

    private static int packetSize = 2;
    private static String recievedResponse = "empty";

    private static final int VERSION = 4;
    private static final int H_LEN = 5;
    private static final int TOS = 0;
    private static final byte INDENT = 0;
    private static final int FLAG = 2;
    private static final byte OFFSET = 0;
    private static final int TTL = 50;
    private static final int PROTOCOL = 6; //TCP
    
    private static final byte[] SOURCE_ADDR = {(byte) 172, (byte) 217, 11, 78};

    //IP Address
    private static byte[] DESTINTION_ADDR;

    public static void main(String[] args) throws Exception {
    	
        try {
        	
            Socket socket = new Socket("18.221.102.182", 38003);
            System.out.println("\nConnected to Server.");
            
            OutputStream os = socket.getOutputStream(); // Outputstream to send packet
            DESTINTION_ADDR = socket.getInetAddress().getAddress(); // Get address from server

            while (packetSize <= 4096) {
            	
                System.out.println("\nData length: " + packetSize);
                byte[] packet = generatePacket(packetSize); // Build packet
                
                os.write(packet); // Send the packet
                
                receiveMessage(socket); // Receive the message from server
                System.out.println("Response: " + recievedResponse);
                
                packetSize *= 2;
            }

          socket.close();
          System.out.println("\nDisconnected from Server.");
          
        } catch (Exception e) { e.printStackTrace(); }
        
    }

    // Receives the message from the server to display
    // on whether the packet was formatted right
    public static void receiveMessage(Socket socket) throws Exception {
    	
        try {
        	
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            
            BufferedReader br = new BufferedReader(isr);
            recievedResponse = br.readLine();
            
        } catch (Exception e) { e.printStackTrace(); }
        
    }

    // Builds the packet with the proper values,
    // calculates the check sum, and returns the packet
    public static byte[] generatePacket(int size) {

        int length = size + 20;
        byte[] packet = new byte[length];

        packet[0] = (VERSION * 16) + H_LEN;
        packet[1] = TOS;
        packet[2] = (byte) (length >> 8);
        packet[3] = (byte) length;
        packet[4] = INDENT;
        packet[5] = INDENT;
        packet[6] = (byte) (FLAG * 32);
        packet[7] = OFFSET;
        packet[8] = TTL;
        packet[9] = PROTOCOL;

        packet = placeAddress(packet);
        packet = placeDataInPacket(packet, size);

        byte[] checkSum = getCheckSum(packet);
        
        packet[10] = checkSum[0];
        packet[11] = checkSum[1];

        return packet;
        
    }

    // Places the data in the appropriate locations
    // in the packet
    public static byte[] placeDataInPacket(byte[] packet, int size) {
    	
        byte[] data = generateData(size);

        int count = 20;
        
        for(int k = 0; k < data.length; ++k) {
        	
            packet[count++] = data[k];
        }
        
        return packet;
        
    }

    // Generates an array with data with random
    // values for the packet
    public static byte[] generateData(int size) {
    	
        byte[] data = new byte[size];
        Random rand = new Random();
        
        for(int i = 0; i < data.length; ++i) {
        	
            data[i] = 0;//(byte) rand.nextInt(256);
        }
        
        return data;
        
    }

    // will place the source and destination address in
    // the appropriate locations in packet
    public static byte[] placeAddress(byte[] packet) {
    	
        int count = 0;
        for(int i = 12; i < 16; ++i) {
        	
            packet[i] = SOURCE_ADDR[count++];
        }
        
        count = 0;
        for(int k = 16; k < 20; ++k) {
        	
            packet[k] = DESTINTION_ADDR[count++];
        }
        
        return packet;
        
    }

    // Returns the checksum as a byte array
    // so that it can then be placed in the proper
    // indecies in the packet
    public static byte[] getCheckSum(byte[] packet) {
    	
        short checkSum = checkSum(packet);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        
        buffer.putShort(checkSum);
        return buffer.array();
        
    }

    // Will calculate the check sum for the packet
    public static short checkSum(byte[] b) {
    	
        long sum = 0;
        int length = b.length;
        int i = 0;
        long highVal;
        long lowVal;
        long value;

        while(length > 1){
        	
            //gets the two halves of the whole byte and adds to the sum
            highVal = ((b[i] << 8) & 0xFF00);
            lowVal = ((b[i + 1]) & 0x00FF);
            
            value = highVal | lowVal;
            sum += value;

            //check for the overflow
            if ((sum & 0xFFFF0000) > 0) {
            	
                sum = sum & 0xFFFF;
                sum += 1;
            }

            //iterates
            i += 2;
            length -= 2;
            
        }
        //leftover bits
        if(length > 0){
        	
            sum += (b[i] << 8 & 0xFF00);
            if ((sum & 0xFFFF0000) > 0) {
            	
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        sum = ~sum;
        sum = sum & 0xFFFF;
        
        return (short)sum;
        
    }
}