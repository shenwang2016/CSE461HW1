/**
 * 
 */
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
/**
 * @author ylh96
 *
 */
public class Client {
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		
		// This part is a sample code that sends out a UDP packet
		BufferedReader inFromUser =
		         new BufferedReader(new InputStreamReader(System.in));
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		
		// needs header
		String sentence = inFromUser.readLine();
		int payload_length = sentence.length();
		// stage a, buffer should contain hello world
		// added 4 bytes just to play safe
		// stage a 1
		byte[] sendData = new byte[payload_length + 12 + 4];
		int header_field_1 = payload_length;  // 4 bytes
		int header_field_2 = 0;  // 4 bytes
		short header_field_3 = 1;  // 2 bytes
		short header_field_4 = 927;  // 2 bytes
		ByteBuffer header = ByteBuffer.allocate(12);
		header.putInt(header_field_1).putInt(header_field_2)
		.putShort(header_field_3).putShort(header_field_4);
		byte[] header_array = header.array();
		for (int i = 0; i < 12; i++) {
			sendData[i] = header_array[i];
		}
		byte[] sentence_byte = sentence.getBytes();
		sentence_byte = padding(sentence_byte, payload_length);
		for (int i = 0; i < payload_length; i++) {
			sendData[i + 12] = sentence_byte[i];
		}
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		// stage a 2
		byte[] receiveData = new byte[28];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		String modifiedSentence = new String(receivePacket.getData());
		System.out.println("FROM SERVER:" + modifiedSentence);
		clientSocket.close();
	}
	
	public static byte[] padding(byte[] ba, int content_length) {
		int padding_num = 4 - content_length % 4;
		if (padding_num < 4) {
			while (padding_num > 0) {
				byte temp = 0;
				ba[content_length + padding_num - 1] = temp; 
				padding_num--;
			}
		}
		return ba;
	}
	
	public static String decryptSecret(String fromServer) {
		return null;
	}

}
