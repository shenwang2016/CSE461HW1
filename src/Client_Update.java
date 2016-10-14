import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * 
 */

/**
 * @author ylh96
 *
 */
public class Client_Update {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		int[] secrets = new int[4];
		int[] result_from_a = part1_stageA();
		System.out.println("Stage A done");
	}
	
	public static int[] part1_stageB(int[] data_from_prev) throws Exception {
		
		
		return null;
	}
	
	public static int[] part1_stageA() throws UnknownHostException, IOException {
		InetAddress IPAddress = InetAddress.getByName("attu2.cs.washington.edu");
		DatagramSocket clientSocket = new DatagramSocket();
		clientSocket.connect(IPAddress, 12235);
		if (clientSocket.isConnected()) {
			System.out.println("Connected");
		} else {
			System.out.println("Disconnected");
			clientSocket.close();
			return null;
		}
		String message = "hello world";
		message = message + "\0";
		byte[] message_byte = message.getBytes("ISO-8859-1");
		int stepA1 = message_byte.length;
		int padding = padding_bytes(stepA1);
		ByteBuffer sendData = ByteBuffer.allocate(stepA1 + 12 + padding);
		sendData.putInt(stepA1).putInt(0).putShort((short) 1).putShort((short) 927);
		sendData.put(message_byte);
		byte[] send_data_a = sendData.array();
		// send packet from client to server
		DatagramPacket sendPacket = new DatagramPacket(send_data_a, send_data_a.length, IPAddress, 12235);
		clientSocket.send(sendPacket);
		// stage a2
		// send packet from server to client
		byte[] receiveData = new byte[28];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		clientSocket.receive(receivePacket);
		// extract data from server packet
		byte[] fromServer = receivePacket.getData();
		ByteBuffer bf = ByteBuffer.wrap(fromServer);
		int num = bf.getInt(12);
		int len = bf.getInt(16);
		int udp_port = bf.getInt(20);
		int secretA = bf.getInt(24);
		System.out.println(secretA);
		int[] result = {num, len, udp_port, secretA};
		clientSocket.close();
		return result;
	}
	
	public static int padding_bytes(int length) {
		if (length % 4 == 0) {
			return 0;
		} else {
			return 4 - length % 4;
		}
	}

}
